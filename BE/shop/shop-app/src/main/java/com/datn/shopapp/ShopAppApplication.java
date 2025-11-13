package com.datn.shopapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.datn")
@EnableJpaRepositories(basePackages = {
		"com.datn.shopcore.repository",
		"com.datn.shopcart.repository"  // thêm module cart
})
@EntityScan(basePackages = {
		"com.datn.shopcore.entity",
		"com.datn.shopcart.entity"      // thêm entity cart
})
public class ShopAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopAppApplication.class, args);
	}

}
