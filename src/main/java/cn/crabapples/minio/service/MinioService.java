package cn.crabapples.minio.service;

import cn.crabapples.minio.config.MinioConfigProperties;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.messages.Bucket;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface MinioService {
    MinioConfigProperties getConfig();

    MinioClient getClient();

    /**
     * 列出bucket
     *
     * @return bucket列表
     */
    List<Bucket> listBuckets();

    /**
     * 创建bucket
     */
    void createBucket();

    /**
     * 创建bucket
     *
     * @param bucket bucket
     */
    void createBucket(String bucket);

    /**
     * 删除bucket
     */
    void removeBucket();

    /**
     * 删除bucket
     *
     * @param bucket bucket
     */
    void removeBucket(String bucket);

    /**
     * 上传文件
     *
     * @param fileName 文件名
     * @param path     文件路径
     * @return 上传文件响应对象
     */
    ObjectWriteResponse uploadFile(String fileName, String path);

    /**
     * 上传文件
     *
     * @param bucket   bucket
     * @param fileName 文件名
     * @param path     文件路径
     * @return 上传文件响应对象
     */
    ObjectWriteResponse uploadFile(String bucket, String fileName, String path);

    /**
     * 上传文件
     *
     * @param fileName    文件名
     * @param inputStream 输入流
     * @return 上传文件响应对象
     */
    ObjectWriteResponse uploadFile(String fileName, InputStream inputStream);

    /**
     * 上传文件
     *
     * @param bucket      bucket
     * @param fileName    文件名
     * @param inputStream 输入流
     * @return 上传文件响应对象
     */
    ObjectWriteResponse uploadFile(String bucket, String fileName, InputStream inputStream);

    /**
     * 下载文件
     *
     * @param fileName     文件名
     * @param outputStream 输出流
     */
    void downloadAsStream(String fileName, OutputStream outputStream);

    /**
     * 下载文件
     *
     * @param bucket       bucket
     * @param fileName     文件名
     * @param outputStream 输出流
     */
    void downloadAsStream(String bucket, String fileName, OutputStream outputStream);

    /**
     * 下载文件
     *
     * @param fileName 文件名
     * @return 下载文件响应对象
     */
    GetObjectResponse downloadFile(String fileName);

    /**
     * 下载文件
     *
     * @param bucket   bucket
     * @param fileName 文件名
     * @return 下载文件响应对象
     */
    GetObjectResponse downloadFile(String bucket, String fileName);

    /**
     * 创建临时下载链接
     *
     * @param fileName 文件名
     * @return url
     */
    String createTempDownloadUrl(String fileName);

    /**
     * 创建临时下载链接
     *
     * @param bucket   bucket
     * @param fileName 文件名
     * @return url
     */
    String createTempDownloadUrl(String bucket, String fileName);

    /**
     * 创建临时下载链接
     *
     * @param fileName 文件名
     * @param time     时间
     * @param unit     时间单位
     * @return url
     */
    String createTempDownloadUrl(String fileName, int time, TimeUnit unit);

    /**
     * 创建临时下载链接
     *
     * @param bucket   bucket
     * @param fileName 文件名
     * @param time     时间
     * @param unit     时间单位
     * @return url
     */
    String createTempDownloadUrl(String bucket, String fileName, int time, TimeUnit unit);

    /**
     * 删除文件
     *
     * @param fileName 文件名
     */
    void remove(String fileName);

    /**
     * 删除文件
     *
     * @param bucket   bucket
     * @param fileName 文件名
     */
    void remove(String bucket, String fileName);

    /**
     * 分片上传
     *
     * @param data     文件数据
     * @param uploadId uploadId
     * @param index    分片索引
     */
    void multipartUpload(byte[] data, String uploadId, int index);

    /**
     * 分片上传
     *
     * @param bucket   bucket
     * @param data     文件数据
     * @param uploadId uploadId
     * @param index    分片索引
     */
    void multipartUpload(String bucket, byte[] data, String uploadId, int index);

    /**
     * 合并分片
     *
     * @param fileName 文件名
     * @param uploadId uploadId
     */
    void mergeMultipart(String fileName, String uploadId);

    /**
     * 合并分片
     *
     * @param bucket   bucket
     * @param fileName 文件名
     * @param uploadId uploadId
     */
    void mergeMultipart(String bucket, String fileName, String uploadId);
}
