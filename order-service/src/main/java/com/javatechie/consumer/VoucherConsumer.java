package com.javatechie.consumer;

import com.javatechie.entity.Voucher;
import com.javatechie.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherConsumer {

    private final VoucherRepository voucherRepository;


    @KafkaListener(topics = "user-registered", groupId = "voucher-group", containerFactory = "stringKafkaListenerContainerFactory")
    public void handleUserRegistered(String userIdStr) {
        log.info("Received userId for voucher: " + userIdStr);

        try {
            userIdStr = userIdStr.replace("\"", "").trim();
            Long userId = Long.parseLong(userIdStr); // Đảm bảo userId là Long

            Voucher voucher = new Voucher();
            voucher.setCode("WELCOME100-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            voucher.setUserId(userId);
            voucher.setDiscountAmount(100d); // Giảm 100k
            voucher.setPercent(false);
            voucher.setExpiredAt(LocalDateTime.now().plusWeeks(1));
            voucher.setCreatedAt(LocalDateTime.now());
            voucher.setMinPurchase(500d);
            voucher.setUsed(false);

            voucherRepository.save(voucher);

            log.info("Created welcome voucher for userId {}: {}", userId, voucher.getCode());
        } catch (NumberFormatException e) {
            log.error("Invalid userId format received: {}", userIdStr);
        } catch (Exception e) {
            log.error("Failed to create voucher for userId {}: {}", userIdStr, e.getMessage());
        }
    }

}
