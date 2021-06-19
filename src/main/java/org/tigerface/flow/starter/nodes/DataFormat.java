package org.tigerface.flow.starter.nodes;

import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.dataformat.*;

import java.util.Map;

public class DataFormat {
    static public DataFormatDefinition create(Map<String, Object> props) {

        String format = (String) props.get("format");
        String type = (String) props.get("type");
        String pretty = (String) props.get("pretty");


        if (format.equalsIgnoreCase("json")) {
            JsonDataFormat json = new JsonDataFormat(JsonLibrary.Jackson);
            if (pretty != null) json.setPrettyPrint(pretty);
            if (type != null) json.setUnmarshalTypeName(type);
            return json;
        } else if (format.equalsIgnoreCase("xml")) {
            JacksonXMLDataFormat xml = new JacksonXMLDataFormat();
            if (pretty != null) xml.setPrettyPrint(pretty);
            if (type != null) xml.setUnmarshalTypeName(type);
            return xml;
        } else if (format.equalsIgnoreCase("base64")) {
            Base64DataFormat base64 = new Base64DataFormat();
            return base64;
        } else if (format.equalsIgnoreCase("barcode")) {
            BarcodeDataFormat barcode = new BarcodeDataFormat();
            return barcode;
        } else if (format.equalsIgnoreCase("csv")) {
            CsvDataFormat csv = new CsvDataFormat();
            if (props.get("skipHeaderRecord") != null)
                csv.setSkipHeaderRecord((String) props.get("skipHeaderRecord"));
            return csv;
        } else throw new RuntimeException("不支持的数据格式：${props.format}");
    }
}
