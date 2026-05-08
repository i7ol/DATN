package com.datn.shopclient.client;

import com.datn.shopobject.dto.request.CreateReturnRequest;
import com.datn.shopobject.dto.response.ReturnResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReturnUserClientFallbackFactory implements FallbackFactory<ReturnUserClient> {

    @Override
    public ReturnUserClient create(Throwable cause) {
        log.error("Return Service is down or unreachable", cause);

        return new ReturnUserClient() {

            @Override
            public ReturnResponse createReturn(CreateReturnRequest request) {
                log.warn("Fallback: createReturn request failed for orderId: {}",
                        request != null ? request.getOrderId() : null);
                throw new RuntimeException("Dịch vụ đổi trả tạm thời không khả dụng. Vui lòng thử lại sau ít phút.");
            }

            @Override
            public Page<ReturnResponse> getMyReturns(int page, int size) {
                log.warn("Fallback: getMyReturns failed");
                throw new RuntimeException("Không thể tải danh sách yêu cầu đổi trả lúc này.");
            }

            @Override
            public ReturnResponse getReturnById(Long returnId) {
                log.warn("Fallback: getReturnById failed for id: {}", returnId);
                throw new RuntimeException("Không thể lấy chi tiết yêu cầu đổi trả.");
            }
        };
    }
}