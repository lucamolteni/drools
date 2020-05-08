package org.drools.core.reteoo;

import java.util.Optional;
import java.util.function.Function;

import org.drools.core.factmodel.traits.TraitCoreService;
import org.kie.api.internal.utils.ServiceRegistry;

// TODO LM move to service registry?
public class ServiceRegistryUtils {

    public static <T> Optional<T> fromTraitRegistry(Function<TraitCoreService, T> producer) {
        try {
            return Optional.ofNullable(ServiceRegistry.getInstance().get(TraitCoreService.class))
                    .map(producer);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public static <T> Optional<T> optionalService(Class<T> clazz) {
        try {
            return Optional.ofNullable(ServiceRegistry.getInstance().get(clazz));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
