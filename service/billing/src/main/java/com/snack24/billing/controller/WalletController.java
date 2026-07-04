package com.snack24.billing.controller;

import com.snack24.billing.service.WalletService;
import com.snack24.billing.service.request.WalletChargeRequest;
import com.snack24.billing.service.response.WalletChargeResponse;
import com.snack24.billing.web.Caller;
import com.snack24.billing.web.CallerContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/wallets")
@Slf4j
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/charge")
    public WalletChargeResponse charge(
            @Caller CallerContext caller,
            @RequestBody @Valid WalletChargeRequest request
            ) {
        log.info("billing service modify test");
        log.info("billing service modify test");
        return walletService.charge(caller.companyId(), request.amount(), request.idempotencyKey());
    }

    @GetMapping
    public WalletChargeResponse wallet(
            @Caller CallerContext caller
    ) {
        return walletService.get(caller.companyId());
    }


}
