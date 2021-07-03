package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.tigerface.flow.starter.utils.StringUtils;

import java.util.Map;

@Slf4j
public class WebSocketFromNode extends EntryNode {
    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String host = (String) props.get("host");
        String port = (String) props.get("port");
        String resourceUri = (String) props.get("resourceUri");
        String uri = "websocket://" + host + ":" + port + resourceUri;

        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = getUri(node);
        String desc = (String) props.get("desc");

        RouteDefinition rd = this.builder.from(uri).description(desc);

        log.info("创建 WebSocket 节点 ");
        log.info("URI = " + uri);
        return (T) rd;
    }
}
