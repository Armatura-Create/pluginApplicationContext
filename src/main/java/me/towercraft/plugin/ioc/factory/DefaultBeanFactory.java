package me.towercraft.plugin.ioc.factory;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.towercraft.plugin.ioc.annotations.Autowire;
import me.towercraft.plugin.ioc.annotations.PostConstruct;
import me.towercraft.plugin.ioc.definition.BeanDefinition;
import me.towercraft.plugin.ioc.definition.registry.BeanDefinitionRegistrar;
import me.towercraft.plugin.ioc.exceptions.MultiplyBeanException;
import me.towercraft.plugin.ioc.exceptions.MultiplyConstructorException;
import me.towercraft.plugin.ioc.exceptions.NotFoundBeanDefinitionException;
import me.towercraft.plugin.ioc.exceptions.WrongQualifierValueException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultBeanFactory implements BeanFactory {

    private final BeanDefinitionRegistrar beanDefinitionRegistrar;
    private final Map<String, Object> beans = new ConcurrentHashMap<>();

    @Override
    public <T> T getBean(Class<T> beanClass) {
        Collection<T> beans = getBeans(beanClass);
        if (beans.size() != 1)
            throw new RuntimeException(new MultiplyBeanException("Bean multiply or null [" + beanClass.getName() + "]"));
        return beans.stream().findFirst().get();
    }

    @Override
    public <T> Set<T> getBeans(Class<T> beanClass) {
        return beanDefinitionRegistrar.getBeanDefinitions(beanClass)
                .stream()
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean -> (T) bean)
                .collect(Collectors.toSet());
    }

    @Override
    public Object getBean(String beanName) {
        if (beans.containsKey(beanName))
            synchronized (this) {
                if (beans.containsKey(beanName))
                    return beans.get(beanName);
            }

        BeanDefinition beanDefinition = beanDefinitionRegistrar.getBeanDefinition(beanName);

        if (beanDefinition == null)
            throw new RuntimeException(new NotFoundBeanDefinitionException(beanName));

        Object bean = createBean(beanDefinition);

        beans.put(beanName, bean);

        return bean;
    }

    @Override
    public void registerBean(String beanName, Object bean) {
        if (beans.containsKey(beanName)) {
            throw new RuntimeException(new WrongQualifierValueException("BeanName is already exits [" + beanName + "}"));
        }

        beans.put(beanName, bean);
    }

    @Override
    public BeanFactory getBeanFactory() {
        return this;
    }

    private Object createBean(BeanDefinition beanDefinition) {
        return beanDefinition.getFactoryMethod() == null ?
                createBeanByConstructor(beanDefinition) :
                createBeanByFactoryMethod(beanDefinition);
    }

    private Object createBeanByFactoryMethod(BeanDefinition beanDefinition) {

        Method factoryMethod = beanDefinition.getFactoryMethod();
        Object factoryBean = getBean(factoryMethod.getDeclaringClass());

        Object[] values = resolveArguments(factoryMethod.getParameters());

        try {
            factoryMethod.setAccessible(true);
            return factoryMethod.invoke(factoryBean, values);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object createBeanByConstructor(BeanDefinition beanDefinition) {
        Constructor<?>[] constructors = beanDefinition.getBeanClass().getConstructors();

        if (constructors.length > 1) {
            throw new RuntimeException(
                    new MultiplyConstructorException("Bean must only one public constructor [" + beanDefinition.getBeanClass() + "]")
            );
        }

        Constructor<?> constructor = constructors[0];
        Object[] values = resolveArguments(constructor.getParameters());

        try {
            Object bean = constructor.newInstance(values);

            injectAutowire(bean, beanDefinition);

            invokePostConstruct(bean);

            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePostConstruct(Object bean) {

        Method[] declaredMethods = bean.getClass().getDeclaredMethods();
        Method method = null;

        for (Method declaredMethod : declaredMethods) {
            for (Annotation annotation : declaredMethod.getAnnotations()) {
                if (annotation.annotationType().isAssignableFrom(PostConstruct.class))
                    method = declaredMethod;
            }
        }

        if (method != null) {
            try {
                method.setAccessible(true);
                method.invoke(bean, resolveArguments(method.getParameters()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SneakyThrows
    private void injectAutowire(Object bean, BeanDefinition beanDefinition) {
        Field[] declaredFields = beanDefinition.getBeanClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (Arrays.stream(declaredField.getAnnotations())
                    .anyMatch(annotation -> annotation.annotationType().isAssignableFrom(Autowire.class))) {
                declaredField.setAccessible(true);
                declaredField.set(bean, getBean(declaredField.getType()));
            }
        }
    }

    private Object[] resolveArguments(Parameter[] parameters) {
        Object[] values = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            values[i] = resolveArgument(parameters[i]);
        }
        return values;
    }

    @SneakyThrows
    private Object resolveArgument(Parameter parameter) {

        if (parameter.getType().isAssignableFrom(List.class)) {
            Type type = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
            Class<?> aClass = Class.forName(type.getTypeName());
            return new ArrayList<>(getBeans(aClass));
        } else if (parameter.getType().isAssignableFrom(Set.class)) {
            Type type = ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
            Class<?> aClass = Class.forName(type.getTypeName());
            return new HashSet<>(getBeans(aClass));
        } else {
            return getBean(parameter.getType());
        }
    }
}
