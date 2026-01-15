    package com.datn.shopclient.client;

    import com.datn.shopdatabase.entity.OrderEntity;
    import com.datn.shopobject.dto.request.CheckoutItemRequest;
    import com.datn.shopobject.dto.request.CheckoutRequest;
    import com.datn.shopobject.dto.response.OrderResponse;
    import jakarta.validation.Valid;
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @FeignClient(
            name = "order-service",
            url = "${order.service.url}",
            fallbackFactory = OrderUserClientFallbackFactory.class
    )
    public interface OrderUserClient {

        @PostMapping("/api/user/orders/checkout")
        OrderResponse checkout(@RequestBody CheckoutRequest request);

        @GetMapping("/api/user/orders/{orderId}")
        OrderResponse getOrder(@PathVariable("orderId") Long orderId);

        @GetMapping("/api/user/orders/my")
        List<OrderResponse> myOrders(@RequestParam("userId") Long userId);
    }

