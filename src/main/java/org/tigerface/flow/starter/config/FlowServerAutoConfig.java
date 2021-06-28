package org.tigerface.flow.starter.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jdbc.BeanRowMapper;
import org.apache.camel.component.jdbc.DefaultBeanRowMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tigerface.flow.starter.route.SystemRoutes;
import org.tigerface.flow.starter.service.DeployService;
import org.tigerface.flow.starter.service.FlowBuilder;
import org.tigerface.flow.starter.service.PluginManager;
import org.tigerface.flow.starter.service.TerminalService;

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
    public TerminalService terminalService() {
        return new TerminalService();
    }

    @Bean
    public RouteBuilder restRoute() {
        return new SystemRoutes();
    }
}
