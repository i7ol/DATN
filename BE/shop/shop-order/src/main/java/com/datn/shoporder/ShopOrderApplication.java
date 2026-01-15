package com.datn.shoporder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.datn.shopdatabase.entity")
@ComponentScan({
        "com.datn.shoporder",
        "com.datn.shopdatabase"
})
@EnableJpaRepositories("com.datn.shopdatabase.repository")
@EnableFeignClients(basePackages = {
        "com.datn.shopclient.client",
        "com.datn.shoporder.client"
})
public class ShopOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopOrderApplication.class, args);
    }

}