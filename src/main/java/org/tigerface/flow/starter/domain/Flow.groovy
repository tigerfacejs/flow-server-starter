package org.tigerface.flow.starter.domain

import lombok.Data
import org.tigerface.flow.starter.domain.FlowStatus;

@Data
public class Flow {
    String id;
    String json;
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
}
