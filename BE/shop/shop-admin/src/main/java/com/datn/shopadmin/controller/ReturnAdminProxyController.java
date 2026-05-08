package com.datn.shopadmin.controller;

import com.datn.shopclient.client.ReturnAdminClient;
import com.datn.shopobject.dto.response.PageResponse;
import com.datn.shopobject.dto.response.ReturnResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
public class ReturnAdminProxyController {

    private final ReturnAdminClient returnAdminClient;

    /**
     * Lấy tất cả yêu cầu đổi trả / bảo hành
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ReturnResponse> getAllReturns(Pageable pageable) {

        Page<ReturnResponse> page = returnAdminClient.getAllReturns(pageable);

        return PageResponse.<ReturnResponse>builder()
                .data(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Lấy danh sách yêu cầu đang chờ duyệt (Pending)
     */
    @GetMapping(value = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResponse<ReturnResponse> getPendingReturns(Pageable pageable) {

        Page<ReturnResponse> page = returnAdminClient.getPendingReturns(pageable);

        return PageResponse.<ReturnResponse>builder()
                .data(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Lấy chi tiết một yêu cầu đổi trả
     */
    @GetMapping(value = "/{returnId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse getReturnDetail(@PathVariable Long returnId) {
        return returnAdminClient.getReturnById(returnId);
    }

    /**
     * Admin duyệt yêu cầu đổi trả
     */
    @PutMapping(value = "/{returnId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse approveReturn(
            @PathVariable Long returnId,
            @RequestParam(required = false) String adminNote,
            @RequestParam BigDecimal refundAmount) {

        return returnAdminClient.approveReturn(returnId, adminNote, refundAmount);
    }

    /**
     * Admin từ chối yêu cầu
     */
    @PutMapping(value = "/{returnId}/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse rejectReturn(
            @PathVariable Long returnId,
            @RequestParam String adminNote) {

        return returnAdminClient.rejectReturn(returnId, adminNote);
    }

    /**
     * Admin hoàn tất xử lý (đã hoàn tiền / xử lý xong)
     */
    @PutMapping(value = "/{returnId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse completeReturn(
            @PathVariable Long returnId,
            @RequestParam(required = false) String refundTransactionId) {

        return returnAdminClient.completeReturn(returnId, refundTransactionId);
    }
}