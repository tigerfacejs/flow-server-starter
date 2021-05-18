package org.tigerface.flow.starter.service

import groovy.util.logging.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext
import org.eclipse.jetty.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
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
    boolean deploy(String flowJson) throws Exception {
        log.info("开始部署");
        Flow flow = flowBuilder.parse(flowJson);
        flow.setJson(flowJson);

        // 简化部署，只检查ID，直接部署
        remove(flow.getId());
        camelContext.addRoutes(flowBuilder.build(flow));

        return true;
    }

    /**
     * 移除 Flow
     *
     * @param id
     * @return
     * @throws Exception
     */
    boolean remove(String id) throws Exception {
        if (camelContext.getRoute(id) != null) {
            log.info("删除已存在的流程 {}", id);
            ((DefaultCamelContext) camelContext).stopRoute(id);
            return camelContext.removeRoute(id);
        }
        return false;
    }

    Object listFlows() {
        def routes = camelContext.getRoutes();
        Map<String, Map> info = new HashMap<>();
        for (Route route : routes) {
            def groupName = route.getGroup();
            Map group = info.get(groupName);
            if (group == null) {
                group = ['title': groupName, 'key': groupName, 'children': new ArrayList()];
                info.put(groupName, group);
            }
            group.get('children').add([
                    'key'  : route.getId(),
                    'title': getRouteInfo(route).get("json").get("desc")
            ]);
        }
        return info.values();
    }

    private Map getRouteInfo(Route route) {
        def desc = ['desc': route.getDescription()];
        try {
            desc = JSON.parse(route.getDescription())
        } catch (IllegalStateException e) {
            // ignore
        }
        return [
                'id'          : route.getId(),
                'group'       : route.getGroup(),
                'uri'         : URLDecoder.decode(route.getEndpoint().getEndpointUri(), "UTF-8"),
                'uptimeMillis': route.getUptimeMillis(),
                'json'        : desc];
    }

    Object getFlow(String id) throws UnsupportedEncodingException {
        Route route = camelContext.getRoute(id);
        if (route != null) {
            return getRouteInfo(route);
        }
        return [];
    }
}
