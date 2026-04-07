package com.datn.shopobject.dto.request;


import com.datn.shopdatabase.enums.OrderStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private OrderStatus status;
}
