package org.tigerface.flow.starter.service;

import org.tigerface.flow.starter.nodes.*;

public class PluginManager {
    public static void init() {
        try {
            FlowNodeFactory.register("from", FromNode.class);
            FlowNodeFactory.register("claimCheck", ClaimCheckNode.class);

            FlowNodeFactory.register("bean", BeanNode.class);
            FlowNodeFactory.register("transform", TransformNode.class);
            FlowNodeFactory.register("to", ToNode.class);
            FlowNodeFactory.register("setBody", SetBodyNode.class);
            FlowNodeFactory.register("setHeader", SetHeaderNode.class);
            FlowNodeFactory.register("script", ScriptNode.class);
            FlowNodeFactory.register("filter", FilterNode.class);
            FlowNodeFactory.register("dynamicRouter", DynamicRouterNode.class);
            FlowNodeFactory.register("wireTap", WireTapNode.class);
            FlowNodeFactory.register("log", LogNode.class);
            FlowNodeFactory.register("marshal", MarshalNode.class);
            FlowNodeFactory.register("unmarshal", UnMarshalNode.class);
            FlowNodeFactory.register("enrich", EnrichNode.class);
            FlowNodeFactory.register("choice", ChoiceNode.class);

            FlowNodeFactory.register("rest", RestFromNode.class);
            FlowNodeFactory.register("cors", CorsNode.class);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}