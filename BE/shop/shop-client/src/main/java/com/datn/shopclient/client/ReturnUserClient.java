package com.datn.shopclient.client;

import com.datn.shopclient.config.FeignClientUserConfig;
import com.datn.shopobject.dto.request.CreateReturnRequest;
import com.datn.shopobject.dto.response.ReturnResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "return-user",
        url = "${order.service.url}",
        fallbackFactory = ReturnUserClientFallbackFactory.class,
        configuration = FeignClientUserConfig.class
)
public interface ReturnUserClient {

    /**
     * Tạo yêu cầu đổi trả / bảo hành
     * Hỗ trợ cả User đã login và Guest (khách vãng lai)
     */
    @PostMapping(value = "/api/returns", produces = MediaType.APPLICATION_JSON_VALUE)
    ReturnResponse createReturn(@RequestBody CreateReturnRequest request);

    /**
     * Lấy danh sách yêu cầu đổi trả của tôi (chỉ dành cho User đã login)
     */
    @GetMapping(value = "/api/returns/my-returns", produces = MediaType.APPLICATION_JSON_VALUE)
    Page<ReturnResponse> getMyReturns(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    /**
     * Lấy chi tiết một yêu cầu đổi trả
     */
    @GetMapping(value = "/api/returns/{returnId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ReturnResponse getReturnById(@PathVariable("returnId") Long returnId);
}