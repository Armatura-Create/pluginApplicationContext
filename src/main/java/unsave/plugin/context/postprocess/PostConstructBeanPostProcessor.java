package unsave.plugin.context.postprocess;

import lombok.SneakyThrows;
import unsave.plugin.context.annotations.PostConstruct;
import unsave.plugin.context.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostConstructBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessorBeforeInitialisation(Object bean, ApplicationContext context) {
        return bean;
    }

    @Override
    @SneakyThrows
    public Object postProcessorAfterInitialisation(Object bean, ApplicationContext context) {

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

                Parameter[] parameters = method.getParameters();

                Object[] values = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].getType().isAssignableFrom(List.class)) {
                        Type type = ((ParameterizedType) parameters[i].getParameterizedType()).getActualTypeArguments()[0];
                        Class<?> aClass = Class.forName(type.getTypeName());
                        return new ArrayList<>(context.getBeans(aClass));
                    } else if (parameters[i].getType().isAssignableFrom(Set.class)) {
                        Type type = ((ParameterizedType) parameters[i].getParameterizedType()).getActualTypeArguments()[0];
                        Class<?> aClass = Class.forName(type.getTypeName());
                        return new HashSet<>(context.getBeans(aClass));
                    } else {
                        values[i] = context.getBean(parameters[i].getType());
                    }
                }

                method.invoke(bean, values);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return bean;
    }
}
