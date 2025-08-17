package com.yadong.yuchuang;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching  // 启动缓存功能
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.yadong.yuchuang.mapper") // 扫描mapper接口
public class YuchuangApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuchuangApplication.class, args);
    }

}
