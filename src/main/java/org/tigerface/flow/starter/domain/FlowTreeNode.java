package org.tigerface.flow.starter.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FlowTreeNode {
    String title;
    String key;
    List children = new ArrayList();

    boolean getIsLeaf() {
        return children.isEmpty();
    }
}
