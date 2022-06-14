package unsave.plugin.context.postprocess;

import unsave.plugin.context.context.ApplicationContext;

public interface BeanPostProcessor {
    Object postProcessorBeforeInitialisation(Object bean, ApplicationContext context);
    Object postProcessorAfterInitialisation(Object bean, ApplicationContext context);
}
