package com.zee.courseauthdemo;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringBootApplication
public class CourseAuthDemoApplication implements ApplicationContextAware {

    @Getter
    private static ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(CourseAuthDemoApplication.class, args);
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
