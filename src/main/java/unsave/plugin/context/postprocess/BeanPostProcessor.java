package unsave.plugin.context.postprocess;

import unsave.plugin.context.context.ApplicationContext;

public interface BeanPostProcessor {
    Object postProcessorBeforeInitialisation(String beanName, Object bean, ApplicationContext context);
    Object postProcessorAfterInitialisation(String beanName, Object bean, ApplicationContext context);
}
