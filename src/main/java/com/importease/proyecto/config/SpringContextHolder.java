package com.importease.proyecto.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static volatile ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static boolean isAvailable() {
        return context != null;
    }

    public static <T> T getBeanOrNull(Class<T> beanType) {
        if (context == null) {
            return null;
        }
        try {
            return context.getBean(beanType);
        } catch (BeansException ignored) {
            return null;
        }
    }
}
