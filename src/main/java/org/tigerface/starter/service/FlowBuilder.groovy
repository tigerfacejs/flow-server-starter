package org.tigerface.starter.service

import groovy.json.JsonSlurper
import org.apache.camel.Expression
import org.apache.camel.builder.ExpressionBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.builder.SimpleBuilder
import org.apache.camel.language.spel.SpelExpression
import org.apache.camel.model.ChoiceDefinition
import org.apache.camel.model.RouteDefinition
import org.apache.camel.model.language.JsonPathExpression
import org.apache.camel.model.language.XPathExpression
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.tigerface.starter.domain.Flow

/**
 * 流解析器
 */

/* Flow Example
{
  "id": "flow_id_xxx",
  "name": "demo",
  "desc": "测试流程",
  "version": "1.2.3",
  "status": "testing",
  "nodes": [
    {
      "id": "node_id_xxx",
      "eip": "from",
      "props": {
        "comp": "rest",
        "method": "get",
        "path": "demo/{who}"
      }
    },
    {
      "id": "node_id_xxx",
      "eip": "transform",
      "props": {
        "lang": "groovy",
        "script": "import groovy.json.JsonOutput;\n return JsonOutput.toJson([msg:\"${headers.who}，你好！\"])"
      }
    },
    {
      "id": "node_id_xxx",
      "eip": "setHeader",
      "props": {
        "header": "Content-Type",
        "lang": "constant",
        "script": "application/json; charset=UTF-8"
      }
    }
  ]
}
*/

class FlowBuilder {

    @Autowired
    ApplicationContext applicationContext

    Flow parse(String flowJson) {
        return new JsonSlurper().parseText(flowJson) as Flow;
    }

    RouteBuilder build(Flow flow) {
        def builder = new RouteBuilder() {
            @Override
            void configure() throws Exception {

                RouteDefinition rd;
                for (def node : flow.nodes) {
                    switch (node.eip) {
                        case "from":
                            rd = from(flow.getFullIDEntry()).routeId(flow.getFullIDEntry().replaceAll(":", "_")).routeDescription(flow.desc);
                            break;
                        case "transform":
                            rd.transform(exp(node.props));
                            break;
                        case "to":
                            rd.to(node.props.uri);
                            break;
                        case "bean":
                            rd.bean(node.props.bean, node.props.method);
                            break;
                        case "setHeader":
                            rd.setHeader(node.props.header, exp(node.props));
                            break;
                        case "setBody":
                            rd.setBody(exp(node.props));
                            break;
                        case "filter":
                            rd.filter(exp(node.props));
                            break;
                        case "script":
                            rd.script(exp(node.props));
                            break;
                        case "log":
                            rd.log(node.props.exp);
                            break;
                        case "choice":
                            ChoiceDefinition cd = rd.choice();
                            for (def w : node.props.when) {
                                cd.when(exp(w)).to(w.to).end();
                            }
                            if (node.props.otherwise != null) {
                                cd.otherwise().to(node.props.otherwise.to).end();
                            }
                            break;
                        case "dynamicRouter":
//                            rd.dynamicRouter(method(DynamicRouterTest.class, "slip"));
                            rd.dynamicRouter(exp(node.props));
                            break;
                        default:
                            throw new Exception("未实现的 EIP ${node.eip}")
                    }
                }
            }
        }
        return builder;
    }

    /**
     * 语言表达式
     * @param lang
     * @param script
     * @return
     */
    private Expression exp(props) {
        switch (props.lang) {
            case "constant":
                return ExpressionBuilder.constantExpression(props.script);
            case "simple":
                return SimpleBuilder.simple(props.script);
            case "spel":
                return SpelExpression.spel(props.script);
            case "method":
                return ExpressionBuilder.beanExpression(applicationContext.getBean(props.ref), props.method);
            case "header":
                return ExpressionBuilder.headerExpression(props.name);
            case "jsonpath":
                return new JsonPathExpression(props.script);
            case "xpath":
                return new XPathExpression(props.script);
            case "groovy":
                return ExpressionBuilder.languageExpression("groovy", props.script);
        }
    }
}
