package com.zee.courseauthdemo.config.oauth2errorhandler;


import com.zee.courseauthdemo.CourseAuthDemoApplication;
import com.zee.courseauthdemo.datatype.MessageType;
import com.zee.courseauthdemo.dto.ApiResponse;
import com.zee.courseauthdemo.exception.ErrorInfo;
import com.zee.courseauthdemo.util.AuthUtil;
import com.zee.courseauthdemo.util.MessageSourceUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 * implemented from {@link OAuth2ErrorHttpMessageConverter}
 */

public class CustomOAuth2ErrorHttpMessageConverter extends AbstractHttpMessageConverter<CustomOAuth2Error> {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {};

    private final GenericHttpMessageConverter<Object> jsonMessageConverter = new CustomGenericHttpMessageConverterAdapter<>(new JacksonJsonHttpMessageConverter());


    public CustomOAuth2ErrorHttpMessageConverter() {
        super(DEFAULT_CHARSET, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    @Override
    protected boolean supports(@NotNull Class<?> clazz) {
        return ApiResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected @NotNull CustomOAuth2Error readInternal(@NotNull Class<? extends CustomOAuth2Error> clazz, @NotNull HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(@NotNull CustomOAuth2Error customOAuth2Error, @NotNull HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
        try {
            //removed inner class
            MessageSourceUtil messageSourceUtil = CourseAuthDemoApplication.getContext().getBean("messageSourceUtil", MessageSourceUtil.class);
            ErrorInfo errorInfo = new ErrorInfo(MessageType.ERROR, customOAuth2Error.getErrorCode(), customOAuth2Error.getDescription(), null);
            ApiResponse<?> errorParameters = AuthUtil.parseExceptionInfoToResponseDto(messageSourceUtil, errorInfo);

            this.jsonMessageConverter.write(errorParameters, STRING_OBJECT_MAP.getType(), MediaType.APPLICATION_JSON,
                    outputMessage);
        }
        catch (Exception ex) {
            throw new HttpMessageNotWritableException(
                    "An error occurred writing the OAuth 2.0 Error: " + ex.getMessage(), ex);
        }
    }



}
