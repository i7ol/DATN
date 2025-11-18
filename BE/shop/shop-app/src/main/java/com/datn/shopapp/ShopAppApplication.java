package com.datn.shopapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.datn.shopapp",
		"com.datn.shopauth",
		"com.datn.shopcore",
		"com.datn.shopproduct",
		"com.datn.shopuser",
		"com.datn.shoporder",
		"com.datn.shopcart",
		"com.datn.shoppayment",
		"com.datn.shopinventory",
		"com.datn.shopshipping",
		"com.datn.shopcms",
		"com.datn.shopnotification"
})
@EntityScan(basePackages = {
		"com.datn.shopproduct.entity",
		"com.datn.shoporder.entity",
		"com.datn.shopcart.entity",
		"com.datn.shoppayment.entity",
		"com.datn.shopcore.entity",
		"com.datn.shopinventory.entity",
		"com.datn.shopshipping.entity",
		"com.datn.shopcms.entity",
		"com.datn.shopnotification.entity"
})
@EnableJpaRepositories(basePackages = {
		"com.datn.shopproduct.repository",
		"com.datn.shoporder.repository",
		"com.datn.shopcart.repository",
		"com.datn.shoppayment.repository",
		"com.datn.shopcore.repository",
		"com.datn.shopinventory.repository",
		"com.datn.shopshipping.repository",
		"com.datn.shopcms.repository",
		"com.datn.shopnotification.repository"
})
public class ShopAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopAppApplication.class, args);
	}
}
