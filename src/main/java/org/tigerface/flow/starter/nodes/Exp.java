package org.tigerface.flow.starter.nodes;

import org.apache.camel.Expression;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.SimpleBuilder;
import org.apache.camel.language.spel.SpelExpression;
import org.apache.camel.model.language.JsonPathExpression;
import org.apache.camel.model.language.XPathExpression;

import java.util.Map;

public class Exp {
    static Expression create(Map<String, Object> props) {
        String lang = (String)props.get("lang");
        String script = (String)props.get("script");

        if(lang.equalsIgnoreCase("constant")) {
            return ExpressionBuilder.constantExpression(script);
        }

        else if(lang.equalsIgnoreCase("simple")) {
            return ExpressionBuilder.simpleExpression(script);
        }

        else if(lang.equalsIgnoreCase("spel")) {
            return ExpressionBuilder.languageExpression("spel", script);
        }

        else if(lang.equalsIgnoreCase("jsonpath")) {
            return ExpressionBuilder.languageExpression("jsonpath", script);
        }

        else if(lang.equalsIgnoreCase("xpath")) {
            return ExpressionBuilder.languageExpression("script", script);
        }

        else if(lang.equalsIgnoreCase("groovy")) {
            return ExpressionBuilder.languageExpression("groovy", script);
        }

        else if(lang.equalsIgnoreCase("method")) {
            return ExpressionBuilder.beanExpression(script);
        }

        else if(lang.equalsIgnoreCase("header")) {
            return ExpressionBuilder.headerExpression(script);
        }

        else if(lang.equalsIgnoreCase("exchangeProperty")) {
            return ExpressionBuilder.exchangePropertyExpression(script);
        }

        else throw new RuntimeException("不支持的表达式语言：${props.lang}");
    }
}
