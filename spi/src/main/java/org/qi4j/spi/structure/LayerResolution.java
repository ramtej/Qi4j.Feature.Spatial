/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public final class LayerResolution
    implements Serializable
{
    private LayerModel layerModel;
    private ApplicationModel applicationModel;
    private Iterable<ModuleResolution> moduleResolutions;
    private List<LayerResolution> uses = new ArrayList<LayerResolution>();

    public LayerResolution( LayerModel layerModel, ApplicationModel applicationModel, Iterable<ModuleResolution> modules, List<LayerResolution> uses )
    {
        this.layerModel = layerModel;
        this.applicationModel = applicationModel;
        this.moduleResolutions = modules;
        this.uses = uses;
    }

    public LayerModel getLayerModel()
    {
        return layerModel;
    }

    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }

    public Iterable<ModuleResolution> getModuleResolutions()
    {
        return moduleResolutions;
    }

    public Iterable<LayerResolution> getUses()
    {
        return uses;
    }

    public ServiceDescriptor getServiceDescriptor( Class serviceType )
    {
        // Check all Modules
        for( ModuleResolution moduleResolution : moduleResolutions )
        {
            ServiceDescriptor instanceProvider = moduleResolution.getServiceDescriptor( serviceType );
            if( instanceProvider != null )
            {
                return instanceProvider;
            }
        }

        // No provider found for this type
        return null;
    }
}
