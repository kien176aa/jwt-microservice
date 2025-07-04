package com.javatechie.service;

import com.javatechie.client.IdentityClient;
import com.javatechie.entity.Voucher;
import com.javatechie.repository.VoucherRepository;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ConstantValue;
import org.example.dtos.CommonResponse;
import org.example.dtos.NotifyGrantVoucher;
import org.example.dtos.UserMessageDto;
import org.example.dtos.VoucherDto;
import org.example.exception.InvalidVoucherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class VoucherService {

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

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
            NotifyGrantVoucher notifyGrantVoucher = new NotifyGrantVoucher();
            notifyGrantVoucher.setTitle("Voucher");
            notifyGrantVoucher.setMessages(new ArrayList<>());
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
                notifyGrantVoucher.getMessages().add(
                    new UserMessageDto(voucher.getUserId(),
                        String.format("Bạn đc tặng voucher giảm %s%.0f%s cho đơn hàng trên $%.0f",
                                voucher.isPercent() ? "" : "$",
                                voucher.getDiscountAmount(),
                                voucher.isPercent() ? "%" : "",
                                voucher.getMinPurchase())
                    )
                );
            }
            kafkaTemplate.send("notification-topic", notifyGrantVoucher);
        } catch (Exception e) {
            log.error("grantVoucherToAll: {}", e.getMessage());
        }
    }

    public List<VoucherDto> getVouchersByUserId(Long userId) {
        List<Voucher> vouchers = voucherRepository.findByUserId(userId);
        return vouchers.stream().map(v -> {
           VoucherDto voucherDto = new VoucherDto();
           voucherDto.setId(v.getId());
           voucherDto.setUserId(v.getUserId());
           voucherDto.setUsed(v.isPercent());
           voucherDto.setCreatedAt(v.getCreatedAt());
           voucherDto.setExpiredAt(v.getExpiredAt());
           voucherDto.setCode(v.getCode());
           voucherDto.setMinPurchase(v.getMinPurchase());
           voucherDto.setPercent(v.isPercent());
           voucherDto.setDiscountAmount(v.getDiscountAmount());
           return voucherDto;
        }).toList();
    }

    public Voucher finbById(Long voucherId, double total) throws InvalidVoucherException {
        if(voucherId == null) {
            return null;
        }
        Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
        if(voucher == null) {
            throw new InvalidVoucherException("KO thấy!!");
        }
        if (voucher.isUsed()){
            throw new InvalidVoucherException("Dùng rồi mà!!");
        }
        if(voucher.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVoucherException("Hết hạn rồi!!");
        }
        if (voucher.getMinPurchase() > total) {
            throw new InvalidVoucherException("Mua thêm đi!!");
        }

        return voucher;
    }
}
