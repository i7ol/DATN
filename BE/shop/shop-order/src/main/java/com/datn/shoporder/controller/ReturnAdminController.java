package com.datn.shoporder.controller;

import com.datn.shopdatabase.entity.OrderReturnEntity;
import com.datn.shopobject.dto.response.ReturnResponse;
import com.datn.shoporder.mapper.ReturnMapper;
import com.datn.shoporder.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
public class ReturnAdminController {

    private final ReturnService returnService;
    // Không cần inject ReturnMapper vì đang dùng static method

    // Danh sách tất cả yêu cầu (có phân trang)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ReturnResponse> getAllReturns(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<OrderReturnEntity> returns = returnService.getAllReturns(pageable);

        return returns.map(ReturnMapper::toResponse);   // ← Sửa đúng
    }

    // Lấy danh sách đang chờ duyệt
    @GetMapping(value = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<ReturnResponse> getPendingReturns(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<OrderReturnEntity> returns = returnService.getPendingReturns(pageable);

        return returns.map(ReturnMapper::toResponse);   // ← Sửa đúng
    }

    // Duyệt yêu cầu
    @PutMapping(value = "/{returnId}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse approveReturn(
            @PathVariable Long returnId,
            @RequestParam(required = false) String adminNote,
            @RequestParam BigDecimal refundAmount) {

        OrderReturnEntity entity = returnService.approveReturn(returnId, adminNote, refundAmount);
        return ReturnMapper.toResponse(entity);   // ← Sửa thành static
    }

    // Từ chối yêu cầu
    @PutMapping(value = "/{returnId}/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse rejectReturn(
            @PathVariable Long returnId,
            @RequestParam String adminNote) {

        OrderReturnEntity entity = returnService.rejectReturn(returnId, adminNote);
        return ReturnMapper.toResponse(entity);   // ← Sửa thành static
    }

    // Hoàn thành (đã xử lý xong, hoàn tiền)
    @PutMapping(value = "/{returnId}/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse completeReturn(
            @PathVariable Long returnId,
            @RequestParam(required = false) String refundTransactionId) {

        OrderReturnEntity entity = returnService.completeReturn(returnId, refundTransactionId);
        return ReturnMapper.toResponse(entity);   // ← Sửa thành static
    }

    // Chi tiết yêu cầu (Admin)
    @GetMapping(value = "/{returnId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ReturnResponse getReturnDetail(@PathVariable Long returnId) {
        OrderReturnEntity entity = returnService.getReturnById(returnId);
        return ReturnMapper.toResponse(entity);   // ← Sửa thành static
    }
}