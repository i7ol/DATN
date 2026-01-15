package com.datn.shopcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.datn.shopdatabase.entity")
@ComponentScan({
        "com.datn.shopcart",
        "com.datn.shopdatabase"
})
@EnableJpaRepositories("com.datn.shopdatabase.repository")
@EnableFeignClients(basePackages = {
        "com.datn.shopclient.client"
})
public class ShopCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopCartApplication.class, args);
    }

}