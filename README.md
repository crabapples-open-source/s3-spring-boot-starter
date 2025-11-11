> # upload-spring-boot-starter(JDK 1.8)

### 快速配置文件上传模块

- 引入maven坐标
  ```xml
  <dependency>
    <groupId>cn.crabapples</groupId>
    <artifactId>upload-spring-boot-starter</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```
- 填写相关配置(s3)
  ```properties
  crabapples.upload.s3.url
  crabapples.upload.s3.accessKey
  crabapples.upload.s3.secretKey
  crabapples.upload.s3.bucketName
  crabapples.upload.s3.region
  ```
- 填写相关配置(minio)
  ```properties
  crabapples.upload.minio.url
  crabapples.upload.minio.accessKey
  crabapples.upload.minio.secretKey
  crabapples.upload.minio.bucketName
  ```
