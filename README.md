> # s3-spring-boot-starter(JDK 1.8)

### 快速配置S3模块

- 引入maven坐标
  ```xml
  <dependency>
    <groupId>cn.crabapples</groupId>
    <artifactId>s3-spring-boot-starter</artifactId>
    <version>1.0.0</version>
  </dependency>
  ```
- 填写相关配置(application.properties)
  ```properties
  crabapples.oss.url
  crabapples.oss.accessKey
  crabapples.oss.secretKey
  crabapples.oss.bucketName
  crabapples.oss.region
  ```
