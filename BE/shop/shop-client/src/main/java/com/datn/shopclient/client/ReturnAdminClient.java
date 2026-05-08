package com.datn.shopclient.client;

import com.datn.shopclient.config.FeignClientUserConfig;
import com.datn.shopobject.dto.response.ReturnResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(
        name = "return-admin",
        url = "${order.service.url}",
        fallbackFactory = ReturnAdminClientFallbackFactory.class,
        configuration = FeignClientUserConfig.class
        // Admin thường không cần fallbackFactory, hoặc có thể thêm nếu muốn
)
public interface ReturnAdminClient {

    @GetMapping(value = "/api/admin/returns", produces = MediaType.APPLICATION_JSON_VALUE)
    Page<ReturnResponse> getAllReturns(Pageable pageable);

    @GetMapping(value = "/api/admin/returns/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    Page<ReturnResponse> getPendingReturns(Pageable pageable);

    @GetMapping(value = "/api/admin/returns/{returnId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ReturnResponse getReturnById(@PathVariable("returnId") Long returnId);

    @PutMapping(value = "/api/admin/returns/{returnId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    ReturnResponse approveReturn(
            @PathVariable("returnId") Long returnId,
            @RequestParam(required = false) String adminNote,
            @RequestParam BigDecimal refundAmount
    );

    @PutMapping(value = "/api/admin/returns/{returnId}/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    ReturnResponse rejectReturn(
            @PathVariable("returnId") Long returnId,
            @RequestParam String adminNote
    );

    @PutMapping(value = "/api/admin/returns/{returnId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    ReturnResponse completeReturn(
            @PathVariable("returnId") Long returnId,
            @RequestParam(required = false) String refundTransactionId
    );
}