package unsave.plugin.context.postprocess;

import lombok.SneakyThrows;
import unsave.plugin.context.annotations.PostConstruct;
import unsave.plugin.context.context.ApplicationContext;
import unsave.plugin.context.exceptions.MultiplyAnnotationTypeException;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PostConstructBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Method> classes = new ConcurrentHashMap<>();

    @Override
    public Object postProcessorBeforeInitialisation(String beanName, Object bean, ApplicationContext context) {
        Class<?> aClass = bean.getClass();

        List<Method> methods = Arrays.stream(aClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(PostConstruct.class))
                .collect(Collectors.toList());

        if (methods.size() > 1)
            throw new RuntimeException(new MultiplyAnnotationTypeException(PostConstruct.class.getSimpleName()));

        if (methods.size() == 1)
            classes.put(beanName, methods.get(0));
        return bean;
    }

    @Override
    @SneakyThrows
    public Object postProcessorAfterInitialisation(String beanName, Object bean, ApplicationContext context) {

        if (classes.containsKey(beanName)) {
            Method method = classes.get(beanName);
            method.setAccessible(true);
            method.invoke(bean);
        }

        return bean;
    }
}
