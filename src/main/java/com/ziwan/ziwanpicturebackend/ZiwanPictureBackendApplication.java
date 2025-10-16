package com.ziwan.ziwanpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.ziwan.ziwanpicturebackend.mapper")
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
public class ZiwanPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZiwanPictureBackendApplication.class, args);
    }

}
