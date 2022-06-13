package me.towercraft.plugin.ioc.definition.registry;

import me.towercraft.plugin.ioc.definition.BeanDefinition;

import java.util.Collection;
import java.util.Set;

public interface BeanDefinitionRegistrar {

    void registerBeanDefinition(BeanDefinition beanDefinition);

    Collection<BeanDefinition> getBeanDefinitions(Class<?> clazz);

    Collection<BeanDefinition> getBeanDefinitions();

    BeanDefinition getBeanDefinition(String name);

    boolean containsBeanDefinition(BeanDefinition beanDefinition);

    Set<String> getBeanDefinitionNames();
}
