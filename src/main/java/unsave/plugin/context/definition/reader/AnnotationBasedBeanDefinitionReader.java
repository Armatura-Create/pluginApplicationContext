package unsave.plugin.context.definition.reader;

import lombok.RequiredArgsConstructor;
import unsave.plugin.context.definition.ConstructorBeanDefinition;
import unsave.plugin.context.definition.BeanDefinition;
import unsave.plugin.context.utils.PackageScanner;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AnnotationBasedBeanDefinitionReader implements BeanDefinitionReader {

    private final List<Class<? extends Annotation>> annotations;
    private final PackageScanner packageScanner;
    private final String packageName;

    @Override
    public Set<BeanDefinition> getBeanDefinitions() {
        return packageScanner.findClasses(packageName)
                .stream()
                .filter(clazz -> annotations.stream().anyMatch(a -> {
                    boolean result = clazz.isAnnotationPresent(a);
                    if (!result) {
                        for (Annotation classAnnotations : clazz.getAnnotations()) {
                            Annotation[] annotation2annotation = classAnnotations.annotationType().getAnnotations();
                            for (Annotation an : annotation2annotation) {
                                if (an.annotationType().isAssignableFrom(a))
                                    return true;
                            }
                        }
                    }
                    return result;
                }))
                .map(ConstructorBeanDefinition::new)
                .collect(Collectors.toSet());
    }
}