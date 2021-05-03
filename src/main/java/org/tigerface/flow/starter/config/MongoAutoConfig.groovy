package org.tigerface.flow.starter.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy;

@Configuration
//@ConditionalOnClass(MongoClient.class)
//@ConditionalOnMissingBean(type = "org.springframework.data.mongodb.MongoDbFactory")
public class MongoAutoConfig {
    @Value('${spring.data.mongodb.host:localhost}')
    private String host;

    @Value('${spring.data.mongodb.port:27017}')
    private int port;

    @Value('${spring.data.mongodb.username:root}')
    private String username;

    @Value('${spring.data.mongodb.password:}')
    private String password;

    @Bean
    public MongoClient flowDB() {
        return MongoClients.create("mongodb://${username}:${password}@${host}:${port}");
    }
}
