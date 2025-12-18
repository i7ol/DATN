package com.datn.shopshipping.scheduler;

import com.datn.shopdatabase.repository.ShippingRepository;
import com.datn.shopshipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShippingSyncScheduler {

    private final ShippingRepository shippingRepository;
    private final ShippingService shippingService;

    // Chạy mỗi 30 phút để đồng bộ trạng thái
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void syncShippingStatus() {
        log.info("Starting shipping sync scheduler");

        LocalDateTime syncBefore = LocalDateTime.now().minusMinutes(30);
        var shippingsToSync = shippingRepository.findShippingsNeedSync(syncBefore);

        for (var shipping : shippingsToSync) {
            try {
                if (shipping.getTrackingNumber() != null && !shipping.getTrackingNumber().isEmpty()) {
                    shippingService.syncWithShippingProvider(shipping.getId());
                    log.info("Synced shipping: {}", shipping.getId());
                }
            } catch (Exception e) {
                log.error("Error syncing shipping {}: {}", shipping.getId(), e.getMessage());
            }
        }

        log.info("Finished shipping sync scheduler. Synced {} shipments", shippingsToSync.size());
    }
}