package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ThreadsDefinition;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class ThreadsNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String poolSize = props.get("poolSize") != null ? (String) props.get("poolSize") : "5";
        String maxPoolSize = props.get("maxPoolSize") != null ? (String) props.get("maxPoolSize") : "10";
        String keepAliveTime = props.get("keepAliveTime") != null ? (String) props.get("keepAliveTime") : "60";

        ThreadsDefinition td = rd.threads();
        td.poolSize(poolSize).maxPoolSize(maxPoolSize).keepAliveTime(keepAliveTime);
        //不绑定队列
        td.maxQueueSize(-1);

        List<Map> nodes = (List<Map>) props.get("nodes");
        if (!nodes.isEmpty()) {
            for (Map sub : nodes) {
                FlowNodeFactory factory = new FlowNodeFactory(builder);
                td = factory.createAndAppend(sub, td);
            }
        }

        log.info("创建 Loop 节点");

        return rd;
    }
}
