package me.towercraft.plugin.ioc.definition;

import me.towercraft.plugin.ioc.annotations.Qualifier;
import me.towercraft.plugin.ioc.exceptions.MultiplyAnnotationTypeException;
import me.towercraft.plugin.ioc.exceptions.WrongQualifierValueException;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodBeanDefinition implements BeanDefinition {

    private final String beanName;
    private final Class<?> beanClass;

    private final Method beanMethod;

    public MethodBeanDefinition(Method beanMethod) {
        this.beanMethod = beanMethod;
        this.beanClass = beanMethod.getReturnType();

        Annotation[] annotations = beanMethod.getAnnotations();

        List<Qualifier> qualifierAnnotations = Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().isAssignableFrom(Qualifier.class))
                .map(annotation -> (Qualifier) annotation)
                .collect(Collectors.toList());

        if (qualifierAnnotations.size() > 1)
            throw new RuntimeException(new MultiplyAnnotationTypeException(Qualifier.class.getSimpleName()));
        if (qualifierAnnotations.size() == 1) {
            Qualifier annotation = qualifierAnnotations.get(0);
            String value = annotation.value();
            if (value.isEmpty())
                throw new RuntimeException(new WrongQualifierValueException(annotation.getClass().toString()));
            beanName = Introspector.decapitalize(annotation.value());
        } else
            this.beanName = Introspector.decapitalize(beanMethod.getName()
                    .replace("get", "")
                    .replace("Get", ""));
    }

    @Override
    public String getName() {
        return beanName;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Method getFactoryMethod() {
        return beanMethod;
    }
}
