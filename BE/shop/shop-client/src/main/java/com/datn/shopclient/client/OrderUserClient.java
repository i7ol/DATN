    package com.datn.shopclient.client;


    import com.datn.shopclient.config.FeignClientUserConfig;
    import com.datn.shopobject.dto.request.CheckoutRequest;
    import com.datn.shopobject.dto.response.OrderResponse;
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.data.domain.Page;

    import org.springframework.http.MediaType;
    import org.springframework.web.bind.annotation.*;

    @FeignClient(
            name = "order-service",
            url = "${order.service.url}",
            fallbackFactory = OrderUserClientFallbackFactory.class,
            configuration = FeignClientUserConfig.class
    )
    public interface OrderUserClient {

        @PostMapping(value = "/api/user/orders/checkout",produces = MediaType.APPLICATION_JSON_VALUE)
        OrderResponse checkout(@RequestBody CheckoutRequest request);

        @GetMapping(value = "/api/user/orders/{orderId}",produces = MediaType.APPLICATION_JSON_VALUE)
        OrderResponse getOrder(@PathVariable("orderId") Long orderId);


        @GetMapping(value = "/api/user/orders/my-orders",produces = MediaType.APPLICATION_JSON_VALUE)
        Page<OrderResponse> myOrders(
                @RequestParam(value = "page", defaultValue = "0") int page,
                @RequestParam(value = "size", defaultValue = "10") int size
        );
    }

