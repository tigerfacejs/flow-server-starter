package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestFromNode extends EntryNode {

    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String service = (String) props.get("service");
        String path = (String) props.get("path");
        String method = (String) props.get("method");
        String uri = "rest:" + method + ":" + service + path;
        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String service = (String) props.get("service");
        String path = (String) props.get("path");
        String method = (String) props.get("method");
        String desc = (String) props.get("desc");

        RestDefinition rd = (service != null & service.length() > 0) ? this.builder.rest(service) : this.builder.rest();

        if (method.equalsIgnoreCase("GET")) {
            rd = (path != null & path.length() > 0) ? rd.get(path) : rd.get();
        } else if (method.equalsIgnoreCase("POST")) {
            rd = (path != null & path.length() > 0) ? rd.post(path) : rd.post();
        } else if (method.equalsIgnoreCase("PUT")) {
            rd = (path != null & path.length() > 0) ? rd.put(path) : rd.put();
        } else if (method.equalsIgnoreCase("DELETE")) {
            rd = (path != null & path.length() > 0) ? rd.delete(path) : rd.delete();
        }
        rd = rd.consumes("application/json").produces("application/json");
        rd = rd.description(desc);

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
                System.out.println("LOG >>> routeUri = " + exchange.getFromEndpoint().getEndpointUri() + ", exchangeId = " + exchange.getExchangeId());
            }
        });

        log.info("创建 rest 节点 ");
        return (T) newRouteDef;
    }
}
