/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.factmodel;

import java.io.Serializable;
import java.util.Map;

import org.drools.core.factmodel.traits.CoreWrapper;
import org.drools.core.factmodel.traits.Thing;
import org.drools.core.factmodel.traits.TraitFieldTMS;
import org.drools.core.factmodel.traits.Traitable;
import org.drools.core.factmodel.traits.TraitableBean;

@Traitable
public interface TraitableMap extends TraitableBean<Map,CoreWrapper<Map>>, Serializable, Map<String,Object>, CoreWrapper<Map> {

	Map<String, Object> _getDynamicProperties();

	void _setDynamicProperties( Map<String, Object> map );

	TraitFieldTMS _getFieldTMS();

    void _setFieldTMS( TraitFieldTMS __$$field_Tms$$ );

    void _setTraitMap(Map map);

    Map<String, Thing<Map>> _getTraitMap();

    void init( Map core );

    Map getCore();

}

