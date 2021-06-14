package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.tigerface.flow.starter.domain.Flow;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestFromNode extends EntryNode {

    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String uri = "rest:" + (String) props.get("method") + ":" + (String) props.get("path");
        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Flow flow = (Flow) node.get("flow");
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String service = (String) props.get("service");
        String path = (String) props.get("path");
        String method = (String) props.get("method");
        String desc = (String) props.get("desc");

        RestDefinition rd = this.builder.rest(service).description(desc);
        if(method.equalsIgnoreCase("GET")) {
            rd = rd.get(path);
        } else if(method.equalsIgnoreCase("POST")) {
            rd = rd.post(path);
        } else if(method.equalsIgnoreCase("PUT")) {
            rd = rd.put(path);
        } else if(method.equalsIgnoreCase("DELETE")) {
            rd = rd.delete(path);
        }

        List<Map> params = (List<Map>) props.get("swagger");

        if (params != null && !params.isEmpty()) {
            for (Map param : params) {
                String name = (String) param.get("name");
                String type = (String) param.get("type");
                String paramDesc = (String) param.get("desc");
                if (name != null && name.length() > 0
                        && type != null && type.length() > 0) {
                    rd.param().name(name).description(paramDesc).type(RestParamType.valueOf(type)).endParam();
                }
            }
        }

        ProcessorDefinition newRouteDef = rd.route();

        newRouteDef.process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                System.out.println("LOG >>> routeUri = "+exchange.getFromEndpoint().getEndpointUri()+", exchangeId = " + exchange.getExchangeId());
            }
        });

        log.info("创建 rest 节点 ");
        return (T) newRouteDef;
    }
}
