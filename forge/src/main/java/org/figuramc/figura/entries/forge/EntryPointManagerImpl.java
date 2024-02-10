package org.figuramc.figura.entries.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.entries.annotations.*;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.*;

public class EntryPointManagerImpl {
    public static final Map<String, Class<?>> nameToAnnotationClass = new HashMap<>();

    static {
        nameToAnnotationClass.put("figura_api", FiguraAPIPlugin.class);
        nameToAnnotationClass.put("figura_permissions", FiguraPermissionsPlugin.class);
        nameToAnnotationClass.put("figura_screen", FiguraScreenPlugin.class);
        nameToAnnotationClass.put("figura_vanilla_part", FiguraVanillaPartPlugin.class);
        nameToAnnotationClass.put("figura_event", FiguraEventPlugin.class);
    }

    public static <T> Set<T> load(String name, Class<T> clazz) {
        Set<T> ret = new HashSet<>();
        Class<?> annotationClass = nameToAnnotationClass.get(name);
        if (annotationClass != null) {
            Type annotationType = Type.getType(annotationClass);
            List<ModFileScanData> modFileScanDataList = ModList.get().getAllScanData();
            Set<String> pluginClassNames = new LinkedHashSet<>();
            for (ModFileScanData scanData : modFileScanDataList) {
                Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
                for (ModFileScanData.AnnotationData a : annotations) {
                    if (Objects.equals(a.annotationType(), annotationType)) {
                        String memberName = a.memberName();
                        pluginClassNames.add(memberName);
                    }
                }
            }
            for (String className : pluginClassNames) {
                try {
                    Class<?> asmClass = Class.forName(className);
                    Class<? extends T> asmInstanceClass = asmClass.asSubclass(clazz);
                    Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
                    T instance = constructor.newInstance();
                    ret.add(instance);
                } catch (ReflectiveOperationException | LinkageError e) {
                    FiguraMod.LOGGER.error("Failed to load entrypoint: {}", className, e);
                }
            }
        }
        return ret;
    }
}
