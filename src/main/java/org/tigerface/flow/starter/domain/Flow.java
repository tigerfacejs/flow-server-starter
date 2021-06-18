package org.tigerface.flow.starter.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.tigerface.flow.starter.nodes.EntryNode;
import org.tigerface.flow.starter.nodes.FlowNode;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;

@Data
public class Flow {
    private String currentServer;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String _entry = null;

    String json;
    String group;
    String desc;
    List<Map> nodes = new ArrayList<>();

    String getUri() {
        if (_entry == null) {
            synchronized (this) {
                if (nodes.size() > 0) {
                    Map node = nodes.get(0);
                    Class clazz = FlowNodeFactory.getNodeClass(node);
                    try {
                        clazz.getMethod("getUri", Map.class);
                        Constructor constructor = clazz.getConstructor();
                        EntryNode nodeObj = (EntryNode) constructor.newInstance();
                        this._entry = nodeObj.getUri(node);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else throw new RuntimeException("空流程");
            }
        }
        return this._entry;
    }

    void setUri(String v) {
        // do nothing
    }

    String getRouteId() throws UnsupportedEncodingException {
        return Base64.getUrlEncoder().encodeToString((this.currentServer + "_" + this.getUri()).getBytes("utf-8"));
    }

    void setRouteId(String v) {
        // do nothing
    }

    Map toMap() throws UnsupportedEncodingException {
        return new HashMap() {{
            put("routeId", getRouteId());
            put("uri", getUri());
            put("group", group);
            put("desc", desc);
            put("nodes", nodes);
        }};
    }
}
