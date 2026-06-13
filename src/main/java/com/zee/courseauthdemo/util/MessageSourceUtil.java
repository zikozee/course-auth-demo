package com.zee.courseauthdemo.util;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zee.courseauthdemo.dto.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @dev : Ezekiel Eromosei
 * @date : 20 Feb, 2025
 */

@Component
@RequiredArgsConstructor
public class MessageSourceUtil {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    private final MessageSource messageSource;

    public ErrorMessage getErrorCodeMessage(String errorCode, String... messageParams) {
        Locale locale = Locale.getDefault();
        String message = messageSource.getMessage(errorCode, null, locale);
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> errorCodeMap = new Gson().fromJson(message, mapType);

        if (errorCodeMap == null || errorCodeMap.isEmpty()) {
            return null;
        }
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorCode(errorCode);
        errorMessage.setMessage(message);

        String resolvedMessage = resolvePlaceholders(errorCodeMap.get("message"), locale, messageParams);
        if(null == resolvedMessage || resolvedMessage.isBlank() || messageParams == null || messageParams.length == 0) {
            errorMessage.setMessage(resolvedMessage);
        }else {
            errorMessage.setMessage(String.format(resolvedMessage, (Object[]) messageParams));
        }

        return errorMessage;
    }

    private String resolvePlaceholders(String input, Locale locale, String... messageParams) {
        if(null == input || input.isBlank()) {
            return null;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        StringBuilder resolvedValue = new StringBuilder();

        while (matcher.find()) {
            String placeholderKey = matcher.group(1);
            String placeholderValue = messageSource.getMessage(placeholderKey, messageParams, locale);
            matcher.appendReplacement(resolvedValue, Matcher.quoteReplacement(placeholderValue));
        }
        matcher.appendTail(resolvedValue);
        return resolvedValue.toString();
    }

    public String getMessage(String key, String... messageParams) {
        Locale locale = Locale.getDefault();

        String message = messageSource.getMessage(key, messageParams, locale);
        String resolvedMessage = resolvePlaceholders(message, locale, messageParams);

        // Format the message with dynamic params (if any)
        if(StringUtils.isBlank(resolvedMessage) || messageParams == null || messageParams.length == 0) {
            return resolvedMessage;
        }else {
            return String.format(resolvedMessage, (Object[]) messageParams);
        }
    }
}
