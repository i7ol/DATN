package com.datn.shoppayment.client;

import com.datn.shopobject.dto.request.UpdateOrderPaymentRequest;
import com.datn.shopobject.dto.request.UpdateOrderStatusRequest;
import com.datn.shopobject.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OrderClient {

    private final RestTemplate restTemplate;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    // INTERNAL - ADMIN API
    public OrderResponse getOrder(Long orderId) {
        return restTemplate.getForObject(
                orderServiceUrl + "/api/admin/orders/" + orderId,
                OrderResponse.class
        );
    }

    public void updateOrderPayment(Long orderId, String paymentStatus) {
        restTemplate.put(
                orderServiceUrl + "/api/admin/orders/" + orderId + "/payment",
                new UpdateOrderPaymentRequest(orderId, paymentStatus)
        );
    }

    // Thêm phương thức này nếu order service có endpoint /status
    public void updateOrderStatus(Long orderId, String orderStatus) {
        restTemplate.put(
                orderServiceUrl + "/api/admin/orders/" + orderId + "/status",
                new UpdateOrderStatusRequest(orderId, orderStatus)
        );
    }
}