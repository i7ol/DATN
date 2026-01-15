package com.datn.shoppayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.datn.shopdatabase.entity")
@EnableJpaRepositories({
        "com.datn.shoppayment.repository"
})
@ComponentScan({
        "com.datn.shoppayment",
        "com.datn.shopdatabase" // entity + base config
})
@EnableFeignClients(basePackages = {
        "com.datn.shoppayment.client",
        "com.datn.shopclient.client"

})
public class ShopPaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopPaymentApplication.class, args);
    }
}

