package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.model.LoadBalanceDefinition;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.ProcessorDefinition;

import java.util.List;
import java.util.Map;

/*
    {
        "type": "multicast",
        "props": {
            "timeout": "500",
            "multicast": ["direct:a", "direct:b", "direct:c"]
        }
    }
*/
@Slf4j
public class LoadBalanceNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        List<String> destinations = (List<String>) props.get("destinations");
        String policy = (String) props.get("policy");
        String distributionRatio = (String) props.get("distributionRatio");
        distributionRatio = distributionRatio != null ? distributionRatio : "";
        Map<String, Object> sticky = (Map<String, Object>) props.get("sticky");
        String[] delimiters = new String[]{",", ";", ":", " "};
        String distributionRatioDelimiter = null;
        for (String delimiter : delimiters) {
            if (distributionRatio.indexOf(delimiter) > 0) {
                distributionRatioDelimiter = delimiter;
                break;
            }
        }

        if (destinations != null && destinations.size() > 0) {
            LoadBalanceDefinition ld;

            ld = rd.loadBalance();
            if (policy.equalsIgnoreCase("Round Robin")) {
                ld = ld.roundRobin();
            } else if (policy.equalsIgnoreCase("Random")) {
                ld = ld.random();
            } else if (policy.equalsIgnoreCase("Sticky")) {
                ld = ld.sticky(Exp.create(sticky));
            } else if (policy.equalsIgnoreCase("Topic")) {
                ld = ld.topic();
            } else if (policy.equalsIgnoreCase("Failover")) {
                ld = ld.failover();
            } else if (policy.equalsIgnoreCase("Weighted Round-Robin")) {
                if (distributionRatioDelimiter == null) throw new RuntimeException("权重分布率不能为空");
                ld = ld.weighted(true, distributionRatio, distributionRatioDelimiter);
            } else if (policy.equalsIgnoreCase("Weighted Random")) {
                if (distributionRatioDelimiter == null) throw new RuntimeException("权重分布率不能为空");
                ld = ld.weighted(false, distributionRatio, distributionRatioDelimiter);
            }

            for (String uri : destinations) {
                ld.toD(uri);
            }
        }

        log.info("创建 loadBalance 节点");
        return rd;
    }
}
