package cn.crabapples.s3;

import cn.crabapples.s3.config.S3ConfigProperties;
import cn.crabapples.s3.service.impl.S3ServiceImpl;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class S3Example {
    private static final String ACCESS_KEY = "gQ6MwJZxCoPX4qoh2O80";
    private static final String SECRET_KEY = "vLmlVPM1mG2cBjBu8mLXhNBpdGkRbgpBJBT9S5yz";
    private static final String URL = "http://192.168.31.60:9000";
    private static final String BUCKET_NAME = "test-bucket";
    private static final String REGION = "region";
    private static final S3ServiceImpl service;

    static {
        S3ConfigProperties properties = new S3ConfigProperties();
        properties.setAccessKey(ACCESS_KEY);
        properties.setSecretKey(SECRET_KEY);
        properties.setUrl(URL);
        properties.setBucketName(BUCKET_NAME);
        properties.setRegion(REGION);
        service = new S3ServiceImpl(properties);
        service.init();
    }


    /**
     * 列出bucket
     */
    public void listBuckets() {
        List<Bucket> buckets = service.listBuckets();
        System.err.println(buckets);
    }

    /**
     * 创建bucket
     */
    public void createBucket() {
        service.createBucket(null);
    }

    /**
     * 上传文件
     */
    public void upload() {
        service.uploadFile("1/2/3/test.jpg", "/Users/mshe/developer/1.jpg");
    }

    /**
     * 上传文件
     */
    public void uploadFromStream() throws IOException {
        File file = new File("/Users/mshe/developer/1.jpg");
        RequestBody requestBody = RequestBody.fromInputStream(new FileInputStream(file), file.length());
        service.uploadFile("test1.jpg", requestBody);
    }


    /**
     * 下载文件
     */
    public void download() {
        service.downloadFile("test1.jpg", "/Users/mshe/developer/2.jpg");
    }


    /**
     * 下载文件为流
     */
    public void downloadAsStream() throws IOException {
        ResponseInputStream<GetObjectResponse> responseInputStream = service.downloadFileAsStream("test1.jpg");
//        byte[] bytes = responseInputStream.readAllBytes();
//        FileOutputStream outputStream = new FileOutputStream("/Users/mshe/developer/3.jpg");
//        outputStream.write(bytes);
//        outputStream.close();
        byte[] b = new byte[1024];
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/mshe/developer/4.jpg");
        for (int i = responseInputStream.read(b); i != -1; i = responseInputStream.read(b)) {
            fileOutputStream.write(b, 0, i);

        }
        fileOutputStream.close();
    }


    /**
     * 下载文件为byte[]
     */
    public void downloadAsByte() throws IOException {
        ResponseBytes<GetObjectResponse> responseBytes = service.downloadFileAsByte("test1.jpg");
        try (InputStream inputStream = responseBytes.asInputStream()) {
            FileOutputStream fileOutputStream = new FileOutputStream("/Users/mshe/developer/5.jpg");
            byte[] b = new byte[1024];
            for (int i = inputStream.read(b); i != -1; i = inputStream.read(b)) {
                fileOutputStream.write(b, 0, i);
            }
            fileOutputStream.close();
        }
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/mshe/developer/6.jpg");
        fileOutputStream.write(responseBytes.asByteArray());
        fileOutputStream.close();
    }


    /**
     * 创建临时下载url
     */
    public void tempDownloadUrl() {
        PresignedGetObjectRequest presignedGetObjectRequest = service.createTempDownloadUrl("test1.jpg", 1L, ChronoUnit.MINUTES);
        URL url = presignedGetObjectRequest.url();
        System.err.println(url);
    }


    /**
     * 创建临时上传url
     */
    public void tempUploadUrl() {
        PresignedPutObjectRequest tempUploadUrl = service.createTempUploadUrl("test2.jpg", 5L, ChronoUnit.MINUTES);
        URL url = tempUploadUrl.url();
        System.err.println(url);
    }


    /**
     * 分片上传
     */
    public void multipartUpload() throws IOException {
        String uploadId = service.createMultipartUploadId("etcd.tar");
        File file = new File("/Users/mshe/developer/etcd.tar");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] b = new byte[1024 * 1024 * 5];
            for (int i = fileInputStream.read(b); i != -1; i = fileInputStream.read(b)) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(b, 0, i);
                RequestBody requestBody = RequestBody.fromRemainingByteBuffer(byteBuffer);
                service.multipartUploadParts("etcd.tar", uploadId, requestBody);
            }
            service.completedMultipartUpload("etcd.tar", uploadId);
        }
    }

}
