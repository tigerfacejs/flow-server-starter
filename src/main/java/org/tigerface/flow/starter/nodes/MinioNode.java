package org.tigerface.flow.starter.nodes;

import io.minio.ListObjectsArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class MinioNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String operation = (String) props.get("operation");
        Map objectName = (Map) props.get("objectName");
        Map contentType = (Map) props.get("contentType");
        Map data = (Map) props.get("data");
        String bucket = (String) props.get("bucket");

        rd.setHeader("CamelMinioObjectName", Exp.create(objectName));
        rd.setHeader("CamelMinioContentType", Exp.create(contentType));
        rd.setBody(Exp.create(data));

        if (operation == null || operation.length() == 0 || operation.equalsIgnoreCase("upload"))
            rd.to("minio://" + bucket);
        else
            rd.to("minio://" + bucket + "?operation=" + operation);

        log.info("创建 ToMinio 节点");
        return rd;
    }
}
