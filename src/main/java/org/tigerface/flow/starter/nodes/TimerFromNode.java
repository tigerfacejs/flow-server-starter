package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.tigerface.flow.starter.utils.StringUtils;

import java.util.Map;

@Slf4j
public class TimerFromNode extends EntryNode {

    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String name = (String) props.get("name");
        String uri = "timer:" + name;

        String time = (String) props.get("time");
        if (time != null && time.length() > 0) {
            uri += "?fixedRate=true&time=" + time;
        } else {
            String delay = (String) props.get("delay");
            if (delay != null && delay.length() > 0) {
                uri += "?fixedRate=true&delay=" + delay;
            }
        }

        uri += StringUtils.queryParam("period", props.get("period"), uri);
        uri += StringUtils.queryParam("repeatCount", props.get("repeatCount"), uri);

        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = getUri(node);
        String desc = (String) props.get("desc");

        RouteDefinition rd = this.builder.from(uri).description(desc);

        log.info("创建 Timer 节点 ");
        return (T) rd;
    }
}
