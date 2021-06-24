package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ThreadsDefinition;
import org.apache.camel.util.concurrent.ThreadPoolRejectedPolicy;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadsNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String poolSize = props.get("poolSize") != null ? (String) props.get("poolSize") : "10";
        String maxPoolSize = props.get("maxPoolSize") != null ? (String) props.get("maxPoolSize") : "20";
        String keepAliveTime = props.get("keepAliveTime") != null ? (String) props.get("keepAliveTime") : "60";
        String maxQueueSize = props.get("maxQueueSize") != null ? (String) props.get("maxQueueSize") : "1000";
        String allowCoreThreadTimeOut = props.get("allowCoreThreadTimeOut") != null ? (String) props.get("allowCoreThreadTimeOut") : "false";
        String rejectedPolicy =  props.get("rejectedPolicy") != null ? (String) props.get("rejectedPolicy") : "CallerRuns";

        ThreadsDefinition td = rd.threads();
        td.poolSize(poolSize);
        td.maxPoolSize(maxPoolSize);
        td.keepAliveTime(keepAliveTime);
        td.maxQueueSize(maxQueueSize);
        td.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        td.rejectedPolicy(ThreadPoolRejectedPolicy.valueOf(rejectedPolicy));
        td.timeUnit(TimeUnit.SECONDS);

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
