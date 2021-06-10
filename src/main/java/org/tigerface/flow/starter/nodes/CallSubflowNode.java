package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.List;
import java.util.Map;

@Slf4j
public class CallSubflowNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = (String) props.get("uri");

        List<Map> headers = (List<Map>) props.get("headers");
        for (Map header : headers) {
            String script = (String) header.get("script");
            if (script != null && script.length() > 0)
                rd.setHeader((String) header.get("header"), Exp.create(header));
        }

        Map body = (Map) props.get("body");
        String script = (String) body.get("script");
        if (script != null && script.length() > 0)
            rd.setBody(Exp.create(body));

        rd.process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Object body = exchange.getMessage().getBody();
                System.out.println("LOG TO >>> uri = " + uri + ", body class = " + body.getClass().getName());
            }
        });

        rd.to(uri);
        log.info("调用子流程 " + uri);
        return (T) rd;
    }
}
