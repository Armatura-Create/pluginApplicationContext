package me.towercraft.plugin.ioc.definition.reader;

import lombok.RequiredArgsConstructor;
import me.towercraft.plugin.ioc.definition.BeanDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CompositeBeanDefinitionReader implements BeanDefinitionReader{
    private final List<BeanDefinitionReader> beanDefinitionReaders;

    @Override
    public Set<BeanDefinition> getBeanDefinitions() {
        return beanDefinitionReaders
                .stream()
                .map(BeanDefinitionReader::getBeanDefinitions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
