package org.tigerface.flow.starter.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tigerface.flow.starter.route.RestRoute;
import org.tigerface.flow.starter.service.DeployService;
import org.tigerface.flow.starter.service.FlowBuilder;
import org.tigerface.flow.starter.service.PluginManager;

@Configuration
//@ConditionalOnClass(RestRoute.class)
//@ConditionalOnMissingBean(type="org.apache.camel.CamelContext")
public class FlowServerAutoConfig {

    @Bean
    public FlowBuilder flowBuilder() {
        PluginManager.init();
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
