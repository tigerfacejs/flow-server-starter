package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.tigerface.flow.starter.domain.Flow;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestFromNode implements IFlowNode {
    RouteBuilder builder;

    public RestFromNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Flow flow = (Flow) node.get("flow");
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = "rest:" + (String) props.get("method") + ":" + (String) props.get("path");

        List<Map> params = (List<Map>) props.get("params");
        StringBuffer buf = new StringBuffer();
        if (params != null && !params.isEmpty()) {
            for (Map param : params) {
                String key = (String) param.get("key");
                String value = (String) param.get("value");
                if (key != null && key.length() > 0 && value != null && value.length() > 0) {
                    if (buf.length() > 0) buf.append("&");
                    try {
                        buf.append(URLEncoder.encode(key + "=" + value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (buf.length() > 0) {
            uri += uri.indexOf("?") < 0 ? "?" : "&";
            uri += buf.toString();
        }

        ProcessorDefinition newRouteDef = this.builder.from(uri);

        if (flow.getKey() != null) newRouteDef.routeId(flow.getKey());
        else throw new RuntimeException("流程定义缺省关键属性：key");

        if (flow.getGroup() != null)
            newRouteDef.routeGroup(flow.getGroup());
        else throw new RuntimeException("流程定义缺省关键属性：group");

        if (flow.getJson() != null)
            newRouteDef.routeDescription(flow.getJson());
        else throw new RuntimeException("流程定义缺省关键属性：json");

        log.info("识别 rest 节点 " + uri);
        return (T) newRouteDef;
    }
}
