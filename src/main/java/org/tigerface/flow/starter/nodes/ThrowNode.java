package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@Slf4j
public class ThrowNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String exceptionClassname = props.get("exceptionClassname") != null ? (String) props.get("exceptionClassname") : "java.lang.Exception";
        String message = props.get("message") != null ? (String) props.get("message") : "";

        try {
            Class clazz = Class.forName(exceptionClassname);
            rd.throwException(clazz, message);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        log.info("创建 delay 节点");

        return rd;
    }
}
