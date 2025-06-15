package com.javatechie.controller;

import com.javatechie.client.IdentityClient;
import com.javatechie.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.example.dtos.UserDto;
import org.example.dtos.VoucherDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@Slf4j
public class VoucherController {
    private final VoucherService voucherService;

    private final IdentityClient identityClient;

    @GetMapping("/my-voucher")
    public CommonResponse<List<VoucherDto>> myVoucher(@RequestHeader("Authorization") String token) {
        CommonResponse<?> authResponse = identityClient.getCurrentUser(token);
        log.info("myVoucher: {}", authResponse);
        if(authResponse.getStatusCode() != HttpStatus.OK.value() || authResponse.getData() == null) {
            log.error("invalid token???");
            return CommonResponse.unAuth();
        }
        Long userId = null;
        if(authResponse.getData() instanceof UserDto) {
            userId = (((UserDto) authResponse.getData()).getId());
        } else{
            log.info(authResponse.getData().getClass().getName());
            return CommonResponse.unAuth();
        }
        log.info("start myVoucher: {}", userId);
        if(userId == null) {
            return CommonResponse.unAuth();
        }
        return CommonResponse.ok(voucherService.getVouchersByUserId(userId));
    }
}
