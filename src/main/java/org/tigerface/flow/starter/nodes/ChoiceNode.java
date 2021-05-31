package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.WireTapDefinition;
import org.apache.camel.model.language.ExpressionDefinition;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class ChoiceNode implements IFlowNode {
    RouteBuilder builder;

    public ChoiceNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        List<Map> whens = (List<Map>) props.get("when");
        Map otherwise = (Map)props.get("otherwise");

        ChoiceDefinition cd = rd.choice();
        if (whens != null) {
            for (Map when : whens) {
                List<Map> nodes = (List<Map>) when.get("nodes");
                if(!nodes.isEmpty()) {
                    Predicate exp = PredicateExp.create(when);
                    ChoiceDefinition wd = cd.when(exp);

                    for (Map sub : nodes) {
                        FlowNodeFactory factory = new FlowNodeFactory(builder);
                        wd = factory.createAndAppend(sub, wd);
                    }
                }
            }
        }

        if(otherwise!=null) {
            List<Map> nodes = (List<Map>) otherwise.get("nodes");
            if(!nodes.isEmpty()) {
                ChoiceDefinition wd = cd.otherwise();

                for (Map sub : nodes) {
                    FlowNodeFactory factory = new FlowNodeFactory(builder);
                    wd = factory.createAndAppend(sub, wd);
                }
            }
        }

        log.info("创建 choice 节点");

        return rd;
    }
}
