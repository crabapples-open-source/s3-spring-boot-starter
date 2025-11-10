package cn.crabapples.s3;

import cn.crabapples.s3.config.OssConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration //开启配置
@ConditionalOnClass(OssConfigProperties.class)
@EnableConfigurationProperties(OssConfigProperties.class) //开启使用映射实体对象
@ConditionalOnProperty(//存在对应配置信息时初始化该配置类
        prefix = "crabapples.oss",//存在配置前缀
        value = "enabled",//  检查 crabapples.oss.enabled 属性
        havingValue = "true",// 仅当属性值为 "true" 时条件成立
        matchIfMissing = true// 如果配置属性不存在，则默认条件成立（即默认启用）
)
public class S3AutoConfiguration {
//    @Bean
//    @ConditionalOnMissingBean
//    public OssConfigProperties configProperties(OssConfigProperties configProperties) {
//        System.err.println("开始配置s3");
//        return configProperties;
//    }

}
