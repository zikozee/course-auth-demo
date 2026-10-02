package com.zee.courseauthdemo.testdto;

public record TokenData(long expiresIn, String accessToken, String tokenType, String refreshToken) {}
