
package com.datn.shopobject.dto.response;


import com.datn.shopobject.dto.ImageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long cartId;
    private UserResponse user;
    private String guestId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;
}

