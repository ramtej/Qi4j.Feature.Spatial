/*
 * Copyright 2011 Marc Grue.
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
package org.qi4j.sample.dcicargo.sample_b.context.interaction.booking.exception;

import org.qi4j.sample.dcicargo.sample_b.data.structure.cargo.RouteSpecification;
import org.qi4j.sample.dcicargo.sample_b.data.structure.itinerary.Itinerary;

/**
 * Javadoc
 */
public class ChangeDestinationException extends Exception
{

    //    private RouteSpecification routeSpec;
//    private Itinerary itinerary;
    protected String msg = "";

    public ChangeDestinationException( String s, RouteSpecification routeSpec, Itinerary itinerary )
    {
        msg = s;
//        this.routeSpec = routeSpec;
//        this.itinerary = itinerary;
    }

    @Override
    public String getMessage()
    {
        return msg;
//        return "Couldn't change destination of cargo: " + msg;
    }

    public ChangeDestinationException( Throwable e )
    {
        super( e );
    }

    public ChangeDestinationException()
    {
        super();
    }

    public ChangeDestinationException( String s )
    {
        msg = s;
    }
}