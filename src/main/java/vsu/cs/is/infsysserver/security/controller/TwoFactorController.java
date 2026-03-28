package vsu.cs.is.infsysserver.security.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vsu.cs.is.infsysserver.security.entity.dto.request.EnableTwoFactorRequest;
import vsu.cs.is.infsysserver.security.entity.dto.request.ResendCodeRequest;
import vsu.cs.is.infsysserver.security.entity.dto.request.VerifyCodeRequest;
import vsu.cs.is.infsysserver.security.entity.dto.request.VerifyTwoFactorRequest;
import vsu.cs.is.infsysserver.security.service.TwoFactorService;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @PostMapping("/enable")
    public ResponseEntity<?> enable(@AuthenticationPrincipal String login,
                                    @RequestBody EnableTwoFactorRequest request) {
        if (login == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return twoFactorService.startEnable(login, request.getEmail());
    }

    @PostMapping("/verify-enable")
    public ResponseEntity<?> verifyEnable(@AuthenticationPrincipal String login,
                                          @RequestBody VerifyTwoFactorRequest request) {
        if (login == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return twoFactorService.verifyEnable(login, request.getEmail(), request.getCode());
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable(@AuthenticationPrincipal String login) {
        if (login == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return twoFactorService.startDisable(login);
    }

    @PostMapping("/verify-disable")
    public ResponseEntity<?> verifyDisable(@AuthenticationPrincipal String login,
                                           @RequestBody VerifyCodeRequest request) {
        if (login == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return twoFactorService.verifyDisable(login, request.getCode());
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestBody ResendCodeRequest request) {
        return twoFactorService.resend(request.getEmail());
    }
}