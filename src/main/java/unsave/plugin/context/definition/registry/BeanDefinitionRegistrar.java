package unsave.plugin.context.definition.registry;

import unsave.plugin.context.definition.BeanDefinition;

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
