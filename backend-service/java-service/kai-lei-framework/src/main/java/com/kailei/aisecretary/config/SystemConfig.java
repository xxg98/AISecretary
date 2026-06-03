package com.kailei.aisecretary.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix="system")
public class SystemConfig {
    private String name;
    private String version;
    private String auther;
}
