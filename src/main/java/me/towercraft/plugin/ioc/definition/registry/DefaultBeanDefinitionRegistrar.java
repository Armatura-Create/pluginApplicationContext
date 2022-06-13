package me.towercraft.plugin.ioc.definition.registry;

import me.towercraft.plugin.ioc.definition.BeanDefinition;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultBeanDefinitionRegistrar implements BeanDefinitionRegistrar {

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    @Override
    public void registerBeanDefinition(BeanDefinition beanDefinition) {
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    }

    @Override
    public Collection<BeanDefinition> getBeanDefinitions(Class<?> clazz) {
        return getBeanDefinitions()
                .stream()
                .filter(bd -> {
                    boolean result = clazz.isAssignableFrom(bd.getBeanClass());

                    if (!result)
                        for (Class<?> anInterface : clazz.getInterfaces()) {
                            if (anInterface.isAssignableFrom(bd.getBeanClass()))
                                return true;
                        }
                    return result;
                }).collect(Collectors.toSet());
    }

    @Override
    public Collection<BeanDefinition> getBeanDefinitions() {
        return beanDefinitionMap.values();
    }

    @Override
    public BeanDefinition getBeanDefinition(String name) {
        return beanDefinitionMap.get(name);
    }

    @Override
    public boolean containsBeanDefinition(BeanDefinition beanDefinition) {
        return beanDefinitionMap.containsValue(beanDefinition);
    }

    @Override
    public Set<String> getBeanDefinitionNames() {
        return beanDefinitionMap.keySet();
    }
}
