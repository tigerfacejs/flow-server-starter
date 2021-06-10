package org.tigerface.flow.starter.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.builder.RouteBuilder;
import org.tigerface.flow.starter.nodes.EntryNode;
import org.tigerface.flow.starter.nodes.FlowNode;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Data
public class Flow {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String _entry = null;

    String json;
    String name;
    String group;
    String desc;
    String status = FlowStatus.draft.toString();
    String version;
    List<Map> nodes = new ArrayList<>();

    String getEntryUri() {
        if (_entry == null) {
            synchronized (this) {
                if (nodes.size() > 0) {
                    Map node = nodes.get(0);
                    Class clazz = FlowNodeFactory.getNodeClass(node);
                    try {
                        clazz.getMethod("getEntryUri", Map.class);
                        Constructor constructor = clazz.getConstructor();
                        EntryNode nodeObj = (EntryNode) constructor.newInstance();
                        this._entry = nodeObj.getEntryUri(node);
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

    String getRouteId() throws UnsupportedEncodingException {
        return Base64.getUrlEncoder().encodeToString(this.getEntryUri().getBytes("utf-8"));
    }
}
