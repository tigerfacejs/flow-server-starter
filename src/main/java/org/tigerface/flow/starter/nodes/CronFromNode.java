package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;

import java.util.List;
import java.util.Map;

@Slf4j
public class CronFromNode extends EntryNode {

    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String name = (String) props.get("name");
        String uri = "cron:" + name;
        String schedule = (String) props.get("schedule");
        if (schedule != null && schedule.length() > 0) uri += "?schedule=" + schedule;
        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = getUri(node);
        String desc = (String) props.get("desc");

        RouteDefinition rd = this.builder.from(uri).description(desc);

        log.info("创建 Cron 节点 ");
        return (T) rd;
    }
}
