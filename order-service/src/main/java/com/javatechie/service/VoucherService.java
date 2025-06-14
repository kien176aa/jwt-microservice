package com.javatechie.service;

import com.javatechie.client.IdentityClient;
import com.javatechie.entity.Voucher;
import com.javatechie.repository.VoucherRepository;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ConstantValue;
import org.example.dtos.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class VoucherService {

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private VoucherRepository voucherRepository;

    private final Random random = new SecureRandom();

    private static final String[] CODES = {
            "FLASHSALE", "LUCKYME", "SUPERDEAL", "HAPPYDAY", "WOWVOUCHER",
            "LUCKYYOU", "HOTSALE", "SAVEBIG", "TRYLUCK", "BESTOFFER"
    };

    public void grantVoucherToRandom() {
        try{
            CommonResponse<List<Long>> response = identityClient.getRandomUsers();
            if(response.getStatusCode() != HttpStatus.OK.value() || response.getData() == null) {
                log.error("grantVoucherToRandom - ERR when getRandomUsers");
                return;
            }
            List<Long> userIds = response.getData();
            LocalDateTime now = LocalDateTime.now(ZoneId.of(ConstantValue.TIME_ZONE_ID));
            LocalDateTime startOfNextDay = LocalDate.now(ZoneId.of(ConstantValue.TIME_ZONE_ID))
                    .plusDays(1)
                    .atStartOfDay();
            for (Long userId : userIds) {
                Voucher voucher = new Voucher();
                voucher.setUserId(userId);
                voucher.setUsed(false);
                voucher.setCreatedAt(now);
                voucher.setExpiredAt(startOfNextDay);

                String code = CODES[random.nextInt(CODES.length)];
                voucher.setCode(code);

                double minPurchase = 500 + random.nextDouble() * 200;
                voucher.setMinPurchase((double) Math.round(minPurchase));

                boolean isPercent = random.nextBoolean();
                voucher.setPercent(isPercent);

                double discountAmount;
                if (isPercent) {
                    discountAmount = 5 + random.nextDouble() * 35;
                } else {
                    discountAmount = 70 + random.nextDouble() * 180;
                }
                voucher.setDiscountAmount((double) Math.round(discountAmount));

                voucherRepository.save(voucher);
            }
        } catch (Exception e) {
            log.error("grantVoucherToAll: {}", e.getMessage());
        }
    }
}
