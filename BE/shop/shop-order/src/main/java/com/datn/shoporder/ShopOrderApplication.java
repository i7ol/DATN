package com.datn.shoporder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {
        "com.datn.shopproduct.repository",
        "com.datn.shoporder.repository"
})
@EntityScan(basePackages = {
        "com.datn.shopproduct.entity",
        "com.datn.shoporder.entity"
})
@SpringBootApplication(scanBasePackages = {
        "com.datn.shopproduct",
        "com.datn.shoporder"
})
public class ShopOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopOrderApplication.class, args);
    }

}
