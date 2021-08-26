package org.tigerface.flow.starter.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AttachmentProcessor implements Processor {
    private List<String> fields;

    public AttachmentProcessor(List fields) {
        this.fields = fields;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        if (this.fields == null || this.fields.size() == 0) this.fields = new ArrayList<String>() {{
            add("file");
        }};

        if (fields.size() == 1) {
            exchange.getIn().setBody(extract(exchange, fields.get(0)));
        } else {
            List<Map> list = new ArrayList<>();
            for (String field : fields) {
                Map data = extract(exchange, field);
                if (data != null) list.add(data);
            }

            exchange.getIn().setBody(list);
        }
    }

    private Map extract(Exchange exchange, String field) throws IOException {
        AttachmentMessage attachmentMessage = exchange.getIn(AttachmentMessage.class);
        Map headers = attachmentMessage.getHeaders();
        Map<String, Attachment> map = attachmentMessage.getAttachmentObjects();
        for (String name : map.keySet()) {
            if (name.equalsIgnoreCase(field)) {
                Attachment attachment = map.get(name);
                String contentType = attachment.getHeader("Content-Type");
                if (contentType == null) {
                    log.debug("表单字段：" + name + "=" + headers.get(name));
                    throw new RuntimeException("上传组件指定字段 " + field + " 不是文件类型");
                }
                String contentDisposition = attachment.getHeader("Content-Disposition");
                Pattern pattern = Pattern.compile("filename=\".+$");
                Matcher matcher = pattern.matcher(contentDisposition);
                if (matcher.find()) {
                    String filename = matcher.group();
                    filename = filename.substring("filename=\"".length());
                    filename = filename.substring(0, filename.indexOf("\""));
//                  filename = filename.replaceAll("filename=|\"", "");
                    byte[] data = exchange.getContext().getTypeConverter()
                            .convertTo(byte[].class, attachment.getDataHandler().getInputStream());
                    String finalFilename = filename;
                    return new HashMap<String, Object>() {{
                        put("filename", finalFilename);
                        put("contentType", contentType);
                        put("inputStream", new BufferedInputStream(attachment.getDataHandler().getInputStream()));
                        put("data", data);
                        put("field", field);
                    }};
                }
            }
        }
        return null;
    }
}
