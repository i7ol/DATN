package com.datn.shopadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.datn.shopadmin",

        // service modules
        "com.datn.shopuser",
        "com.datn.shopproduct",
        "com.datn.shopshipping",

        // shared
        "com.datn.shopdatabase",
        "com.datn.shopobject"
})
@EnableFeignClients(basePackages = {
        "com.datn.shopadmin.client",
        "com.datn.shopclient.client",
})
@EntityScan("com.datn.shopdatabase.entity")
@EnableJpaRepositories("com.datn.shopdatabase.repository")
public class ShopAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopAdminApplication.class, args);
    }
}

