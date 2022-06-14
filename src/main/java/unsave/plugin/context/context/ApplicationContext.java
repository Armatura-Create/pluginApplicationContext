package unsave.plugin.context.context;

import unsave.plugin.context.factory.BeanFactory;

public interface ApplicationContext extends BeanFactory {
    void refresh(Object plugin);
}
