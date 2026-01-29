package com.datn.shopclient.client;

import com.datn.shopdatabase.entity.OrderEntity;
import com.datn.shopdatabase.exception.AppException;
import com.datn.shopdatabase.exception.ErrorCode;
import com.datn.shopobject.dto.request.CheckoutRequest;
import com.datn.shopobject.dto.response.OrderResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderUserClientFallbackFactory
        implements FallbackFactory<OrderUserClient> {

    @Override
    public OrderUserClient create(Throwable cause) {
        log.error("OrderUserClient fallback triggered", cause);

        return new OrderUserClient() {

            @Override
            public OrderResponse checkout(CheckoutRequest request) {
                throw new AppException(
                        ErrorCode.SERVICE_UNAVAILABLE,
                        "Order service hiện không khả dụng"
                );
            }

            @Override
            public OrderResponse getOrder(Long orderId) {
                throw new AppException(
                        ErrorCode.SERVICE_UNAVAILABLE,
                        "Không thể lấy đơn hàng"
                );
            }

            @Override
            public Page<OrderResponse> myOrders(Long userId, Pageable pageable) {
                throw new AppException(
                        ErrorCode.SERVICE_UNAVAILABLE,
                        "Không thể lấy danh sách đơn hàng"
                );
            }
        };
    }
}


