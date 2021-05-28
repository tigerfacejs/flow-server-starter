package org.tigerface.flow.starter.nodes;

import org.apache.camel.Expression;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.language.spel.SpelExpression;
import org.apache.camel.model.language.*;

import java.util.Map;

public class PredicateExp {
    static org.apache.camel.Predicate create(Map<String, Object> props) {
        String lang = (String)props.get("lang");
        String script = (String)props.get("script");

        if(lang.equalsIgnoreCase("constant")) {
            return new ConstantExpression(script);
        }

        else if(lang.equalsIgnoreCase("simple")) {
            return new SimpleExpression(script);
        }

        else if(lang.equalsIgnoreCase("spel")) {
            return SpelExpression.spel(script);
        }

        else if(lang.equalsIgnoreCase("jsonpath")) {
            return new JsonPathExpression(script);
        }

        else if(lang.equalsIgnoreCase("xpath")) {
            return new XPathExpression(script);
        }

        else if(lang.equalsIgnoreCase("groovy")) {
            return new GroovyExpression(script);
        }

        else if(lang.equalsIgnoreCase("method")) {
            return new MethodCallExpression(script);
        }

        else if(lang.equalsIgnoreCase("header")) {
            return new HeaderExpression(script);
        }

        else throw new RuntimeException("不支持的表达式语言：${props.lang}");
    }
}
