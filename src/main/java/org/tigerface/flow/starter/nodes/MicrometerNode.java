package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;

import java.util.List;
import java.util.Map;

@Slf4j
public class MicrometerNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        List<Map> micrometerList = (List<Map>) props.get("micrometer");
        if (micrometerList != null) {
            for (Map micrometer : micrometerList) {
                String metricsType = (String) micrometer.get("metricsType");
                String metricsName = (String) micrometer.get("metricsName");

                if (metricsName != null && metricsName.length() > 0) {
                    if ("timer_start".equalsIgnoreCase(metricsType)) {
                        rd.to("micrometer:timer:" + metricsName + "?action=start");
                    } else if ("timer_stop".equalsIgnoreCase(metricsType)) {
                        rd.to("micrometer:timer:" + metricsName + "?action=stop");
                    } else if ("counter".equalsIgnoreCase(metricsType)) {
                        rd.to("micrometer:counter:" + metricsName);
                    }
                }
            }
        }

        log.info("创建 End 节点");
        return rd;
    }
}
