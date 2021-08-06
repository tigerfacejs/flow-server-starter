package org.tigerface.flow.starter.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.groovy.GroovyShellFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public class FlowServerAutoConfig implements ApplicationContextAware {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public FlowBuilder flowBuilder() {
        PluginManager.init(this.applicationContext);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

//    @Primary
//    @Bean(name = "dataSource")
//    @ConfigurationProperties(prefix = "spring.datasource")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().build();
//    }

//    @Bean(name = "flowDS")
//    public DataSource flowDS() {
//        DataSourceBuilder builder = DataSourceBuilder.create();
//        builder.driverClassName("org.apache.derby.jdbc.EmbeddedDriver");
//        builder.url("jdbc:derby:derbyDB;create=true");
//        return builder.build();
//    }
}
