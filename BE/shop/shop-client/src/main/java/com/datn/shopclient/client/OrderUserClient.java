    package com.datn.shopclient.client;


    import com.datn.shopclient.config.FeignClientUserConfig;
    import com.datn.shopobject.dto.request.CheckoutRequest;
    import com.datn.shopobject.dto.response.OrderResponse;
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.data.domain.Page;

    import org.springframework.web.bind.annotation.*;

    @FeignClient(
            name = "order-service",
            url = "${order.service.url}",
            fallbackFactory = OrderUserClientFallbackFactory.class,
            configuration = FeignClientUserConfig.class
    )
    public interface OrderUserClient {

        @PostMapping("/api/user/orders/checkout")
        OrderResponse checkout(@RequestBody CheckoutRequest request);

        @GetMapping("/api/user/orders/{orderId}")
        OrderResponse getOrder(@PathVariable("orderId") Long orderId);


        @GetMapping("/api/user/orders/my-orders")
        Page<OrderResponse> myOrders(
                @RequestParam(value = "page", defaultValue = "0") int page,
                @RequestParam(value = "size", defaultValue = "10") int size
        );
    }

