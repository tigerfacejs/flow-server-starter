package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.utils.StringUtils;

import java.util.Map;

@Slf4j
public class RabbitMQNode extends FlowNode {
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String exchangeName = (String) props.get("exchangeName");
        String uri = "spring-rabbitmq:" + exchangeName;

        uri += StringUtils.queryParam("autoDeclare", "true", uri);
        uri += StringUtils.queryParam("routingKey", props.get("routingKey"), uri);
        uri += StringUtils.queryParam("queues", props.get("queues"), uri);
        uri += StringUtils.queryParam("asyncConsumer", props.get("asyncConsumer"), uri);
        uri += StringUtils.queryParam("exchangeType", props.get("exchangeType"), uri);
        uri += StringUtils.queryParam("exclusive", props.get("exclusive"), uri);

        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = getUri(node);
        String desc = (String) props.get("desc");

        rd.toD(uri).description(desc);

        log.info("创建 RabbitMQ 节点 ");
        log.info("URI = " + uri);
        return (T) rd;
    }
}
