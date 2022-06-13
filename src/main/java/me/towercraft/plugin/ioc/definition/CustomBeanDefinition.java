package me.towercraft.plugin.ioc.definition;

import lombok.RequiredArgsConstructor;

import java.beans.Introspector;

@RequiredArgsConstructor
public class CustomBeanDefinition implements BeanDefinition {

    private final Class<?> beanClass;

    @Override
    public String getName() {
        return Introspector.decapitalize(beanClass.getSimpleName());
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }
}
