package org.tigerface.flow.starter.nodes;

import com.google.zxing.BarcodeFormat;
import org.apache.camel.dataformat.barcode.BarcodeImageType;
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

            String barcodeImageType = (String) props.get("barcodeImageType");
            if (barcodeImageType != null) barcode.setImageType(barcodeImageType);

            String barcodeFormat = (String) props.get("barcodeFormat");
            if (barcodeFormat != null) barcode.setBarcodeFormat(barcodeFormat);

            String barcodeWidth = (String) props.get("barcodeWidth");
            if (barcodeWidth != null) barcode.setWidth(barcodeWidth);

            String barcodeHeight = (String) props.get("barcodeHeight");
            if (barcodeHeight != null) barcode.setHeight(barcodeHeight);

            return barcode;
        } else if (format.equalsIgnoreCase("csv")) {
            CsvDataFormat csv = new CsvDataFormat();
            if (props.get("skipHeaderRecord") != null)
                csv.setSkipHeaderRecord((String) props.get("skipHeaderRecord"));
            return csv;
        } else throw new RuntimeException("不支持的数据格式：${props.format}");
    }
}
