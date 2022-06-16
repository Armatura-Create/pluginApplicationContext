package unsave.plugin.context.definition.reader;

import lombok.RequiredArgsConstructor;
import unsave.plugin.context.annotations.Component;
import unsave.plugin.context.definition.ConstructorBeanDefinition;
import unsave.plugin.context.definition.BeanDefinition;
import unsave.plugin.context.utils.PackageScanner;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AnnotationComponentBeanDefinitionReader implements BeanDefinitionReader {
    private final PackageScanner packageScanner;
    private final String packageName;

    @Override
    public Set<BeanDefinition> getBeanDefinitions() {
        return packageScanner.findClasses(packageName)
                .stream()
                .filter(clazz -> {
                    boolean result = clazz.isAnnotationPresent(Component.class);
                    if (!result) {
                        for (Annotation classAnnotations : clazz.getAnnotations()) {
                            Annotation[] annotation2annotation = classAnnotations.annotationType().getAnnotations();
                            for (Annotation an : annotation2annotation) {
                                if (an.annotationType().isAssignableFrom(Component.class))
                                    return true;
                            }
                        }
                    }
                    return result;
                })
                .map(ConstructorBeanDefinition::new)
                .collect(Collectors.toSet());
    }
}
