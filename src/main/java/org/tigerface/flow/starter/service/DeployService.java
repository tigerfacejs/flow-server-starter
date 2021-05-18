package org.tigerface.flow.starter.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Route;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.tigerface.flow.starter.domain.Flow;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DeployService {
    @Autowired
    DefaultCamelContext camelContext;

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
            camelContext.stopRoute(id);
            return camelContext.removeRoute(id);
        }
        return false;
    }

    Object getFlow(String id) throws UnsupportedEncodingException {
        Route route = camelContext.getRoute(id);
        Map map = new HashMap<String, Object>();
        if (route != null) {
            map.put("id", route.getId());
            map.put("uri", URLDecoder.decode(route.getEndpoint().getEndpointUri(), "UTF-8"));
            map.put("uptimeMillis", route.getUptimeMillis());
            map.put("description", route.getDescription());
        }
        return map;
    }
}
