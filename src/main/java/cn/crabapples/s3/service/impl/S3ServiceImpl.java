package cn.crabapples.s3.service.impl;

import cn.crabapples.s3.config.S3ConfigProperties;
import cn.crabapples.s3.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class S3ServiceImpl implements S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3ServiceImpl.class);
    private final S3ConfigProperties config;
    private final ConcurrentHashMap<String, List<CompletedPart>> multipartMap = new ConcurrentHashMap<>();


    public S3ServiceImpl(S3ConfigProperties config) {
        this.config = config;
    }

    private S3Client s3Client = null;
    private S3Presigner s3Presigner = null;

    @Override
    public S3Client getClient() {
        return s3Client;
    }

    @Override
    public S3ConfigProperties getConfig() {
        return config;
    }


    @Override
    @PostConstruct
    public void init() {
        logger.info("初始化S3服务");
        s3Client = S3Client.builder()
                .endpointOverride(URI.create(config.getUrl())) // RustFS 地址
                .region(Region.of(config.getRegion())) // 可写死，RustFS 不校验 region
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())
                        )
                )
                .forcePathStyle(true) // 关键配置！RustFS 需启用 Path-Style
                .build();
        s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(config.getUrl())) // RustFS 地址
                .region(Region.of(config.getRegion())) // 可写死，RustFS 不校验 region
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())
                        )
                )
                .build();
        logger.info("初始化S3服务完成");
    }

    @Override
    public CreateBucketResponse createBucket() {
        return createBucket(config.getBucketName());
    }

    @Override
    public CreateBucketResponse createBucket(String bucket) {
        logger.debug("创建Bucket: [{}]", bucket);
        try {
            return s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            logger.error("Bucket[{}]已经存在", bucket);
            throw new RuntimeException("Bucket已经存在");
        }
    }

    @Override
    public List<Bucket> listBuckets() {
        return s3Client.listBuckets().buckets();
    }

    @Override
    public DeleteBucketResponse deleteBucket(String bucket) {
        logger.info("删除Bucket: [{}]", bucket);
        return s3Client.deleteBucket(builder -> builder.bucket(bucket));
    }

    @Override
    public PutObjectResponse uploadFile(String fileName, RequestBody requestBody) {
        return uploadFile(config.getBucketName(), fileName, requestBody);
    }

    @Override
    public PutObjectResponse uploadFile(String bucket, String fileName, RequestBody requestBody) {
        logger.info("上传文件: [{}]", fileName);
        return s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .build(),
                requestBody
        );
    }

    @Override
    public PutObjectResponse uploadFile(String fileName, String path) {
        return uploadFile(config.getBucketName(), fileName, path);
    }

    @Override
    public PutObjectResponse uploadFile(String bucket, String fileName, String path) {
        logger.info("上传文件: [{}]", fileName);
        return s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName).build(),
                Paths.get(path)
        );
    }

    @Override
    public List<S3Object> listFiles() {
        return listFiles(config.getBucketName());
    }

    @Override
    public List<S3Object> listFiles(String bucket) {
        return s3Client.listObjectsV2(builder -> builder.bucket(bucket)).contents();
    }

    @Override
    public DeleteObjectResponse deleteFile(String fileName) {
        return deleteFile(config.getBucketName(), fileName);
    }

    @Override
    public DeleteObjectResponse deleteFile(String bucket, String fileName) {
        logger.info("删除文件: [{}]", fileName);
        return s3Client.deleteObject(builder -> builder.bucket(bucket).key(fileName));
    }

    @Override
    public GetObjectResponse downloadFile(String fileName, String path) {
        return downloadFile(config.getBucketName(), fileName, path);
    }


    @Override
    public GetObjectResponse downloadFile(String bucket, String fileName, String path) {
        logger.info("下载文件: [{}]", fileName);
        return s3Client.getObject(builder -> builder.bucket(bucket).key(fileName), Paths.get(path));
    }

    @Override
    public ResponseInputStream<GetObjectResponse> downloadFileAsStream(String fileName) {
        return downloadFileAsStream(config.getBucketName(), fileName);
    }

    @Override
    public ResponseInputStream<GetObjectResponse> downloadFileAsStream(String bucket, String fileName) {
        logger.info("下载文件: [{}]", fileName);
        return s3Client.getObject(builder -> builder.bucket(bucket)
                .key(fileName));
    }

    @Override
    public ResponseBytes<GetObjectResponse> downloadFileAsByte(String fileName) {
        return downloadFileAsByte(config.getBucketName(), fileName);
    }

    @Override
    public ResponseBytes<GetObjectResponse> downloadFileAsByte(String bucket, String fileName) {
        logger.info("下载文件: [{}]", fileName);
        return s3Client.getObjectAsBytes(builder -> builder.bucket(bucket)
                .key(fileName));
    }

    @Override
    public ResponseInputStream<GetObjectTorrentResponse> downloadFileAsTorrentStream(String fileName) {
        return downloadFileAsTorrentStream(config.getBucketName(), fileName);
    }

    @Override
    public ResponseInputStream<GetObjectTorrentResponse> downloadFileAsTorrentStream(String bucket, String fileName) {
        logger.info("下载文件: [{}]", fileName);
        return s3Client.getObjectTorrent(builder -> builder.bucket(bucket)
                .key(fileName));
    }


    @Override
    public PresignedGetObjectRequest createTempDownloadUrl(String fileName, Long time, TemporalUnit unit) {
        return createTempDownloadUrl(config.getBucketName(), fileName, time, unit);
    }

    @Override
    public PresignedGetObjectRequest createTempDownloadUrl(String bucket, String fileName, Long time, TemporalUnit unit) {
        logger.info("分享文件: [{}]", fileName);
        Duration duration = Duration.of(time, unit);
        return s3Presigner.presignGetObject(request -> request.getObjectRequest(builder ->
                        builder.bucket(bucket).key(fileName).build())
                .signatureDuration(duration).build());
    }

    @Override
    public PresignedPutObjectRequest createTempUploadUrl(String fileName) {
        return createTempUploadUrl(fileName, 5L, ChronoUnit.MINUTES);
    }

    @Override
    public PresignedPutObjectRequest createTempUploadUrl(String fileName, Long time, TemporalUnit unit) {
        return createTempUploadUrl(config.getBucketName(), fileName, time, unit);
    }

    @Override
    public PresignedPutObjectRequest createTempUploadUrl(String bucket, String fileName) {
        return createTempUploadUrl(bucket, fileName, 5L, ChronoUnit.MINUTES);
    }

    @Override
    public PresignedPutObjectRequest createTempUploadUrl(String bucket, String fileName, Long time, TemporalUnit unit) {
        logger.info("生成临时上传地址: [{}]", fileName);
        return s3Presigner.presignPutObject(
                build ->
                        build.putObjectRequest(b -> b.bucket(bucket).key(fileName).build())
                                .signatureDuration(Duration.of(time, unit))
        );
    }


    @Override
    public String createMultipartUploadId(String fileName) {
        return createMultipartUploadId(config.getBucketName(), fileName);
    }

    @Override
    public String createMultipartUploadId(String bucket, String fileName) {
        logger.info("生成分片上传ID: [{}]", fileName);
        CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(builder ->
                builder.bucket(bucket)
                        .key(fileName)
                        .build());
        String uploadId = createResponse.uploadId();
        multipartMap.put(uploadId, new LinkedList<>());
        logger.info("分片上传ID: [{}]", uploadId);
        return uploadId;
    }

    @Override
    public List<CompletedPart> multipartUploadParts(String fileName, String uploadId, RequestBody requestBody) {
        return multipartUploadParts(config.getBucketName(), fileName, uploadId, requestBody);
    }

    @Override
    public List<CompletedPart> multipartUploadParts(String bucket, String fileName, String uploadId, RequestBody requestBody) {
        logger.info("上传分片,ID:[{}]: [{}]", uploadId, fileName);
        List<CompletedPart> completedParts = multipartMap.get(uploadId);
        Integer index = completedParts.size() + 1;
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .uploadId(uploadId)
                .partNumber(index)
                .build();
        UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody);
        completedParts.add(CompletedPart.builder()
                .partNumber(index)
                .eTag(uploadPartResponse.eTag())
                .build()
        );
        multipartMap.put(uploadId, completedParts);
        logger.info("上传分片完成,ID:[{}]: [{}]", uploadId, fileName);
        return completedParts;
    }

    @Override
    public List<CompletedPart> multipartUploadParts(String fileName, String partPath, String uploadId) {
        return multipartUploadParts(config.getBucketName(), fileName, partPath, uploadId);
    }

    @Override
    public List<CompletedPart> multipartUploadParts(String bucket, String fileName, String partPath, String uploadId) {
        logger.info("上传分片,ID:[{}]: [{}]", uploadId, fileName);
        List<CompletedPart> completedParts = multipartMap.get(uploadId);
        Integer index = completedParts.size() + 1;
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .uploadId(uploadId)
                .partNumber(index)
                .build();
        UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, Paths.get(partPath));
        completedParts.add(CompletedPart.builder()
                .partNumber(index)
                .eTag(uploadPartResponse.eTag())
                .build()
        );
        multipartMap.put(uploadId, completedParts);
        logger.info("上传分片完成,ID:[{}]: [{}]", uploadId, fileName);
        return completedParts;
    }

    @Override
    public CompleteMultipartUploadResponse completedMultipartUpload(String fileName, String uploadId) {
        return completedMultipartUpload(config.getBucketName(), fileName, uploadId);
    }

    @Override
    public CompleteMultipartUploadResponse completedMultipartUpload(String bucket, String fileName, String uploadId) {
        logger.info("合并分片,ID:[{}]: [{}]", uploadId, fileName);
        CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                .parts(multipartMap.get(uploadId))
                .build();
        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .uploadId(uploadId)
                .multipartUpload(completedUpload)
                .build();
        CompleteMultipartUploadResponse completeResponse = s3Client.completeMultipartUpload(completeRequest);
        multipartMap.remove(uploadId);
        logger.info("合并分片完成,ID:[{}]: [{}]", uploadId, fileName);
        return completeResponse;
    }

    @Override
    public AbortMultipartUploadResponse abortMultipartUpload(String fileName, String uploadId) {
        return abortMultipartUpload(config.getBucketName(), fileName, uploadId);
    }

    @Override
    public AbortMultipartUploadResponse abortMultipartUpload(String bucket, String fileName, String uploadId) {
        logger.info("取消分片上传,ID:[{}]: [{}]", uploadId, fileName);
        AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .uploadId(uploadId)
                .build();
        AbortMultipartUploadResponse abortResponse = s3Client.abortMultipartUpload(abortRequest);
        multipartMap.remove(uploadId);
        logger.info("取消分片上传完成,ID:[{}]: [{}]", uploadId, fileName);
        return abortResponse;
    }
}
