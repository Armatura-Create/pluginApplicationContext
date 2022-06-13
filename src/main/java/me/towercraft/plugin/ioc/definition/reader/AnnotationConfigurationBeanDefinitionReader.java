package me.towercraft.plugin.ioc.definition.reader;

import lombok.RequiredArgsConstructor;
import me.towercraft.plugin.ioc.annotations.Bean;
import me.towercraft.plugin.ioc.annotations.Configuration;
import me.towercraft.plugin.ioc.definition.BeanDefinition;
import me.towercraft.plugin.ioc.definition.MethodBeanDefinition;
import me.towercraft.plugin.ioc.utils.PackageScanner;

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
