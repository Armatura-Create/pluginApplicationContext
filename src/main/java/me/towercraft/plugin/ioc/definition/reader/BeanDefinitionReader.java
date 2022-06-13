package me.towercraft.plugin.ioc.definition.reader;

import me.towercraft.plugin.ioc.definition.BeanDefinition;

import java.util.Set;

public interface BeanDefinitionReader {
    Set<BeanDefinition> getBeanDefinitions();

}
