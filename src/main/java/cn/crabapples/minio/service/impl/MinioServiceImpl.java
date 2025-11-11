package cn.crabapples.minio.service.impl;

import cn.crabapples.minio.config.MinioConfigProperties;
import cn.crabapples.minio.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.utils.Md5Utils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
public class MinioServiceImpl implements MinioService {
    private static final Logger logger = LoggerFactory.getLogger(MinioServiceImpl.class);
    private final MinioConfigProperties config;
    private MinioClient minioClient;

    public MinioClient getClient() {
        return minioClient;
    }

    public MinioServiceImpl(MinioConfigProperties config) {
        this.config = config;
    }


    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(config.getUrl())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }

    public List<Bucket> listBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            throw new RuntimeException("Bucket 获取失败", e);
        }
    }

    public void createBucket() {
        createBucket(config.getBucketName());
    }

    public void createBucket(String bucket) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        } catch (Exception e) {
            throw new RuntimeException("Bucket 创建失败", e);
        }
    }

    public void removeBucket() {
        removeBucket(config.getBucketName());
    }

    public void removeBucket(String bucket) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
        } catch (Exception e) {
            throw new RuntimeException("Bucket 删除失败", e);
        }
    }

    public ObjectWriteResponse uploadFile(String fileName, String path) {
        return uploadFile(config.getBucketName(), fileName, path);
    }

    public ObjectWriteResponse uploadFile(String bucket, String fileName, String path) {
        logger.debug("开始上传文件到Minio:[{}]", fileName);
        try {
            UploadObjectArgs args = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .filename(path)
                    .build();
            ObjectWriteResponse objectWriteResponse = minioClient.uploadObject(args);
            return objectWriteResponse;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }


    public ObjectWriteResponse uploadFile(String fileName, InputStream inputStream) {
        return uploadFile(config.getBucketName(), fileName, inputStream);
    }

    public ObjectWriteResponse uploadFile(String bucket, String fileName, InputStream inputStream) {
        logger.debug("开始上传文件到Minio:[{}]", fileName);
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(args);
            inputStream.close();
            return objectWriteResponse;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    public void downloadAsStream(String fileName, OutputStream outputStream) {
        downloadAsStream(config.getBucketName(), fileName, outputStream);
    }

    public void downloadAsStream(String bucket, String fileName, OutputStream outputStream) {
        logger.debug("开始从Minio下载文件:[{}]", fileName);
        try (BufferedOutputStream stream = new BufferedOutputStream(outputStream)) {
            GetObjectResponse object = downloadFile(bucket, fileName);
            byte[] data = new byte[1024];
            for (int i = object.read(data); i != -1; i = object.read(data)) {
                stream.write(data, 0, i);
            }
            stream.flush();
            object.close();
            logger.debug("从Minio下载文件[{}]完成", fileName);
        } catch (Exception e) {
            logger.error("从Minio下载文件[{}]失败", fileName, e);
            throw new RuntimeException(e);
        }
    }

    public GetObjectResponse downloadFile(String fileName) {
        return downloadFile(config.getBucketName(), fileName);
    }

    public GetObjectResponse downloadFile(String bucket, String fileName) {
        logger.debug("开始从Minio下载文件:[{}]", fileName);
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName).build();
            return minioClient.getObject(args);
        } catch (Exception e) {
            logger.error("从Minio下载文件[{}]失败", fileName, e);
            throw new RuntimeException(e);
        }
    }


    public String createTempDownloadUrl(String fileName) {
        return createTempDownloadUrl(fileName, 30, TimeUnit.MINUTES);
    }

    public String createTempDownloadUrl(String bucket, String fileName) {
        return createTempDownloadUrl(bucket, fileName, 30, TimeUnit.MINUTES);
    }

    public String createTempDownloadUrl(String fileName, int time, TimeUnit unit) {
        return createTempDownloadUrl(config.getBucketName(), fileName, time, unit);
    }

    public String createTempDownloadUrl(String bucket, String fileName, int time, TimeUnit unit) {
        try {
            logger.debug("开始从Minio获取文件分享连接:[{}]", fileName);
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .method(Method.GET)
                    .expiry(time, unit)
                    .build();
            String shareUrl = minioClient.getPresignedObjectUrl(args);
            logger.debug("从Minio获取文件分享连接:[{}]完成,分享地址为:[{}]", fileName, shareUrl);
            return shareUrl;
        } catch (Exception e) {
            throw new RuntimeException("文件分享失败", e);
        }
    }

    public void remove(String fileName) {
        remove(config.getBucketName(), fileName);
    }

    public void remove(String bucket, String fileName) {
        try {
            logger.debug("开始从Minio删除文件:[{}]", fileName);
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName).build();
            minioClient.removeObject(args);
            logger.debug("从Minio删除文件:[{}]完成", fileName);
        } catch (Exception e) {
            throw new RuntimeException("文件分享失败", e);
        }
    }

    /**
     * 分片上传
     *
     * @param data     文件数据
     * @param uploadId uploadId
     * @param index    分片索引
     */
    public void multipartUpload(byte[] data, String uploadId, int index) {
        multipartUpload(config.getBucketName(), data, uploadId, index);
    }

    /**
     * 分片上传
     *
     * @param bucket   bucket
     * @param data     文件数据
     * @param uploadId uploadId
     * @param index    分片索引
     */
    public void multipartUpload(String bucket, byte[] data, String uploadId, int index) {
        logger.debug("开始上传Minio分片文件:[{}],index:[{}]", uploadId, index);
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(uploadId + "/" + index + ".chunk")
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .build();
            minioClient.putObject(args);
            logger.debug("上传分片文件:[{}],index:[{}]完成", uploadId, index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void mergeMultipart(String fileName, String uploadId) {
        mergeMultipart(config.getBucketName(), fileName, uploadId);
    }

    public void mergeMultipart(String bucket, String fileName, String uploadId) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(uploadId)
                            .delimiter("/")
                            .recursive(true)
                            .build());
            List<Item> items = new ArrayList<>();
            for (Result<Item> result : results) {
                items.add(result.get());
            }
            List<ComposeSource> composeSources = items.stream()
                    .sorted((a, b) -> a.lastModified().isAfter(b.lastModified()) ? 1 : -1)
                    .map(item -> ComposeSource.builder().object(item.objectName()).bucket(bucket).build())
                    .peek(composeSource -> logger.debug("合并分片文件:[{}]", composeSource.object()))
                    .collect(Collectors.toList());
            logger.debug("开始合并Minio分片文件:[{}]", fileName);
            ComposeObjectArgs args = ComposeObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .sources(composeSources)
                    .build();
            minioClient.composeObject(args);
            List<DeleteObject> deleteObjectList = composeSources.stream()
                    .map(e -> new DeleteObject(e.object()))
                    .collect(Collectors.toList());
            Iterable<Result<DeleteError>> removed = minioClient.removeObjects(RemoveObjectsArgs
                    .builder()
                    .bucket(bucket)
                    .objects(deleteObjectList)
                    .build());
            for (Result<DeleteError> result : removed) {
                DeleteError deleteError = result.get();
                logger.debug("删除分片文件:[{}]", deleteError.objectName());
            }
            logger.debug("合并Minio分片文件:[{}]完成", fileName);
        } catch (Exception e) {
            throw new RuntimeException("文件合并失败", e);
        }
    }
}
