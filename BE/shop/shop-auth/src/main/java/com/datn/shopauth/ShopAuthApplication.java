package com.datn.shopauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = {
        "com.datn.shopauth",
        "com.datn.shopdatabase","com.datn.shopobject",
})
@EntityScan(basePackages = "com.datn.shopdatabase.entity")
@EnableJpaRepositories(basePackages = "com.datn.shopdatabase.repository")
@EnableFeignClients(basePackages = "com.datn.shopobject.client")
public class ShopAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopAuthApplication.class, args);
    }
}