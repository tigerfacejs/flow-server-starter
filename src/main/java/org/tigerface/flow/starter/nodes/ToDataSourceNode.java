package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jdbc.BeanRowMapper;
import org.apache.camel.component.jdbc.DefaultBeanRowMapper;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class ToDataSourceNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String dataSource = (String) props.get("dataSource");
        Object retrieveGeneratedKeys = props.get("retrieveGeneratedKeys");

        if (retrieveGeneratedKeys != null && "true".equalsIgnoreCase(retrieveGeneratedKeys.toString())) {
            rd.setHeader("CamelRetrieveGeneratedKeys", builder.constant(true));
        } else {
            rd.removeHeader("CamelRetrieveGeneratedKeys");
        }

        Map sql = (Map) props.get("sql");
        if (sql != null && sql.get("script") != null && sql.get("script").toString().length() > 0) {
            rd.setBody(Exp.create(sql));
        }

        Object useHeadersAsParameters = props.get("useHeadersAsParameters");
        if (useHeadersAsParameters != null && "true".equalsIgnoreCase(useHeadersAsParameters.toString())) {
            rd.to("spring-jdbc:" + dataSource + "?useHeadersAsParameters=true");
        } else {
            rd.to("spring-jdbc:" + dataSource);
        }

        if (retrieveGeneratedKeys != null && "true".equalsIgnoreCase(retrieveGeneratedKeys.toString())) {
            String script = "[CamelGeneratedKeysRowCount:headers.CamelGeneratedKeysRowCount, CamelGeneratedKeysRows:headers.CamelGeneratedKeysRows]";
            rd.setBody(ExpressionBuilder.languageExpression("groovy", script));
            rd.removeHeader("CamelRetrieveGeneratedKeys");
        }

        log.info("创建 toDB 节点");
        return rd;
    }
}
