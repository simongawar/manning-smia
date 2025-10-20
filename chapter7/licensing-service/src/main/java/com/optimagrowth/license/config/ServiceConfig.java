package com.optimagrowth.license.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceConfig {

    @Value("${example.property:This is the default property value}")
    private String property;

    public String getProperty() {
        return property;
    }
}