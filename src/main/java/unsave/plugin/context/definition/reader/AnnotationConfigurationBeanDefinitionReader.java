package unsave.plugin.context.definition.reader;

import lombok.RequiredArgsConstructor;
import unsave.plugin.context.annotations.Bean;
import unsave.plugin.context.annotations.Configuration;
import unsave.plugin.context.definition.BeanDefinition;
import unsave.plugin.context.definition.MethodBeanDefinition;
import unsave.plugin.context.utils.PackageScanner;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AnnotationConfigurationBeanDefinitionReader implements BeanDefinitionReader {

    private final PackageScanner packageScanner;
    private final String packageName;

    @Override
    public Set<BeanDefinition> getBeanDefinitions() {
        return packageScanner.findClasses(packageName)
                .stream()
                .filter(clazz -> clazz.isAnnotationPresent(Configuration.class))
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .map(MethodBeanDefinition::new)
                .collect(Collectors.toSet());
    }
}
