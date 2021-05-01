package org.tigerface.starter.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tigerface.starter.service.DeployService;
import org.tigerface.starter.service.FlowBuilder;

@Configuration
//@ConditionalOnClass(RestRoute.class)
//@ConditionalOnMissingBean(type="org.apache.camel.CamelContext")
public class FlowServerConfig {

    @Bean
    public FlowBuilder flowBuilder() {
        return new FlowBuilder();
    }

    @Bean
    public DeployService deployService() {
        return new DeployService();
    }

    @Bean
    public RouteBuilder restRoute() {
        return new RestRoute();
    }
}
