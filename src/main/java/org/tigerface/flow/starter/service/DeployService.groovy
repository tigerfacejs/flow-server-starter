package org.tigerface.flow.starter.service

import groovy.util.logging.Slf4j;
import org.apache.camel.CamelContext
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.Route
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.tigerface.flow.starter.domain.Flow;


@Slf4j
public class DeployService {
    @Autowired
    CamelContext camelContext;

    @Autowired
    FlowBuilder flowBuilder;

    @Value('${flow.server}')
    private String flowServer;

    /**
     * 部署 Flow
     *
     * @param flowJson
     * @return
     * @throws Exception
     */
    String deploy(String flowJson) throws Exception {
        log.info("------ 准备部署流程 ------");
        log.info("服务器: " + flowServer);
        log.info("解析...");
        Flow flow = flowBuilder.parse(flowJson);
        flow.setJson(flowJson);

        String uri = flow.getUri();
        log.info("入口 uri = " + uri);

        String id = flow.getRouteId();
        log.info("生成 id = " + id);

        def ret = remove(id);
        log.info("尝试移除现有流程: " + ret)

        RouteBuilder routeBuilder = flowBuilder.build(flow);

        log.info("部署...");
        camelContext.addRoutes(routeBuilder);
        log.info("------ end ------\n");
        return id;
    }

    boolean remove(String id) throws Exception {
        if (camelContext.getRoute(id) != null) {
            log.info("流程已存在：{}", id);
            ((DefaultCamelContext) camelContext).stopRoute(id);
            return camelContext.removeRoute(id);
        }
        return true;
    }

    private treeNodeComparator = new Comparator() {
        int compare(a, b) {
            if (a.title > b.title) {
                return 1;
            } else if (a.title < b.title) {
                return -1;
            } else return 0;
        }
    }

    private Map createGroups(Map index, String groupNames) {
        Map exist = index.get(groupNames);
        if (exist) return exist;

//        Map parent = index;
//        log.info("createGroup ${groupNames} ${groupNames.split('[.]')}");

        String parentName = '';
        Map parent = null;
        for (String currentName : groupNames.split('[.]')) {
            if (parentName.length() > 0) parentName += '.'
            parentName += currentName;
            Map group = index.get(parentName);
            if (group == null) {
                log.info("创建组 ${parentName}")
                group = ['title': currentName, 'key': currentName, 'children': new ArrayList(), 'isLeaf': false];
                index.put(parentName, group);
                if (parent != null) {
                    parent.get('children').add(group)
                    Collections.sort(parent.get('children'), treeNodeComparator);
                };
            }
            parent = group;
        }

        return index.get(groupNames);
    }

    Object listFlows() {
        def routes = camelContext.getRoutes();
        Map<String, Map> index = new HashMap<>();
        for (Route route : routes) {
            String flowJson = getRouteInfo(route).get('flow');
            if (flowJson != null) {
                Flow flow = flowBuilder.parse(flowJson);
                def groupName = route.getGroup() ? route.getGroup() : '缺省分组';
                Map group = createGroups(index, groupName.replaceAll('/', '.'));
                group.get('children').add([
                        'key'   : route.getId(),
                        'title' : flow.desc,
                        'isLeaf': true
                ]);
                Collections.sort(group.get('children'), treeNodeComparator);
            }
        }
        List result = new ArrayList();
        for (String key : index.keySet()) {
            if (key.indexOf('.') == -1) {
                result.add(index.get(key));
            }
        }
        Collections.sort(result, treeNodeComparator);
        return result;
    }

    Object listDirectFlows() {
        def routes = camelContext.getRoutes();
        List<Map> flows = new ArrayList<>();
        for (Route route : routes) {
            String flowJson = getRouteInfo(route).get('flow');
            if (flowJson != null) {
                Flow flow = flowBuilder.parse(flowJson);
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

    private Map getRouteInfo(Route route) {
        def routeId = route.getId();
        def group = route.getGroup();
        def desc = route.getDescription();
        try {
            if (desc != null && desc.startsWith("{")) {
                String flowJson = desc;
                Flow flow = flowBuilder.parse(flowJson);
                return [
                        'routeId'     : routeId,
                        'group'       : group ? group : '缺省分组',
                        'desc'        : flow.desc,
                        'uri'         : URLDecoder.decode(route.getEndpoint().getEndpointUri(), "UTF-8"),
                        'uptimeMillis': route.getUptimeMillis(),
                        'flow'        : flowJson
                ];
            }
        } catch (RuntimeException e) {
            // ignore
            e.printStackTrace();
        }
        return new HashMap();
    }

    Map getFlow(String id) throws UnsupportedEncodingException {
        Route route = camelContext.getRoute(id);
        if (route != null) {
            return getRouteInfo(route);
        }
        return new HashMap();
    }
}
