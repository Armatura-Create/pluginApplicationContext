package me.towercraft.plugin.ioc.exceptions;

public class NotFoundBeanDefinitionException extends Exception{
    public NotFoundBeanDefinitionException(String message) {
        super(message);
    }
}
