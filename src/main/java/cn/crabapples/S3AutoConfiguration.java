package cn.crabapples;

import cn.crabapples.s3.config.S3ConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration //开启配置
@ConditionalOnClass(S3ConfigProperties.class)
@EnableConfigurationProperties(S3ConfigProperties.class) //开启使用映射实体对象
@ConditionalOnProperty(//存在对应配置信息时初始化该配置类
        prefix = "crabapples.upload.s3",//存在配置前缀
        value = "enabled",//  检查 crabapples.upload.s3.enabled 属性
        havingValue = "true",// 仅当属性值为 "true" 时条件成立
        matchIfMissing = true// 如果配置属性不存在，则默认条件成立（即默认启用）
)
public class S3AutoConfiguration {
//    @Bean
//    @ConditionalOnMissingBean
//    public S3ConfigProperties configProperties(S3ConfigProperties properties) {
//        System.err.println("开始配置s3");
//        return properties;
//    }
}
