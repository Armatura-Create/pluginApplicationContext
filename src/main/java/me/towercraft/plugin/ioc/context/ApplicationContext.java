package me.towercraft.plugin.ioc.context;

import me.towercraft.plugin.ioc.factory.BeanFactory;

public interface ApplicationContext extends BeanFactory {
    void refresh();
}
