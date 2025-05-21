package org.interview.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "robokassa")
public class RobokassaConfig {
    private String merchantLogin;
    private String password1;
    private String password2;
}