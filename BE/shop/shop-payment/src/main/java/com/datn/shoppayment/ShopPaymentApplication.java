//package com.datn.shoppayment;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//
//@SpringBootApplication
//@EnableJpaRepositories(
//        basePackages = "com.datn.shopdatabase.repository",
//        entityManagerFactoryRef = "entityManagerFactory",
//        transactionManagerRef = "transactionManager"
//)
//@EntityScan(basePackages = "com.datn.shopdatabase.entity")
//@ComponentScan(basePackages = {
//        "com.datn.shoppayment",
//        "com.datn.shopdatabase",
//        "com.datn.shopobject"
//})
//@EnableFeignClients(basePackages = {
//        "com.datn.shopobject.client",
//        "com.datn.shoppayment.client"
//})
//public class ShopPaymentApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(ShopPaymentApplication.class, args);
//    }
//
//}