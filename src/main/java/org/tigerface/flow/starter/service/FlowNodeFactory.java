package org.tigerface.flow.starter.service;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Service;
import org.tigerface.flow.starter.nodes.IFlowNode;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class FlowNodeFactory {
    static Map<String, Class> nodeClazz = new HashMap<>();
    static void register(String type, Class clazz) throws Exception {
        FlowNodeFactory.nodeClazz.put(type, clazz);
    }
    private RouteBuilder builder;
    public FlowNodeFactory(RouteBuilder builder) {
        this.builder = builder;
    }

    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        String type = (String)node.get("type");
        Class clazz = FlowNodeFactory.nodeClazz.get(type);
        IFlowNode nodeObj = null;
        try {
            nodeObj = (IFlowNode)clazz.getConstructor(RouteBuilder.class).newInstance(builder);
            return nodeObj.createAndAppend(node, rd);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("节点组件 "+type+" 实例化失败");
    }
}
