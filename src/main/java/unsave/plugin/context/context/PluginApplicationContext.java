package unsave.plugin.context.context;

import unsave.plugin.context.annotations.Component;
import unsave.plugin.context.annotations.Configuration;
import unsave.plugin.context.definition.BeanDefinition;
import unsave.plugin.context.definition.CustomBeanDefinition;
import unsave.plugin.context.definition.reader.*;
import unsave.plugin.context.definition.registry.BeanDefinitionRegistrar;
import unsave.plugin.context.definition.registry.DefaultBeanDefinitionRegistrar;
import unsave.plugin.context.factory.BeanFactory;
import unsave.plugin.context.factory.DefaultBeanFactory;
import unsave.plugin.context.utils.PackageScanner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PluginApplicationContext implements ApplicationContext {

    private final BeanDefinitionReader beanDefinitionReader;
    private BeanDefinitionRegistrar beanDefinitionRegistrar;
    private BeanFactory beanFactory;

    public PluginApplicationContext(Object plugin) {
        PackageScanner packageScanner = new PackageScanner();
        String packageName = plugin.getClass().getPackage().getName();

        List<BeanDefinitionReader> readers = new ArrayList<>();
        readers.add(new AnnotationConfigurationBeanDefinitionReader(packageScanner, packageName));
        readers.add(new SystemBeanDefinitionReader());

        List<Class<? extends Annotation>> annotations = new ArrayList<>();
        annotations.add(Configuration.class);
        annotations.add(Component.class);

        readers.add(new AnnotationBasedBeanDefinitionReader(annotations, packageScanner, packageName));

        beanDefinitionReader = new CompositeBeanDefinitionReader(readers);

        refresh(plugin);
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
    public void invokeDestroy() {
        beanFactory.invokeDestroy();
    }

    @Override
    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    @Override
    public void registerBean(String beanName, Object bean) {
        BeanDefinition beanDefinition = new CustomBeanDefinition(bean.getClass());
        beanDefinitionRegistrar.registerBeanDefinition(beanDefinition);
        beanFactory.registerBean(beanName == null ? beanDefinition.getName() : beanName, bean);
    }

    @Override
    public void refresh(Object plugin) {
        beanDefinitionRegistrar = new DefaultBeanDefinitionRegistrar();
        Set<BeanDefinition> beanDefinitions = beanDefinitionReader.getBeanDefinitions();

        beanDefinitions.forEach(
                bd -> beanDefinitionRegistrar.registerBeanDefinition(bd)
        );
        beanFactory = new DefaultBeanFactory(this, beanDefinitionRegistrar);

        registerBean(null, plugin);

        beanDefinitionRegistrar.getBeanDefinitionNames().forEach(beanFactory::getBean);
    }

    @Override
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
