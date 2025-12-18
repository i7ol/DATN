// ShippingSearchRequest.java
package com.datn.shopobject.dto.request;


import com.datn.shopdatabase.enums.StatusEnum;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ShippingSearchRequest {
    private StatusEnum status;
    private String shippingCompany;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime toDate;
}