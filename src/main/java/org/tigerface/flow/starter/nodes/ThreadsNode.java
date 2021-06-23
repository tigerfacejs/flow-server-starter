package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class ThreadsNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String poolSize = props.get("poolSize") != null ? (String) props.get("poolSize") : "5";
        String maxPoolSize = props.get("maxPoolSize") != null ? (String) props.get("maxPoolSize") : "10";
        String keepAliveTime = props.get("keepAliveTime") != null ? (String) props.get("keepAliveTime") : "60";

        rd.threads().poolSize(poolSize).maxPoolSize(maxPoolSize).keepAliveTime(keepAliveTime);

        log.info("创建 Threads 节点");

        return rd;
    }
}
