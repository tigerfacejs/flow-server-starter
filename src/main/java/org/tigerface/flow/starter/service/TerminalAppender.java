package org.tigerface.flow.starter.service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.LoggerFactory;

public class TerminalAppender extends AppenderBase<ILoggingEvent> {
    CamelContext camelContext = null;
    FluentProducerTemplate template = null;

    public void init() throws Exception {
        init(null);
    }

    public void init(CamelContext camelContext) throws Exception {
        this.camelContext = camelContext;
        if (this.camelContext == null) {
            camelContext = new DefaultCamelContext();
            
//            RouteBuilder builder = new RouteBuilder() {
//                public void configure() {
//                    String uri = "ahc-ws://127.0.0.1:7086/terminal";
//                    from("direct:logAppendToTerminal").to(uri);
//                    from(uri).log(">>> from server ${body}");
//                }
//            };

//            camelContext.addRoutes(builder);
            camelContext.start();
        }

        this.template = camelContext.createFluentProducerTemplate();

        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ROOT")).addAppender(this);
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        setContext(ctx);
        start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (template != null) {
            String msg = event.getFormattedMessage();
            template.withBody(msg).to("direct:logAppendToTerminal").request();
        }
    }
}
