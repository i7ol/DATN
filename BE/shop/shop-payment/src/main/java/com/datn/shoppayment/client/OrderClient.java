package com.datn.shoppayment.client;

import com.datn.shoppayment.dto.request.UpdateOrderPaymentRequest;
import com.datn.shoppayment.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class OrderClient {

    private final RestTemplate restTemplate;

    public OrderResponse getOrder(Long orderId) {
        String url = "http://localhost:8080/api/orders/" + orderId;
        return restTemplate.getForObject(url, OrderResponse.class);
    }

    public void updateOrderPayment(Long orderId, String paymentStatus) {
        String url = "http://localhost:8080/api/orders/" + orderId + "/payment";
        restTemplate.put(url, new UpdateOrderPaymentRequest(orderId,paymentStatus));
    }

}
