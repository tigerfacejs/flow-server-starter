package org.tigerface.flow.starter.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnClass(MongoClient.class)
//@EnableConfigurationProperties(MongoProperties.class)
@ConditionalOnMissingBean(type = "org.springframework.data.mongodb.MongoDbFactory")
public class MongoAutoConfig {
    @Value('${flow.mongodb.host:localhost}')
    private String host;

    @Value('${flow.mongodb.port:27017}')
    private int port;

    @Value('${flow.mongodb.username:root}')
    private String username;

    @Value('${flow.mongodb.password:}')
    private String password;

    public @Bean MongoClient mongoClient() {
        return MongoClients.create("mongodb://${username}:${password}@${host}:${port}");
    }
}
