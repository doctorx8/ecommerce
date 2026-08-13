package com.ecommerce.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AccountDtos {
    private AccountDtos() {}

    public record UpdateProfileRequest(
            @Email @NotBlank String email,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String telephone,
            Boolean newsletter
    ) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8) String newPassword
    ) {}

    public record DeleteAccountRequest(
            @NotBlank String password
    ) {}

    public record ForgotPasswordRequest(
            @Email @NotBlank String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 8) String newPassword
    ) {}

    public record AddressRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String company,
            @NotBlank String address1,
            String address2,
            @NotBlank String city,
            @NotBlank String postcode,
            @NotBlank String country,
            String zone,
            Boolean isDefault
    ) {}
}
