package org.tigerface.flow.starter.domain

import lombok.Data
import org.tigerface.flow.starter.domain.FlowStatus;

@Data
public class Flow {
    String id;
    String name;
    String desc;
    String status = FlowStatus.draft.toString();
    String version;
    List<HashMap> nodes = new ArrayList<>();

    String getEntry() {
        if (nodes.size() > 0) {
            def entry = nodes.get(0);
            def type = entry.type ? entry.type : entry.eip;
            if (type == "from") {
                return type;
            }
        }
        throw new RuntimeException("无效的 entry")
    }

//    String getEntry() {
//        if (nodes.size() > 0) {
//            def entry = nodes.get(0);
//            if (entry.eip == "from") {
//                switch (entry.props.comp) {
//                    case "rest":
//                        return "rest:${entry.props.method}:${entry.props.path}?produces=application/json&consumes=application/json";
//                    case "direct":
//                        return "direct:${name}";
//                }
//            }
//        }
//        throw new RuntimeException("无效的 entry")
//    }

//    String getFullEntry() {
//        if (nodes.size() > 0) {
//            def entry = nodes.get(0);
//            if (entry.eip == "from") {
//                switch (entry.props.comp) {
//                    case "rest":
//                        return "rest:${entry.props.method}:${status == FlowStatus.published.toString() ? 'v' + getVersion() : 'test'}:${entry.props.path}";
//                    case "direct":
//                        return "direct:${name}_${status == FlowStatus.published.toString() ? 'v' + getVersion() : 'test'}";
//                }
//            }
//        }
//        throw new RuntimeException("无效的 entry")
//    }
//
//    String getIDEntry() {
//        return "direct:${name}";
//    }
//    String getFullIDEntry() {
//        return "direct:${name}_${status == FlowStatus.published.toString() ? 'v' + getVersion() : 'test'}";
//    }
}
