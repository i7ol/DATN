package com.datn.shopshipping.service;

import com.datn.shopobject.dto.response.ShippingCalculateResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingProviderService {

    private final RestTemplate restTemplate;

    public ShippingCalculateResponse calculateFee(String address, Double weight,
                                                  String shippingMethod, String company) {
        // Gọi API của các hãng vận chuyển (GHN, GHTK, ViettelPost, ...)
        return ShippingCalculateResponse.builder()
                .company(company)
                .service(shippingMethod)
                .fee(calculateMockFee(address, weight, shippingMethod))
                .estimatedDays(estimateDeliveryDays(address, shippingMethod))
                .build();
    }

    public TrackingInfo getTrackingInfo(String trackingNumber, String company) {
        // Gọi API tracking của hãng vận chuyển
        // Đây là mock implementation - thực tế sẽ gọi API thật
        return TrackingInfo.builder()
                .trackingNumber(trackingNumber)
                .status(getMockStatus())
                .currentLocation("Hà Nội")
                .estimatedDeliveryDate(java.time.LocalDate.now().plusDays(2))
                .build();
    }

    private String getMockStatus() {
        // Mock các trạng thái từ nhà vận chuyển
        String[] statuses = {
                "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY",
                "DELIVERED", "CANCELLED", "RETURNED"
        };
        return statuses[(int) (Math.random() * statuses.length)];
    }

    private Double calculateMockFee(String address, Double weight, String method) {
        double baseFee = 20000.0;
        if ("EXPRESS".equals(method)) {
            baseFee *= 1.5;
        }
        if (weight > 1.0) {
            baseFee += (weight - 1) * 5000;
        }
        return baseFee;
    }

    private Integer estimateDeliveryDays(String address, String method) {
        if ("EXPRESS".equals(method)) {
            return 1;
        } else if ("STANDARD".equals(method)) {
            return 3;
        } else {
            return 5;
        }
    }

    @Data
    @Builder
    public static class TrackingInfo {
        private String trackingNumber;
        private String status;
        private String currentLocation;
        private java.time.LocalDate estimatedDeliveryDate;
    }
}