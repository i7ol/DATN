package com.datn.shopobject.dto.request;
import com.datn.shopdatabase.enums.ReturnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnRequest {

    private Long orderId;

    private ReturnType returnType;     // RETURN, EXCHANGE, WARRANTY

    private String reason;             // Lý do chung

    private String description;        // Mô tả chi tiết

    private List<ReturnItemRequest> items;   // Danh sách sản phẩm đổi/trả

    private List<String> imageUrls;    // Danh sách link ảnh minh chứng
    private String guestId;      // Quan trọng nhất
    private String guestPhone;   // Dùng để xác thực
    private String guestEmail;
}


