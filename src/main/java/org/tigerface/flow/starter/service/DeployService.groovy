package org.tigerface.flow.starter.service

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext
import org.springframework.beans.factory.annotation.Autowired
import org.tigerface.flow.starter.domain.Flow;


@Slf4j
public class DeployService {
    @Autowired
    CamelContext camelContext;

    @Autowired
    FlowBuilder flowBuilder;

    /**
     * 部署 Flow
     *
     * @param flowJson
     * @return
     * @throws Exception
     */
    Map deploy(String flowJson) throws Exception {
        log.info("开始部署");
        Flow flow = flowBuilder.parse(flowJson);
        flow.setJson(flowJson);
        log.info("流程细节", flow);

        // 简化部署，只检查ID，直接部署
        remove(flow.getKey());
        camelContext.addRoutes(flowBuilder.build(flow));

        return [message: '部署完毕'];
    }

    /**
     * 移除 Flow
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map remove(String id) throws Exception {
        if (camelContext.getRoute(id) != null) {
            log.info("删除已存在的流程 {}", id);
            ((DefaultCamelContext) camelContext).stopRoute(id);
            if (camelContext.removeRoute(id)) {
                return [message: '删除成功'];
            }
        }
        return [message: '删除失败'];
    }

    Object listFlows() {
        def routes = camelContext.getRoutes();
        Map<String, Map> info = new HashMap<>();
        for (Route route : routes) {
            Flow flow = getRouteInfo(route).get('flow');
            if (flow != null) {
                def groupName = route.getGroup() ? route.getGroup() : '缺省分组';
                Map group = info.get(groupName);
                if (group == null) {
                    group = ['title': groupName, 'key': groupName, 'children': new ArrayList(), 'isLeaf': false];
                    info.put(groupName, group);
                }

                group.get('children').add([
                        'key'   : route.getId(),
                        'title' : flow.desc,
                        'isLeaf': true
                ]);
            }
        }
        return info.values();
    }

    Object listDirectFlows() {
        def routes = camelContext.getRoutes();
        List<Map> flows = new ArrayList<>();
        for (Route route : routes) {
            Flow flow = getRouteInfo(route).get('flow');

            if (flow != null) {
                def entry = flow.nodes.get(0);
                def type = entry.type ? entry.type : entry.eip;
                if (type == 'from' && entry.props.uri.startsWith('direct:')) {
                    flows.add([
                            'value': entry.props.uri,
                            'label': flow.desc
                    ]);
                }
            }
        }
        return flows;
    }

//    private Map getRouteInfo(Route route) {
//        def desc = ['desc': route.getDescription()];
//        try {
//            if (desc.desc!=null && desc.desc.startsWith("{")) {
//                desc = new JsonSlurper().parseText(desc.desc)
//            }
//        } catch (RuntimeException e) {
//            // ignore
//            e.printStackTrace();
//        }
//        return [
//                'id'          : route.getId(),
//                'group'       : route.getGroup(),
//                'uri'         : URLDecoder.decode(route.getEndpoint().getEndpointUri(), "UTF-8"),
//                'uptimeMillis': route.getUptimeMillis(),
//                'json'        : desc
//        ];
//    }

    private Map getRouteInfo(Route route) {
        def id = route.getId();
        def group = route.getGroup();
        def desc = route.getDescription();
        try {
            if (desc != null && desc.startsWith("{")) {
                Flow flow = flowBuilder.parse(desc);
                return [
                        'id'          : id,
                        'group'       : group ? group : '缺省分组',
                        'uri'         : URLDecoder.decode(route.getEndpoint().getEndpointUri(), "UTF-8"),
                        'uptimeMillis': route.getUptimeMillis(),
                        'flow'        : flow
                ];
            }
        } catch (RuntimeException e) {
            // ignore
            e.printStackTrace();
        }
        return new HashMap();
    }

    Object getFlow(String id) throws UnsupportedEncodingException {
        Route route = camelContext.getRoute(id);
        if (route != null) {
            return getRouteInfo(route);
        }
        return new HashMap();
    }
}
