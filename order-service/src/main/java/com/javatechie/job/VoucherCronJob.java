package com.javatechie.job;

import com.javatechie.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ConstantValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class VoucherCronJob {
    private final VoucherService voucherService;

    @Autowired
    public VoucherCronJob(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    /**
     * Chạy mỗi ngày lúc 00:00:00
     * Cú pháp: second minute hour day-of-month month day-of-week
     */
//    @Scheduled(cron = "0 0 0 2,4,6,8 * *", zone = ConstantValue.TIME_ZONE_ID)
    @Scheduled(cron = "0 32 14 * * *", zone = ConstantValue.TIME_ZONE_ID)
    public void dailyGrantVoucher() {
        log.info("🔔 Bắt đầu tặng voucher cho tất cả user vào {}",
                LocalDateTime.now(ZoneId.of(ConstantValue.TIME_ZONE_ID)));
        voucherService.grantVoucherToRandom();
        log.info("✅ Hoàn tất tặng voucher cho tất cả user");
    }

}

