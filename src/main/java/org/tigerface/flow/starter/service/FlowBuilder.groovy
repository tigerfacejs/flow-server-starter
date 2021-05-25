package org.tigerface.flow.starter.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
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
import org.apache.camel.model.RouteDefinition
import org.apache.camel.model.WireTapDefinition
import org.apache.camel.model.dataformat.Base64DataFormat
import org.apache.camel.model.dataformat.JacksonXMLDataFormat
import org.apache.camel.model.dataformat.JsonDataFormat
import org.apache.camel.model.dataformat.JsonLibrary
import org.apache.camel.model.language.JsonPathExpression
import org.apache.camel.model.language.XPathExpression
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
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

@Slf4j
class FlowBuilder {

    @Autowired
    ApplicationContext applicationContext

    Flow parse(String flowJson) {
        Flow flow = null;
        try {
            flow = new JsonSlurper().parseText(flowJson) as Flow;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return flow;
    }

    RouteBuilder build(Flow flow) {
        log.info("创建流程 \n{}", flow);
        if (flow.nodes.size() < 1) throw new Exception("空流程")
        def builder = new RouteBuilder() {
            @Override
            void configure() throws Exception {
                RouteDefinition rd;
                for (def node : flow.nodes) {
                    def type = node.type ? node.type : node.eip;
                    switch (type) {
                        case "from":
                            rd = from(node.props.uri).routeId(flow.getKey()).routeGroup(flow.getGroup()).routeDescription(flow.json);
                            log.info("识别 from 节点");
                            break;
                        case "rest":
                            rd = from("rest:${node.props.method}:${node.props.path}").routeId(flow.getKey()).routeGroup(flow.getGroup()).routeDescription(flow.json);
                            break;
                        case "cors":
                            rd.setHeader("Access-Control-Allow-Credentials", constant("true"));
                            rd.setHeader("Access-Control-Allow-Headers", constant("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"));
                            rd.setHeader("Access-Control-Allow-Origin", header("Origin"));
                            rd.setHeader("Access-Control-Allow-Methods", constant("GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, CONNECT, PATCH"));
                            rd.setHeader("Access-Control-Max-Age", constant("3600"));
                            log.info("识别 cors 节点");
                            break;
                        case "claimCheck":
                            rd.claimCheck(node.props.operation, node.props.key);
                            log.info("创建 claimCheck 节点");
                            break;
                        case "transform":
                            rd.transform(exp(node.props));
                            log.info("创建 transform 节点");
                            break;
                        case "to":
                            rd.toD(node.props.uri);
//                            toESLog(rd, flow)
                            log.info("创建 to 节点");
                            break;
                        case "bean":
                            rd.bean(node.props.bean, node.props.method);
                            log.info("创建 bean 节点");
                            break;
                        case "setHeader":
                            rd.setHeader(node.props.header, exp(node.props));
                            log.info("创建 setHeader 节点");
                            break;
                        case "setBody":
                            rd.setBody(exp(node.props));
                            log.info("创建 setBody 节点");
                            break;
                        case "filter":
                            rd.filter(exp(node.props));
                            log.info("创建 filter 节点");
                            break;
                        case "unmarshal":
                            rd.unmarshal(dataFormat(node.props));
                            log.info("创建 unmarshal 节点");
                            break;
                        case "marshal":
                            rd.marshal(dataFormat(node.props));
                            log.info("创建 unmarshal 节点");
                            break;
                        case "script":
                            rd.script(exp(node.props));
                            log.info("创建 script 节点");
                            break;
                        case "log":
                            LogDefinition ld = new LogDefinition(node.props.exp);
                            if (node.props.level) ld.setLoggingLevel(node.props.level);
//                            rd.log();
                            rd.process(ld);
                            log.info("创建 log 节点");
                            break;
                        case "choice":
                            ChoiceDefinition cd = rd.choice();
                            for (def w : node.props.when) {
                                cd.when(exp(w)).to(w.to).end();
                            }
                            if (node.props.otherwise != null) {
                                cd.otherwise().to(node.props.otherwise.to).end();
                            }
                            log.info("创建 choice 节点");
                            break;
                        case "dynamicRouter":
//                            rd.dynamicRouter(method(DynamicRouterTest.class, "slip"));
                            rd.dynamicRouter(exp(node.props));
                            log.info("创建 dynamicRouter 节点");
                            break;
                        case "wireTap":
                            WireTapDefinition wtd = rd.wireTap(node.props.uri);
                            wtd.newExchangeBody(exp(node.props.body))
                            for(def header : node.props.headers) {
                                wtd.newExchangeHeader(header.header, exp(header));
                            }
                            log.info("创建 wireTap 节点");
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

    private toESLog(RouteDefinition rd, flow) {
        def now = new Date();
        rd.wireTap("direct:toES")
//                .newExchangeHeader("ESIndexName", constant("flowlog"))
//                .newExchangeBody(ExpressionBuilder.languageExpression("groovy",
//                                "return ['flowId': '${flow.getKey()}', 'requestTime': '${new SimpleDateFormat('yyyy-MM-dd hh:mm:ss.S').format(now)}', 'data': ${data}]"))
                .newExchange(new Processor() {
                    @Override
                    void process(Exchange exchange) throws Exception {
                        def body = exchange.getIn().getBody();
                        def bodyJson = (body instanceof String || body instanceof byte[]) ? body : JsonOutput.toJson(body);
                        exchange.getIn().setBody(['flowId': flow.getKey(), 'requestTime': new SimpleDateFormat('yyyy-MM-dd hh:mm:ss.S').format(now), 'data': bodyJson]);
                        exchange.getIn().setHeader("ESIndexName", "flowlog");
                    }
                })
                .end();
    }
}
