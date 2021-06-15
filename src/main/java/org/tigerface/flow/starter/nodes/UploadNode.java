package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.service.AttachmentProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class UploadNode extends EntryNode {

    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String path = props.get("path") != null ? (String) props.get("path") : "/upload";
        if (!path.startsWith("/")) path = "/" + path;
        String uri = "jetty:http://0.0.0.0:8086" + path + "?httpMethodRestrict=post";
        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        String uri = this.getUri(node);

        Map<String, Object> props = (Map<String, Object>) node.get("props");

        ProcessorDefinition newRouteDef = this.builder.from(uri);

        newRouteDef.process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                System.out.println("LOG >>> routeUri = " + exchange.getFromEndpoint().getEndpointUri() + ", exchangeId = " + exchange.getExchangeId());
            }
        });

        List fields = props.get("fields") != null ? (List) props.get("fields") : new ArrayList() {{
            add("file");
        }};

        newRouteDef.process(new AttachmentProcessor(fields));

        log.info("创建 single upload 节点 ");
        return (T) newRouteDef;
    }
}
