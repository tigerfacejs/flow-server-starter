package org.tigerface.flow.starter.service;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.nodes.FlowNode;

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

    public static Class getNodeClass(Map<String, Object> node) {
        String type = (String) node.get("type");
        Class clazz = nodeClazz.get(type);
        if(clazz==null) throw new RuntimeException("工厂创建对象失败：节点类型 "+type+" 不存在");
        return clazz;
    }

    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Class clazz = FlowNodeFactory.getNodeClass(node);
        FlowNode nodeObj = null;
        try {
            nodeObj = (FlowNode) clazz.getConstructor().newInstance();
            nodeObj.setBuilder(builder);
            T pd = nodeObj.createAndAppend(node, rd);
//            pd.wireTap("direct:esTracker").newExchange(exchange -> {
//                Message message = exchange.getIn();
//                message.setHeader("LastNodeType", node.get("type"));
////                System.out.println("\n>>>" + node.get("type"));
//                node.remove("flow");
//                message.setBody(node);
//            });
            return pd;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("节点组件 " + node.get("type") + " 实例化失败");
    }
}
