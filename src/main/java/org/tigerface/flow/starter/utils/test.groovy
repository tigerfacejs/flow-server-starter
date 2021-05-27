package org.tigerface.flow.starter.utils

import org.apache.camel.AggregationStrategy
import org.apache.camel.Exchange

class ExampleAggregationStrategy implements AggregationStrategy {
    @Override
    Exchange aggregate(Exchange original, Exchange resource) {
        Object ob = original.getIn().getBody();
        Object rb = resource.getIn().getBody();
        Object mergeResult = ob+ ' '+rb;
        original.getIn().setBody(mergeResult);
        return original;
    }
}
