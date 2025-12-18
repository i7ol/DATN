package com.datn.shopadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.datn.shopadmin",
                "com.datn.shopdatabase",
                "com.datn.shopuser",
                "com.datn.shopobject",
                "com.datn.shopproduct",
                "com.datn.shopinventory",
                "com.datn.shopshipping",
                "com.datn.shoppayment",
                "com.datn.shoporder",
        }
)
@EntityScan(basePackages = "com.datn.shopdatabase.entity")
@EnableJpaRepositories(basePackages = "com.datn.shopdatabase.repository")
@EnableFeignClients(basePackages = "com.datn.shopobject.client")
public class ShopAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopAdminApplication.class, args);
    }
}
