package me.towercraft.plugin.ioc.factory;

import java.util.Set;

public interface BeanFactory {
    <T> T getBean(Class<T> beanClass);
    <T> Set<T> getBeans(Class<T> beanClass);
    void invokeDestroy();
    Object getBean(String beanName);

    void registerBean(String beanName, Object bean);

    BeanFactory getBeanFactory();
}
