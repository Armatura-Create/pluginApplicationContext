package unsave.plugin.context.utils;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

public class PackageScanner {
    public Set<Class<?>> findClasses(String packageName) {
        Reflections reflections = new Reflections(packageName, Scanners.SubTypes.filterResultsBy(s -> true));
        return reflections.getSubTypesOf(Object.class);
    }
}
