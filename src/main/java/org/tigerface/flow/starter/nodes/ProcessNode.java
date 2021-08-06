package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.tigerface.flow.starter.service.PluginManager;

import java.util.Map;

@Slf4j
public class ProcessNode extends FlowNode implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String script = (String) (props.get("processor") != null ? props.get("processor") : props.get("script"));

        if (script != null && script.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class processorClazz = groovyClassLoader.parseClass(script);

            try {
                Object processor = processorClazz.newInstance();
                PluginManager.autowireBean(processor);

                rd.process((Processor) processor);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析动态 Processor 脚本失败");
            }
        } else {
            throw new RuntimeException("Process 脚本不能为空");
        }
        log.info("创建 process 节点");

        return rd;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
