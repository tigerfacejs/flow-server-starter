package org.tigerface.flow.starter.service

import groovy.util.logging.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route
import org.apache.camel.builder.RouteBuilder;
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
        log.info("------ 准备部署流程 ------");
        log.info("解析...");
        Flow flow = flowBuilder.parse(flowJson);
        flow.setJson(flowJson);
        String uri = flow.getEntryUri();
        log.info("入口 uri = " + uri);

        String id = flow.getRouteId();
        log.info("生成 id = " + id);

        def ret = _remove(id);
        log.info("尝试移除现有流程: " + ret)

        RouteBuilder routeBuilder = flowBuilder.build(flow);

        log.info("部署...");
        camelContext.addRoutes(routeBuilder);
        log.info("------ end ------\n");
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
        return _remove(id) ? [message: '删除成功'] : [message: '删除失败'];
    }

    boolean _remove(String id) throws Exception {
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
            Flow flow = getRouteInfo(route).get('flow');
            if (flow != null) {
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
        def routeId = route.getId();
        def group = route.getGroup();
        def desc = route.getDescription();
        try {
            if (desc != null && desc.startsWith("{")) {
                Flow flow = flowBuilder.parse(desc);
                return [
                        'routeId'          : routeId,
                        'group'       : group ? group : '缺省分组',
                        'uri'         : URLDecoder.decode(route.getEndpoint().getEndpointUri(), "UTF-8"),
                        'uptimeMillis': route.getUptimeMillis(),
                        'flow'        : flow.toJSON()
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
