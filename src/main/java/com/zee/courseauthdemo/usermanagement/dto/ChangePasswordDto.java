package com.zee.courseauthdemo.usermanagement.dto;


import jakarta.validation.constraints.NotBlank;

/**
 * @dev : Ezekiel Eromosei
 * @date : 28 Sep, 2026
 */

public record ChangePasswordDto(
        @NotBlank(message = "Old password cannot be empty")
        String oldPassword,
        @NotBlank(message = "New password cannot be empty")
        String newPassword) {
}
