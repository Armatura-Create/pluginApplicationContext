package unsave.plugin.context.definition;

import java.lang.reflect.Method;

public interface BeanDefinition {
    String getName();
    Class<?> getBeanClass();
    default Method getFactoryMethod() {
        return null;
    }
}
