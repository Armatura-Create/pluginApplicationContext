package unsave.plugin.context.factory;

import lombok.SneakyThrows;
import unsave.plugin.context.annotations.PreDestroy;
import unsave.plugin.context.context.ApplicationContext;
import unsave.plugin.context.definition.BeanDefinition;
import unsave.plugin.context.definition.CustomBeanDefinition;
import unsave.plugin.context.definition.registry.BeanDefinitionRegistrar;
import unsave.plugin.context.exceptions.MultiplyBeanException;
import unsave.plugin.context.exceptions.MultiplyConstructorException;
import unsave.plugin.context.exceptions.NotFoundBeanDefinitionException;
import unsave.plugin.context.exceptions.WrongQualifierValueException;
import unsave.plugin.context.postprocess.BeanPostProcessor;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class DefaultBeanFactory implements BeanFactory {

    private final BeanDefinitionRegistrar beanDefinitionRegistrar;
    private final Map<String, Object> beans = new ConcurrentHashMap<>();

    public DefaultBeanFactory(ApplicationContext context, BeanDefinitionRegistrar beanDefinitionRegistrar) {
        this.beanDefinitionRegistrar = beanDefinitionRegistrar;
        BeanDefinition beanDefinition = new CustomBeanDefinition(context.getClass());
        this.beanDefinitionRegistrar.registerBeanDefinition(beanDefinition);
        this.beans.put(beanDefinition.getName(), context);
    }

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

        if (bean != null)
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

    private Object createBean(BeanDefinition beanDefinition) {
        Object bean = beanDefinition.getFactoryMethod() == null ?
                createBeanByConstructor(beanDefinition) :
                createBeanByFactoryMethod(beanDefinition);

        if (bean == null)
            return null;

        if (!BeanPostProcessor.class.isAssignableFrom(beanDefinition.getBeanClass())) {
            List<BeanPostProcessor> beanPostProcessors = getBeanPostProcessor();
            ApplicationContext context = getBean(ApplicationContext.class);

            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                bean = beanPostProcessor.postProcessorBeforeInitialisation(beanDefinition.getName(), bean, context);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                bean = beanPostProcessor.postProcessorAfterInitialisation(beanDefinition.getName(), bean, context);
            }
        }

        return bean;
    }

    private List<BeanPostProcessor> getBeanPostProcessor() {
        return beanDefinitionRegistrar.getBeanDefinitions()
                .stream()
                .filter(bd -> BeanPostProcessor.class.isAssignableFrom(bd.getBeanClass()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean -> (BeanPostProcessor) bean)
                .collect(Collectors.toList());
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

        if (constructors.length != 1) {
//            throw new RuntimeException(
//                    new MultiplyConstructorException("Bean must only one public constructor [" + beanDefinition.getBeanClass() + "]")
//            );
            return null;
        }

        Constructor<?> constructor = constructors[0];
        Object[] values = resolveArguments(constructor.getParameters());

        try {
            return constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
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
