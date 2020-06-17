/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
