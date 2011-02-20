/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.sample.rental.web.assembly;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;

public class RentalApplicationAssembler
    implements ApplicationAssembler
{
    private Application.Mode mode;

    public RentalApplicationAssembler( Application.Mode mode )
    {
        this.mode = mode;
    }

    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setMode( mode );
        LayerAssembly webLayer = assembly.layerAssembly( "WebLayer" );
        new PagesModule().assemble( webLayer.moduleAssembly( "PagesModule" ) );
        LayerAssembly domainLayer = assembly.layerAssembly( "DomainLayer" );
        new RentalModule().assemble( domainLayer.moduleAssembly( "RentalModule" ) );
        LayerAssembly infraLayer = assembly.layerAssembly( "InfraLayer" );
        new StorageModule().assemble( infraLayer.moduleAssembly( "StorageModule" ) );

        webLayer.uses( domainLayer );
        domainLayer.uses( infraLayer );
        return assembly;
    }
}