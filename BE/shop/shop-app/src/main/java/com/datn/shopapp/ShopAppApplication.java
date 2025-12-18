package com.datn.shopapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.datn.shopapp",
		"com.datn.shopcart",
		"com.datn.shopproduct",
		"com.datn.shoporder",
		"com.datn.shopshipping",
		"com.datn.shoppayment",
		"com.datn.shopdatabase",
		"com.datn.shopobject",
})
@EntityScan(basePackages = "com.datn.shopdatabase.entity")
@EnableJpaRepositories(basePackages = "com.datn.shopdatabase.repository")
@EnableFeignClients(basePackages = "com.datn.shopobject.client")
public class ShopAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(ShopAppApplication.class, args);
	}
}