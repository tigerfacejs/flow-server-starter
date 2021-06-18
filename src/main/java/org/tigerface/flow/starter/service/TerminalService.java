package org.tigerface.flow.starter.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TerminalService {
    @Autowired
    CamelContext camelContext;

    void initTerminal(Exchange exchange) throws Exception {
        TerminalAppender terminalAppender = new TerminalAppender();
        terminalAppender.init(camelContext);
    }
}
