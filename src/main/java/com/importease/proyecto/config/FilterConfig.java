package com.importease.proyecto.config;

import com.importease.proyecto.service.AuthFilter;
import com.importease.proyecto.service.CsrfFilter;
import com.importease.proyecto.service.SecurityHeadersFilter;
import com.importease.proyecto.service.SessionFilter;
import com.importease.proyecto.service.XssFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CacheControlFilter> cacheControlFilterRegistration() {
        FilterRegistrationBean<CacheControlFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CacheControlFilter());
        registration.addUrlPatterns("/*");
        registration.setName("CacheControlFilter");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setName("SecurityHeadersFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("AuthFilter");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilterRegistration() {
        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CsrfFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("CsrfFilter");
        registration.setOrder(3);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<SessionFilter> sessionFilterRegistration() {
        FilterRegistrationBean<SessionFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SessionFilter());
        registration.addUrlPatterns("*.jsp");
        registration.setName("SessionFilter");
        registration.setOrder(4);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("XssFilter");
        registration.setOrder(5);
        return registration;
    }
}
