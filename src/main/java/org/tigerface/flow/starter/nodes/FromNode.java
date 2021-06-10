package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.domain.Flow;

import java.util.Map;

@Slf4j
public class FromNode extends EntryNode {

    @Override
    public String getEntryUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        return (String) props.get("uri");
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Flow flow = (Flow) node.get("flow");
        String uri = this.getEntryUri(node);

        ProcessorDefinition newRouteDef = this.builder.from(uri);
//        newRouteDef.process(new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                System.out.println("LOG >>> routeUri = "+exchange.getFromEndpoint().getEndpointUri()+", exchangeId = " + exchange.getExchangeId());
//            }
//        });

//        if (flow.getKey() != null) newRouteDef.routeId(flow.getKey());
//        else throw new RuntimeException("流程定义缺省关键属性：key");

        if (flow.getGroup() != null)
            newRouteDef.routeGroup(flow.getGroup());
        else throw new RuntimeException("流程定义缺省关键属性：group");

        if (flow.getJson() != null)
            newRouteDef.routeDescription(flow.getJson());
        else throw new RuntimeException("流程定义缺省关键属性：json");

        log.info("创建 from 节点");
        return (T) newRouteDef;
    }
}
