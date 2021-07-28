package org.tigerface.flow.starter.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.groovy.GroovyShellFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.tigerface.flow.starter.route.SystemRoutes;
import org.tigerface.flow.starter.service.DeployService;
import org.tigerface.flow.starter.service.FlowBuilder;
import org.tigerface.flow.starter.service.PluginManager;
import org.tigerface.flow.starter.service.TerminalService;

import javax.sql.DataSource;

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
    public GroovyShellFactory groovyShellFactory() {
        return new CustomGroovyShellFactory();
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

    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

//    @Bean(name = "flowDS")
//    public DataSource flowDS() {
//        DataSourceBuilder builder = DataSourceBuilder.create();
//        builder.driverClassName("org.apache.derby.jdbc.EmbeddedDriver");
//        builder.url("jdbc:derby:derbyDB;create=true");
//        return builder.build();
//    }
}
