package com.zee.courseauthdemo.dto;


/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 */

public record AuthFieldError(
        String errorCode,
        String field,
        String message,
        Object rejectedInput
) { }
