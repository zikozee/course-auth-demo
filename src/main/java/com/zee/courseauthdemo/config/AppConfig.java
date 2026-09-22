package com.zee.courseauthdemo.config;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * @dev : Ezekiel Eromosei
 * @date : 04 Jun, 2026
 */

@Configuration
public class AppConfig {

    @Bean
    ObjectMapper customObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/message", "i18n/error-code");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Primary
    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor virtualThreadTaskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setVirtualThreads(true);
        simpleAsyncTaskExecutor.setThreadNamePrefix("VirtualThread-");
        return simpleAsyncTaskExecutor;

    }

    @Primary
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")  // Allow all origins
                        .allowedMethods(
                                HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                                HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()
                        )
                        .allowedHeaders("*")
                        .exposedHeaders("Set-Cookie")
                        .allowCredentials(false)  // Must be false when allowedOrigins is "*"
                        .maxAge(3600);  // Cache the preflight response for 1 hour
            }
        };
    }
}
