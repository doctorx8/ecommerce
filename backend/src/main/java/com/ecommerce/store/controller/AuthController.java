package com.ecommerce.store.controller;

import com.ecommerce.store.config.ApiException;
import com.ecommerce.store.dto.AccountDtos.ChangePasswordRequest;
import com.ecommerce.store.dto.AccountDtos.DeleteAccountRequest;
import com.ecommerce.store.dto.AccountDtos.ForgotPasswordRequest;
import com.ecommerce.store.dto.AccountDtos.ResetPasswordRequest;
import com.ecommerce.store.dto.AccountDtos.UpdateProfileRequest;
import com.ecommerce.store.dto.AuthDtos.LoginRequest;
import com.ecommerce.store.dto.AuthDtos.RegisterRequest;
import com.ecommerce.store.security.AuthUser;
import com.ecommerce.store.service.AuthService;
import com.ecommerce.store.util.SecurityUtils;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        return authService.loginCustomer(request);
    }

    @PostMapping("/admin/login")
    public Map<String, Object> adminLogin(@Valid @RequestBody LoginRequest request) {
        return authService.loginAdmin(request);
    }

    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        return authService.me(currentCustomerId());
    }

    @PutMapping("/me")
    public Map<String, Object> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(currentCustomerId(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentCustomerId(), request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        authService.deleteAccount(currentCustomerId(), request);
    }

    private Long currentCustomerId() {
        AuthUser user = SecurityUtils.currentUser()
                .filter(AuthUser::isCustomer)
                .orElseThrow(() -> new ApiException("Customer access required", HttpStatus.FORBIDDEN));
        return user.getId();
    }
}
