package unsave.plugin.context.definition.reader;

import unsave.plugin.context.definition.BeanDefinition;
import unsave.plugin.context.definition.ConstructorBeanDefinition;
import unsave.plugin.context.postprocess.AutowireAnnotationBeanPostProcessor;
import unsave.plugin.context.postprocess.PostConstructBeanPostProcessor;

import java.util.HashSet;
import java.util.Set;

public class SystemBeanDefinitionReader implements BeanDefinitionReader{
    @Override
    public Set<BeanDefinition> getBeanDefinitions() {

        Set<BeanDefinition> postProcessors = new HashSet<>();
        postProcessors.add(new ConstructorBeanDefinition(AutowireAnnotationBeanPostProcessor.class));
        postProcessors.add(new ConstructorBeanDefinition(PostConstructBeanPostProcessor.class));

        return postProcessors;
    }
}
