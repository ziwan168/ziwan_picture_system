package com.ziwan.ziwanpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.ziwan.ziwanpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class ZiwanPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZiwanPictureBackendApplication.class, args);
    }

}
