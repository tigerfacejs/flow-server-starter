package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.domain.Flow;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.camel.util.function.Suppliers.constant;

@Slf4j
public class SingleUploadNode extends EntryNode {

    @Override
    public String getUri(Map node) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String path = props.get("path") != null ? (String) props.get("path") : "/upload";
        if (!path.startsWith("/")) path = "/" + path;
        String uri = "jetty:http://0.0.0.0:8086" + path + "?httpMethodRestrict=post";
        return uri;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node) {
        Flow flow = (Flow) node.get("flow");
        String uri = this.getUri(node);

        Map<String, Object> props = (Map<String, Object>) node.get("props");

        ProcessorDefinition newRouteDef = this.builder.from(uri);

        newRouteDef.process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                System.out.println("LOG >>> routeUri = "+exchange.getFromEndpoint().getEndpointUri()+", exchangeId = " + exchange.getExchangeId());
            }
        });

        String field = props.get("field") != null ? (String) props.get("field") : "file";
        newRouteDef.process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                AttachmentMessage attachmentMessage = exchange.getIn(AttachmentMessage.class);
                Map headers = exchange.getIn().getHeaders();
                Map<String, Attachment> map = attachmentMessage.getAttachmentObjects();
                for (String name : map.keySet()) {
                    if (name.equalsIgnoreCase(field)) {
                        Attachment attachment = map.get(name);
                        String contentType = attachment.getHeader("Content-Type");
                        if (contentType == null) {
                            System.out.println("表单字段：" + name + "=" + headers.get(name));
                            throw new RuntimeException("上传组件指定字段 " + field + " 不是文件类型");
                        }
                        String contentDisposition = attachment.getHeader("Content-Disposition");
                        Pattern pattern = Pattern.compile("filename=\".+$");
                        Matcher matcher = pattern.matcher(contentDisposition);
                        if (matcher.find()) {
                            String filename = matcher.group().replaceAll("filename=|\"", "");
                            exchange.getIn().setHeader("SingleUploadFilename", filename);
                            exchange.getIn().setHeader("SingleUploadContentType", contentType);
                        }
                        byte[] data = exchange.getContext().getTypeConverter()
                                .convertTo(byte[].class, attachment.getDataHandler().getInputStream());
                        exchange.getIn().setBody(data);
                        return;
                    }
                }
            }
        });

        log.info("创建 single upload 节点 ");
        return (T) newRouteDef;
    }
}
