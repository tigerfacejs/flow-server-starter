package org.tigerface.flow.starter.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.tigerface.flow.starter.domain.Flow;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;


@Slf4j
public class DeployService {
    @Autowired
    CamelContext camelContext;

    @Autowired
    FlowBuilder flowBuilder;

    @Value("${flow.server}")
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

        boolean ret = remove(id);
        log.info("尝试移除现有流程: " + ret);

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

    private Comparator treeNodeComparator = new Comparator() {
        @Override
        public int compare(Object a, Object b) {
            return ((String) ((Map) a).get("title")).compareTo((String) ((Map) b).get("title"));
        }
    };

    private Map createGroups(Map index, String groupNames) {
        Map exist = (Map) index.get(groupNames);
        if (exist != null) return exist;

//        Map parent = index;
//        log.info("createGroup ${groupNames} ${groupNames.split("[.]")}");

        String parentName = "";
        Map parent = null;
        for (String currentName : groupNames.split("[.]")) {
            if (parentName.length() > 0) parentName += ".";
            parentName += currentName;
            Map group = (Map) index.get(parentName);
            if (group == null) {
//                group = ["title":currentName, "key":currentName, "children":new ArrayList(), "isLeaf":false];
                group = new HashMap() {{
                    put("title", currentName);
                    put("key", currentName);
                    put("children", new ArrayList());
                    put("isLeaf", false);
                }};
                index.put(parentName, group);
                if (parent != null) {
                    ((List) parent.get("children")).add(group);
                    Collections.sort(((List) parent.get("children")), treeNodeComparator);
                }
                ;
            }
            parent = group;
        }

        return (Map) index.get(groupNames);
    }

    Object listFlows() {
        List<Route> routes = camelContext.getRoutes();
        Map<String, Map> index = new HashMap<>();
        for (Route route : routes) {
            String flowJson = (String) getRouteInfo(route).get("flow");
            if (flowJson != null) {
                Flow flow = flowBuilder.parse(flowJson);
                String groupName = route.getGroup() != null ? (String) route.getGroup() : "缺省分组";
                Map group = createGroups(index, groupName.replaceAll("/", "."));
                ((List) group.get("children")).add(
                        new HashMap() {{
                            put("key", route.getId());
                            put("title", flow.getDesc());
                            put("isLeaf", true);
                        }});
                Collections.sort(((List) group.get("children")), treeNodeComparator);
            }
        }
        List result = new ArrayList();
        for (String key : index.keySet()) {
            if (key.indexOf(".") == -1) {
                result.add(index.get(key));
            }
        }
        Collections.sort(result, treeNodeComparator);
        return result;
    }

    Object listDirectFlows() {
        List<Route> routes = camelContext.getRoutes();
        List<Map> flows = new ArrayList<>();
        for (Route route : routes) {
            String flowJson = (String) getRouteInfo(route).get("flow");
            if (flowJson != null) {
                Flow flow = flowBuilder.parse(flowJson);
                Map entry = flow.getNodes().get(0);
                String type = (String) entry.get("type");
                String uri = (String) ((Map) entry.get("props")).get("uri");
                if (type == "from" && (uri != null && uri.startsWith("direct:"))) {
                    flows.add(
                            new HashMap() {{
                                put("value", uri);
                                put("label", flow.getDesc());
                            }});
                }
            }
        }
        return flows;
    }

    private Map getRouteInfo(Route route) {
        String routeId = route.getId();
        String group = route.getGroup();
        String desc = route.getDescription();
        try {
            if (desc != null && desc.startsWith("{")) {
                String flowJson = desc;
                Flow flow = flowBuilder.parse(flowJson);
                return new HashMap() {{
                    put("routeId", routeId);
                    put("group", group != null ? group : "缺省分组");
                    put("desc", flow.getDesc());
                    put("uri", URLDecoder.decode(flow.getUri(), "UTF-8"));
                    put("uptimeMillis", route.getUptimeMillis());
                    put("flow", flowJson);
                }};
            }
        } catch (Exception e) {
            log.error("获取流程信息时发生异常\n{}", e.getMessage(), e);
//            e.printStackTrace();
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
