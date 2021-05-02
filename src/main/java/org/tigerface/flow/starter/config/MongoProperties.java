package org.tigerface.flow.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tigerface.flow.component.mongodb")
@Data
public class MongoProperties {
    private String host;
    private String dbname;
}
