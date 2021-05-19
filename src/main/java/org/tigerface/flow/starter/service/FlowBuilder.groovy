package org.tigerface.flow.starter.service

import groovy.json.JsonSlurper
import org.apache.camel.Exchange
import org.apache.camel.Expression
import org.apache.camel.Processor
import org.apache.camel.builder.ExpressionBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.builder.SimpleBuilder
import org.apache.camel.language.spel.SpelExpression
import org.apache.camel.model.ChoiceDefinition
import org.apache.camel.model.DataFormatDefinition
import org.apache.camel.model.LogDefinition
import org.apache.camel.model.ProcessorDefinition
import org.apache.camel.model.RouteDefinition
import org.apache.camel.model.UnmarshalDefinition
import org.apache.camel.model.WireTapDefinition
import org.apache.camel.model.dataformat.Base64DataFormat
import org.apache.camel.model.dataformat.JacksonXMLDataFormat
import org.apache.camel.model.dataformat.JsonDataFormat
import org.apache.camel.model.dataformat.JsonLibrary
import org.apache.camel.model.language.JsonPathExpression
import org.apache.camel.model.language.XPathExpression
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.tigerface.flow.starter.domain.Flow

import java.text.SimpleDateFormat

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
                    def type = node.type ? node.type : node.eip;
                    switch (type) {
                        case "from":
//                            def entry = flow.getFullIDEntry();
//                            def id = flow.getFullIDEntry().replaceAll(":", "_");
//                            rd = from(entry).routeId(id).routeDescription(flow.desc);

                            rd = from(node.props.uri).routeId(flow.getId()).routeGroup(flow.getGroup()).routeDescription(flow.json);
                            // wiretap 向 es 写 log
                            rd.wireTap("direct:toES").copy(false).newExchange(new Processor() {
                                @Override
                                void process(Exchange exchange) throws Exception {
                                    def now = new Date();
                                    exchange.getIn().setBody(['flowId': flow.id, 'name': flow.name, 'desc': flow.desc, 'version': flow.version, 'requestTime': new SimpleDateFormat('yyyy-MM-dd hh:mm:ss.S').format(now)]);
                                    exchange.getIn().setHeader("ESIndexName", "flowlog");
                                }
                            })
                            break;
                        case "cors":
                            rd.setHeader("Access-Control-Allow-Credentials", constant("true"));
                            rd.setHeader("Access-Control-Allow-Headers", constant("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"));
                            rd.setHeader("Access-Control-Allow-Origin", header("Origin"));
                            rd.setHeader("Access-Control-Allow-Methods", constant("GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, CONNECT, PATCH"));
                            rd.setHeader("Access-Control-Max-Age", constant("3600"));
                            break;
                        case "transform":
                            rd.transform(exp(node.props));
                            break;
                        case "to":
                            rd.toD(node.props.uri);
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
                        case "unmarshal":
                            rd.unmarshal(dataFormat(node.props));
                            break;
                        case "marshal":
                            rd.marshal(dataFormat(node.props));
                            break;
                        case "script":
                            rd.script(exp(node.props));
                            break;
                        case "log":
                            LogDefinition ld = new LogDefinition(node.props.exp);
                            if (node.props.level) ld.setLoggingLevel(node.props.level);
//                            rd.log();
                            rd.process(ld);
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
            default:
                throw new RuntimeException("不支持的表达式语言：${props.lang}");
        }
    }

    /**
     * 数据格式
     * @param props
     * @return
     */
    private DataFormatDefinition dataFormat(props) {
        switch (props.format) {
            case "json":
                JsonDataFormat json = new JsonDataFormat(JsonLibrary.Jackson);
                if (props.pretty) json.setPrettyPrint(Boolean.toString(props.pretty));
                if (props.type) json.setUnmarshalTypeName(props.type);
                return json;
            case "base64":
                Base64DataFormat base64 = new Base64DataFormat();
                return base64;
            case "xml":
                JacksonXMLDataFormat xml = new JacksonXMLDataFormat();
                if (props.pretty) xml.setPrettyPrint(Boolean.toString(props.pretty));
                if (props.type) xml.setUnmarshalTypeName(props.type);
                return xml;
            default:
                throw new RuntimeException("不支持的数据格式：${props.format}");
        }
    }
}
