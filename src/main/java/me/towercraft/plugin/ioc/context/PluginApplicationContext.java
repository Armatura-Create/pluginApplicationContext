package me.towercraft.plugin.ioc.context;

import me.towercraft.plugin.ioc.annotations.Component;
import me.towercraft.plugin.ioc.annotations.Configuration;
import me.towercraft.plugin.ioc.definition.CustomBeanDefinition;
import me.towercraft.plugin.ioc.definition.BeanDefinition;
import me.towercraft.plugin.ioc.definition.reader.*;
import me.towercraft.plugin.ioc.definition.registry.BeanDefinitionRegistrar;
import me.towercraft.plugin.ioc.definition.registry.DefaultBeanDefinitionRegistrar;
import me.towercraft.plugin.ioc.factory.BeanFactory;
import me.towercraft.plugin.ioc.factory.DefaultBeanFactory;
import me.towercraft.plugin.ioc.utils.PackageScanner;

import java.beans.Introspector;
import java.util.List;
import java.util.Set;

public class PluginApplicationContext implements ApplicationContext {

    private final BeanDefinitionReader beanDefinitionReader;
    private BeanDefinitionRegistrar beanDefinitionRegistrar;
    private BeanFactory beanFactory;

    public PluginApplicationContext(Class<?> mainClass) {
        PackageScanner packageScanner = new PackageScanner();
        String packageName = mainClass.getPackageName();

        beanDefinitionReader = new CompositeBeanDefinitionReader(
                List.of(
                        new AnnotationConfigurationBeanDefinitionReader(packageScanner, packageName),
                        new AnnotationBasedBeanDefinitionReader(
                                List.of(
                                        Component.class,
                                        Configuration.class
                                ),
                                packageScanner, packageName)
                )
        );

        refresh();
    }

    @Override
    public <T> T getBean(Class<T> beanClass) {
        return beanFactory.getBean(beanClass);
    }

    @Override
    public <T> Set<T> getBeans(Class<T> beanClass) {
        return beanFactory.getBeans(beanClass);
    }

    @Override
    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    @Override
    public void registerBean(String beanName, Object bean) {
        beanDefinitionRegistrar.registerBeanDefinition(new CustomBeanDefinition(bean.getClass()));
        beanFactory.registerBean(beanName, bean);
    }

    @Override
    public void refresh() {
        beanDefinitionRegistrar = new DefaultBeanDefinitionRegistrar();
        Set<BeanDefinition> beanDefinitions = beanDefinitionReader.getBeanDefinitions();

        beanDefinitions.forEach(
                bd -> beanDefinitionRegistrar.registerBeanDefinition(bd)
        );
        beanFactory = new DefaultBeanFactory(beanDefinitionRegistrar);

        registerBean(Introspector.decapitalize(this.getClass().getSimpleName()), this);

        beanDefinitionRegistrar.getBeanDefinitionNames().forEach(beanFactory::getBean);
    }

    @Override
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
