package cn.crabapples.s3.service;

import cn.crabapples.s3.config.S3ConfigProperties;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.temporal.TemporalUnit;
import java.util.List;

public interface S3Service {
    S3Client getClient();

    S3ConfigProperties getConfig();

    /**
     * 初始化S3服务
     */
    void init();

    CreateBucketResponse createBucket();

    /**
     * 创建Bucket
     *
     * @param bucketName Bucket名称
     * @return 创建的Bucket信息
     */
    CreateBucketResponse createBucket(String bucketName);

    /**
     * 列出所有Bucket
     *
     * @return Bucket列表
     */
    List<Bucket> listBuckets();


    /**
     * 删除Bucket
     *
     * @param bucketName Bucket名称
     * @return 删除Bucket的响应结果
     */
    DeleteBucketResponse deleteBucket(String bucketName);


    /**
     * 上传文件
     *
     * @param fileName    文件名称
     * @param requestBody 文件内容
     * @return 上传文件的响应结果
     */
    PutObjectResponse uploadFile(String fileName, RequestBody requestBody);

    /**
     * 上传文件
     *
     * @param bucket      Bucket名称
     * @param fileName    文件名称
     * @param requestBody 文件内容
     * @return 上传文件的响应结果
     */
    PutObjectResponse uploadFile(String bucket, String fileName, RequestBody requestBody);

    /**
     * 上传文件
     *
     * @param fileName 文件名称
     * @param path     文件路径
     * @return 上传文件的响应结果
     */
    PutObjectResponse uploadFile(String fileName, String path);

    /**
     * 上传文件
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @param path     文件路径
     * @return 上传文件的响应结果
     */
    PutObjectResponse uploadFile(String bucket, String fileName, String path);

    /**
     * 列出所有文件
     *
     * @return 文件列表
     */
    List<S3Object> listFiles();

    /**
     * 列出所有文件
     *
     * @param bucket Bucket名称
     * @return 文件列表
     */
    List<S3Object> listFiles(String bucket);


    /**
     * 删除文件
     *
     * @param fileName 文件名称
     * @return 删除文件的响应结果
     */
    DeleteObjectResponse deleteFile(String fileName);

    /**
     * 删除文件
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @return 删除文件的响应结果
     */
    DeleteObjectResponse deleteFile(String bucket, String fileName);


    /**
     * 下载文件
     *
     * @param fileName 文件名称
     * @param path     文件保存路径
     * @return 下载文件的响应结果
     */
    GetObjectResponse downloadFile(String fileName, String path);

    /**
     * 下载文件
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @param path     文件保存路径
     * @return 下载文件的响应结果
     */
    GetObjectResponse downloadFile(String bucket, String fileName, String path);

    /**
     * 下载文件
     *
     * @param fileName 文件名称
     * @return 响应输入流
     */
    ResponseInputStream<GetObjectResponse> downloadFileAsStream(String fileName);

    /**
     * 下载文件
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @return 响应输入流
     */
    ResponseInputStream<GetObjectResponse> downloadFileAsStream(String bucket, String fileName);

    /**
     * 下载文件
     *
     * @param fileName 文件名称
     * @return 响应字节
     */
    ResponseBytes<GetObjectResponse> downloadFileAsByte(String fileName);

    /**
     * 下载文件
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @return 响应字节
     */
    ResponseBytes<GetObjectResponse> downloadFileAsByte(String bucket, String fileName);

    /**
     * 下载文件
     *
     * @param fileName 文件名称
     * @return 响应输入流
     */
    ResponseInputStream<GetObjectTorrentResponse> downloadFileAsTorrentStream(String fileName);

    /**
     * 下载文件
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @return 响应输入流
     */
    ResponseInputStream<GetObjectTorrentResponse> downloadFileAsTorrentStream(String bucket, String fileName);

    /**
     * 生成临时下载地址
     *
     * @param fileName 文件名称
     * @param time     时间
     * @param unit     时间单位
     * @return 响应地址
     */
    PresignedGetObjectRequest createTempDownloadUrl(String fileName, Long time, TemporalUnit unit);

    /**
     * 生成临时下载地址
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @param time     时间
     * @param unit     时间单位
     * @return 响应地址
     */
    PresignedGetObjectRequest createTempDownloadUrl(String bucket, String fileName, Long time, TemporalUnit unit);

    /**
     * 创建临时上传URL
     *
     * @param fileName 文件名称
     * @return 响应地址
     **/
    PresignedPutObjectRequest createTempUploadUrl(String fileName);

    /**
     * 创建临时上传URL
     *
     * @param fileName 文件名称
     * @param time     时间
     * @param unit     时间单位
     * @return 响应地址
     **/
    PresignedPutObjectRequest createTempUploadUrl(String fileName, Long time, TemporalUnit unit);

    /**
     * 创建临时上传URL
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @return 响应地址
     **/
    PresignedPutObjectRequest createTempUploadUrl(String bucket, String fileName);

    /**
     * 创建临时上传URL
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @param time     时间
     * @param unit     时间单位
     * @return 响应地址
     **/
    PresignedPutObjectRequest createTempUploadUrl(String bucket, String fileName, Long time, TemporalUnit unit);

    /**
     * 创建分片上传ID
     *
     * @param fileName 文件名称
     * @return 分片上传ID
     */
    String createMultipartUploadId(String fileName);

    /**
     * 创建分片上传ID
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @return 分片上传ID
     */
    String createMultipartUploadId(String bucket, String fileName);

    /**
     * 上传分片
     *
     * @param fileName    文件名称
     * @param uploadId    分片上传ID
     * @param requestBody 请求体
     * @return 响应结果
     */
    List<CompletedPart> multipartUploadParts(String fileName, String uploadId, RequestBody requestBody);

    /**
     * 上传分片
     *
     * @param bucket      Bucket名称
     * @param fileName    文件名称
     * @param uploadId    分片上传ID
     * @param requestBody 请求体
     * @return 响应结果
     */
    List<CompletedPart> multipartUploadParts(String bucket, String fileName, String uploadId, RequestBody requestBody);

    /**
     * 上传分片
     *
     * @param fileName 文件名称
     * @param partPath 分片路径
     * @param uploadId 分片上传ID
     * @return 响应结果
     */
    List<CompletedPart> multipartUploadParts(String fileName, String partPath, String uploadId);

    /**
     * 上传分片
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @param partPath 分片路径
     * @param uploadId 分片上传ID
     * @return 响应结果
     */
    List<CompletedPart> multipartUploadParts(String bucket, String fileName, String partPath, String uploadId);

    /**
     * 合并分片
     *
     * @param fileName 文件名称
     * @param uploadId 分片上传ID
     * @return 响应结果
     */
    CompleteMultipartUploadResponse completedMultipartUpload(String fileName, String uploadId);

    /**
     * 合并分片
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @param uploadId 分片上传ID
     * @return 响应结果
     */
    CompleteMultipartUploadResponse completedMultipartUpload(String bucket, String fileName, String uploadId);

    /**
     * 取消分片上传
     *
     * @param fileName 文件名称
     * @param uploadId 分片上传ID
     * @return 响应结果
     */
    AbortMultipartUploadResponse abortMultipartUpload(String fileName, String uploadId);

    /**
     * 取消分片上传
     *
     * @param bucket   Bucket名称
     * @param fileName 文件名称
     * @param uploadId 分片上传ID
     * @return 响应结果
     */
    AbortMultipartUploadResponse abortMultipartUpload(String bucket, String fileName, String uploadId);
}
