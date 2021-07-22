package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.domain.Flow;

import java.util.List;
import java.util.Map;

@Slf4j
public class FromNode extends EntryNode {

    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        return (String) props.get("uri");
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Flow flow = (Flow) node.get("flow");
        String uri = this.getUri(node);

        ProcessorDefinition newRouteDef = this.builder.from(uri);
//        newRouteDef.process(new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                System.out.println("LOG >>> routeUri = "+exchange.getFromEndpoint().getEndpointUri()+", exchangeId = " + exchange.getExchangeId());
//            }
//        });

        log.info("创建 from 节点");
        return (T) newRouteDef;
    }
}
