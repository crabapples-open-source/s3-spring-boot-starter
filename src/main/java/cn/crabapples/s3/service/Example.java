package cn.crabapples.s3.service;

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

public class Example {
    S3Service s3Service;

    /**
     * 列出bucket
     */
    public void test1() {
        List<Bucket> buckets = s3Service.listBuckets();
        System.err.println(buckets);
    }

    /**
     * 创建bucket
     */
    public void test2() {
        s3Service.createBucket(null);
    }

    /**
     * 上传文件
     */
    public void test3() {
        s3Service.uploadFile("1/2/3/test.jpg", "/Users/mshe/developer/1.jpg");
    }

    /**
     * 上传文件
     */
    public void test4() throws IOException {
        File file = new File("/Users/mshe/developer/1.jpg");
        RequestBody requestBody = RequestBody.fromInputStream(new FileInputStream(file), file.length());
        s3Service.uploadFile("test1.jpg", requestBody);
    }


    /**
     * 下载文件
     */
    public void test5() {
        s3Service.downloadFile("test1.jpg", "/Users/mshe/developer/2.jpg");
    }


    /**
     * 下载文件为流
     */
    public void test6() throws IOException {
        ResponseInputStream<GetObjectResponse> responseInputStream = s3Service.downloadFileAsStream("test1.jpg");
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
    public void test7() throws IOException {
        ResponseBytes<GetObjectResponse> responseBytes = s3Service.downloadFileAsByte("test1.jpg");
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
    public void test8() {
        PresignedGetObjectRequest presignedGetObjectRequest = s3Service.createTempDownloadUrl("test1.jpg", 1L, ChronoUnit.MINUTES);
        URL url = presignedGetObjectRequest.url();
        System.err.println(url);
    }


    /**
     * 创建临时上传url
     */
    public void test9() {
        PresignedPutObjectRequest tempUploadUrl = s3Service.createTempUploadUrl("test2.jpg", 5L, ChronoUnit.MINUTES);
        URL url = tempUploadUrl.url();
        System.err.println(url);
    }


    /**
     * 分片上传
     */
    public void test10() throws IOException {
        String uploadId = s3Service.createMultipartUploadId("etcd.tar");
        File file = new File("/Users/mshe/developer/etcd.tar");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] b = new byte[1024 * 1024 * 5];
            for (int i = fileInputStream.read(b); i != -1; i = fileInputStream.read(b)) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(b, 0, i);
                RequestBody requestBody = RequestBody.fromRemainingByteBuffer(byteBuffer);
                s3Service.multipartUploadParts("etcd.tar", uploadId, requestBody);
            }
            s3Service.completedMultipartUpload("etcd.tar", uploadId);
        }
    }

}
