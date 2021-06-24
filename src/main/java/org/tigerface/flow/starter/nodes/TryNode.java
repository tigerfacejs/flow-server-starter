package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Predicate;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.TryDefinition;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class TryNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        List<Map> catchBlocks = (List<Map>) props.get("catchBlocks");
        Map tryBlock = (Map) props.get("tryBlock");
        Map finallyBlock = (Map) props.get("finallyBlock");

        TryDefinition td = rd.doTry();
        if (tryBlock != null) {
            List<Map> nodes = (List<Map>) tryBlock.get("nodes");
            if (!nodes.isEmpty()) {
                for (Map sub : nodes) {
                    FlowNodeFactory factory = new FlowNodeFactory(builder);
                    td = factory.createAndAppend(sub, td);
                }
            }
        }

        if (catchBlocks != null) {
            for (Map catchBlock : catchBlocks) {
                List<Map> nodes = (List<Map>) catchBlock.get("nodes");
                if (!nodes.isEmpty()) {
                    String exceptionClassname = catchBlock.get("exceptionClassname") != null ? (String) catchBlock.get("exceptionClassname") : "java.lang.Throwable";
                    try {
                        td = td.doCatch((Class<Throwable>) Class.forName(exceptionClassname));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        throw new RuntimeException("无效的类名：" + exceptionClassname);
                    }
                    for (Map sub : nodes) {
                        FlowNodeFactory factory = new FlowNodeFactory(builder);
                        td = factory.createAndAppend(sub, td);
                    }
                }
            }
        }

        if (finallyBlock != null) {
            List<Map> nodes = (List<Map>) finallyBlock.get("nodes");
            if (!nodes.isEmpty()) {
                td = td.doFinally();
                for (Map sub : nodes) {
                    FlowNodeFactory factory = new FlowNodeFactory(builder);
                    td = factory.createAndAppend(sub, td);
                }
            }
        }

        log.info("创建 try 节点");

        return rd;
    }
}
