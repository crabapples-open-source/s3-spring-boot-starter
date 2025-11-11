package cn.crabapples.minio;

import cn.crabapples.minio.config.MinioConfigProperties;
import cn.crabapples.minio.service.impl.MinioServiceImpl;
import io.minio.GetObjectResponse;
import io.minio.messages.Bucket;
import org.bouncycastle.util.encoders.Hex;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.utils.Md5Utils;

import java.io.*;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MinioExample {
    private static final String ACCESS_KEY = "gQ6MwJZxCoPX4qoh2O80";
    private static final String SECRET_KEY = "vLmlVPM1mG2cBjBu8mLXhNBpdGkRbgpBJBT9S5yz";
    private static final String URL = "http://192.168.31.60:9000";
    private static final String BUCKET_NAME = "test-bucket";
    private static final MinioServiceImpl service;

    static {
        MinioConfigProperties properties = new MinioConfigProperties();
        properties.setAccessKey(ACCESS_KEY);
        properties.setSecretKey(SECRET_KEY);
        properties.setUrl(URL);
        properties.setBucketName(BUCKET_NAME);
        service = new MinioServiceImpl(properties);
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
        service.uploadFile("test1.jpg", new FileInputStream(file));
    }


    /**
     * 下载文件
     */
    public void downloadFile() {
        GetObjectResponse getObjectResponse = service.downloadFile("test1.jpg", "/Users/mshe/developer/2.jpg");
        System.err.println(getObjectResponse);
    }


    /**
     * 下载文件为流
     */
    public void downloadAsStream() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/mshe/developer/6.jpg");
        service.downloadAsStream("38.jpeg", fileOutputStream);
    }

    /**
     * 创建临时下载url
     */
    public void tempDownloadUrl() {
        String tempDownloadUrl = service.createTempDownloadUrl("38.jpeg", 1, TimeUnit.MINUTES);
        System.err.println(tempDownloadUrl);
    }

    /**
     * 分片上传
     */
    public void multipartUpload() throws IOException {
        File file = new File("/Users/mshe/developer/etcd.tar");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] bytes = Md5Utils.computeMD5Hash(file);
            String uploadId = Hex.toHexString(bytes);
            int index = 0;
            byte[] b = new byte[1024 * 1024 * 5];
            for (int i = fileInputStream.read(b); i != -1; i = fileInputStream.read(b)) {
                byte[] data = Arrays.copyOf(b, i);
                service.multipartUpload(data, uploadId, index);
                index++;
            }
            service.mergeMultipart("etcd1.tar", uploadId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        MinioExample example = new MinioExample();
//        example.downloadAsStream();
//        example.tempDownloadUrl();
        example.multipartUpload();
    }
}
