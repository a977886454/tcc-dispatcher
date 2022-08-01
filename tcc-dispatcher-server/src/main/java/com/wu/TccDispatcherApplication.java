package com.wu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@SpringBootApplication
@MapperScan
public class TccDispatcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(TccDispatcherApplication.class,args);
    }
}
