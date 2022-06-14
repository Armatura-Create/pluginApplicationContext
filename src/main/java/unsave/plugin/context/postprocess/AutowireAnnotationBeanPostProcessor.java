package unsave.plugin.context.postprocess;

import unsave.plugin.context.annotations.Autowire;
import unsave.plugin.context.context.ApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutowireAnnotationBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessorBeforeInitialisation(String beanName, Object bean, ApplicationContext context) {

        Class<?> clazz = bean.getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {

            if (!declaredField.isAnnotationPresent(Autowire.class)) continue;

            try {
                Object value;
                if (declaredField.getType().isAssignableFrom(List.class)) {
                    Type type = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                    Class<?> aClass = Class.forName(type.getTypeName());
                    value = new ArrayList<>(context.getBeans(aClass));
                } else if (declaredField.getType().isAssignableFrom(Set.class)) {
                    Type type = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                    Class<?> aClass = Class.forName(type.getTypeName());
                    value = new HashSet<>(context.getBeans(aClass));
                } else
                    value = context.getBean(declaredField.getType());

                declaredField.setAccessible(true);
                declaredField.set(bean, value);
            } catch (Exception e) {
                throw new RuntimeException(e); //TODO Custom Exception
            }

        }

        return bean;
    }

    @Override
    public Object postProcessorAfterInitialisation(String beanName, Object bean, ApplicationContext context) {
        return bean;
    }
}
