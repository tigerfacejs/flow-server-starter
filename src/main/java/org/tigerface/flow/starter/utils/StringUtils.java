package org.tigerface.flow.starter.utils;

import java.util.Map;

public class StringUtils {
    static public String queryParam(String name, Object value, String url) {
        String part = "";
        if (value != null && value.toString().length() > 0) {
            if (url.indexOf("?") >= 0) part += "&";
            else part += "?";
            part += name + "=" + value;
        }
        return part;
    }
}
