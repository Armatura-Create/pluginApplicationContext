package unsave.plugin.context.factory;

import java.util.Set;

public interface BeanFactory {
    <T> T getBean(Class<T> beanClass);
    <T> Set<T> getBeans(Class<T> beanClass);
    Object getBean(String beanName);

    void registerBean(String beanName, Object bean);

}
