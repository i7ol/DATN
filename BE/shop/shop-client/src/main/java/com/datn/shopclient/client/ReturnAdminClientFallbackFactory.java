package com.datn.shopclient.client;

import com.datn.shopobject.dto.response.ReturnResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class ReturnAdminClientFallbackFactory implements FallbackFactory<ReturnAdminClient> {

    @Override
    public ReturnAdminClient create(Throwable cause) {
        log.error("Return Admin Service fallback activated: {}", cause.getMessage(), cause);

        return new ReturnAdminClient() {

            @Override
            public Page<ReturnResponse> getAllReturns(Pageable pageable) {
                log.warn("Fallback: getAllReturns failed");
                throw new RuntimeException("Không thể lấy danh sách yêu cầu đổi trả lúc này.");
            }

            @Override
            public Page<ReturnResponse> getPendingReturns(Pageable pageable) {
                log.warn("Fallback: getPendingReturns failed");
                throw new RuntimeException("Không thể lấy danh sách yêu cầu đang chờ duyệt.");
            }

            @Override
            public ReturnResponse getReturnById(Long returnId) {
                log.warn("Fallback: getReturnById failed for id: {}", returnId);
                throw new RuntimeException("Không thể lấy chi tiết yêu cầu đổi trả.");
            }

            @Override
            public ReturnResponse approveReturn(Long returnId, String adminNote, BigDecimal refundAmount) {
                log.warn("Fallback: approveReturn failed for id: {}", returnId);
                throw new RuntimeException("Không thể duyệt yêu cầu đổi trả lúc này.");
            }

            @Override
            public ReturnResponse rejectReturn(Long returnId, String adminNote) {
                log.warn("Fallback: rejectReturn failed for id: {}", returnId);
                throw new RuntimeException("Không thể từ chối yêu cầu đổi trả.");
            }

            @Override
            public ReturnResponse completeReturn(Long returnId, String refundTransactionId) {
                log.warn("Fallback: completeReturn failed for id: {}", returnId);
                throw new RuntimeException("Không thể hoàn thành xử lý yêu cầu đổi trả.");
            }
        };
    }
}