package com.zee.courseauthdemo.config.oauth2errorhandler;


import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.SmartHttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 * implemented from {@link org.springframework.security.oauth2.core.http.converter.GenericHttpMessageConverterAdapter}
 */

// this was created for Jackson 3, mimicked same
public class CustomGenericHttpMessageConverterAdapter <T> implements GenericHttpMessageConverter<T> {

    private final SmartHttpMessageConverter<T> smartConverter;

    CustomGenericHttpMessageConverterAdapter(SmartHttpMessageConverter<T> smartConverter) {
        this.smartConverter = smartConverter;
    }

    @Override
    public boolean canRead(@NotNull Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return this.smartConverter.canRead(ResolvableType.forType(type), mediaType);
    }

    @Override
    public @NotNull T read(@NotNull Type type, @Nullable Class<?> contextClass, @NotNull HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return this.smartConverter.read(ResolvableType.forType(type), inputMessage, null);
    }

    @Override
    public boolean canWrite(@Nullable Type type, @NotNull Class<?> clazz, @Nullable MediaType mediaType) {
        return this.smartConverter.canWrite(ResolvableType.forType(type), clazz, mediaType);
    }

    @Override
    public void write(@NotNull T t, @Nullable Type type, @Nullable MediaType contentType, @NotNull HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        this.smartConverter.write(t, ResolvableType.forType(type), contentType, outputMessage, null);
    }

    @Override
    public boolean canRead(@NotNull Class<?> clazz, @Nullable MediaType mediaType) {
        return this.smartConverter.canRead(ResolvableType.forClass(clazz), mediaType);
    }

    @Override
    public boolean canWrite(@NotNull Class<?> clazz, @Nullable MediaType mediaType) {
        return this.smartConverter.canWrite(clazz, mediaType);
    }

    @Override
    public @NotNull List<MediaType> getSupportedMediaTypes() {
        return this.smartConverter.getSupportedMediaTypes();
    }

    @Override
    public @NotNull T read(@NotNull Class<? extends T> clazz, @NotNull HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return this.smartConverter.read(clazz, inputMessage);
    }

    @Override
    public void write(@NotNull T t, @Nullable MediaType contentType, @NotNull HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        this.smartConverter.write(t, contentType, outputMessage);
    }

}