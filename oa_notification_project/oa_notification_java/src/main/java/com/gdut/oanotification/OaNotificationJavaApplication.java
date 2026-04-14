package com.gdut.oanotification;

import com.gdut.oanotification.config.OaProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.gdut.oanotification.mapper")
@SpringBootApplication
@EnableConfigurationProperties(OaProperties.class)
@EnableAsync
@EnableScheduling
public class OaNotificationJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaNotificationJavaApplication.class, args);
    }
}
