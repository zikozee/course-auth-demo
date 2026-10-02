package com.zee.courseauthdemo.testdto;

public record TokenResponse(boolean successful, String messageType, TokenData data) {}
