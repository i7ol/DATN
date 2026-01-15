package com.datn.shopinventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EntityScan("com.datn.shopdatabase.entity")
@ComponentScan({
        "com.datn.shopinventory",
        "com.datn.shopdatabase"
})
@EnableJpaRepositories("com.datn.shopdatabase.repository")
public class ShopInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopInventoryApplication.class, args);
    }

}