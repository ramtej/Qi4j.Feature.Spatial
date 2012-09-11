/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.entitystore.riak;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.RiakRetryFailedException;
import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.raw.http.HTTPClientConfig;
import com.basho.riak.client.raw.http.HTTPClusterConfig;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.ServiceActivation;
import org.qi4j.io.Input;
import org.qi4j.io.Output;
import org.qi4j.io.Receiver;
import org.qi4j.io.Sender;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.helpers.MapEntityStore;
import org.qi4j.spi.entitystore.helpers.MapEntityStoreMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RiakHttpMapEntityStoreMixin
        extends MapEntityStoreMixin
        implements ServiceActivation, RiakHttpMapEntityStoreService, MapEntityStore
{

    private static final Logger LOGGER = LoggerFactory.getLogger( "org.qi4j.entitystore.riak" );

    private static final int DEFAULT_MAX_CONNECTIONS = 50;

    private static final String DEFAULT_HOST = "http://localhost:8098/riak";

    /* package */ static final String DEFAULT_BUCKET_KEY = "qi4j:entities";

    @This
    private Configuration<RiakHttpEntityStoreConfiguration> configuration;

    private IRiakClient riakClient;

    private String bucketKey;

    public void activateService()
            throws Exception
    {
        configuration.refresh();
        RiakHttpEntityStoreConfiguration config = configuration.get();

        int maxConnections = config.maxConnections().get() == null ? DEFAULT_MAX_CONNECTIONS : config.maxConnections().get();
        int timeoutMillis = config.timeout().get();
        List<String> hosts = config.hosts().get();
        if ( hosts.isEmpty() ) {
            hosts.add( DEFAULT_HOST );
        }
        bucketKey = config.bucket().get() == null ? DEFAULT_BUCKET_KEY : config.bucket().get();

        HTTPClusterConfig httpClusterConfig = new HTTPClusterConfig( maxConnections );
        for ( String host : hosts ) {
            HTTPClientConfig clientConfig = new HTTPClientConfig.Builder().withTimeout( timeoutMillis ).withUrl( host ).build();
            //HTTPClientConfig clientConfig = new HTTPClientConfig.Builder().withUrl( host ).build();
            httpClusterConfig.addClient( clientConfig );
        }
        riakClient = RiakFactory.newClient( httpClusterConfig );

        if ( !riakClient.listBuckets().contains( bucketKey ) ) {
            riakClient.createBucket( bucketKey ).execute();
        }
    }

    public void passivateService()
            throws Exception
    {
        riakClient.shutdown();
        riakClient = null;
        bucketKey = null;
    }

    public IRiakClient riakClient()
    {
        return riakClient;
    }

    public String bucket()
    {
        return bucketKey;
    }

    @Override
    public Reader get( EntityReference entityReference )
            throws EntityStoreException
    {
        try {

            Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();
            IRiakObject entity = bucket.fetch( entityReference.identity() ).execute();
            if ( entity == null ) {
                throw new EntityNotFoundException( entityReference );
            }
            String jsonState = entity.getValueAsString();
            return new StringReader( jsonState );

        } catch ( RiakRetryFailedException ex ) {
            throw new EntityStoreException( "Unable to get Entity " + entityReference.identity(), ex );
        }
    }

    @Override
    public void applyChanges( MapChanges changes )
            throws IOException
    {
        try {
            final Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();

            changes.visitMap( new MapChanger()
            {

                @Override
                public Writer newEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                        throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                                throws IOException
                        {
                            try {
                                super.close();
                                bucket.store( ref.identity(), toString() ).execute();
                            } catch ( RiakException ex ) {
                                throw new EntityStoreException( "Unable to apply entity change: newEntity", ex );
                            }
                        }

                    };
                }

                @Override
                public Writer updateEntity( final EntityReference ref, EntityDescriptor entityDescriptor )
                        throws IOException
                {
                    return new StringWriter( 1000 )
                    {

                        @Override
                        public void close()
                                throws IOException
                        {
                            try {
                                super.close();
                                IRiakObject entity = bucket.fetch( ref.identity() ).execute();
                                if ( entity == null ) {
                                    throw new EntityNotFoundException( ref );
                                }
                                bucket.store( ref.identity(), toString() ).execute();
                            } catch ( RiakException ex ) {
                                throw new EntityStoreException( "Unable to apply entity change: updateEntity", ex );
                            }
                        }

                    };
                }

                @Override
                public void removeEntity( EntityReference ref, EntityDescriptor entityDescriptor )
                        throws EntityNotFoundException
                {
                    try {
                        IRiakObject entity = bucket.fetch( ref.identity() ).execute();
                        if ( entity == null ) {
                            throw new EntityNotFoundException( ref );
                        }
                        bucket.delete( ref.identity() ).execute();
                    } catch ( RiakException ex ) {
                        throw new EntityStoreException( "Unable to apply entity change: removeEntity", ex );
                    }
                }

            } );

        } catch ( RiakRetryFailedException ex ) {
            throw new EntityStoreException( "Unable to apply entity changes.", ex );
        }
    }

    @Override
    public Input<Reader, IOException> entityStates()
    {
        return new Input<Reader, IOException>()
        {

            @Override
            public <ReceiverThrowableType extends Throwable> void transferTo( Output<? super Reader, ReceiverThrowableType> output )
                    throws IOException, ReceiverThrowableType
            {
                output.receiveFrom( new Sender<Reader, IOException>()
                {

                    @Override
                    public <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super Reader, ReceiverThrowableType> receiver )
                            throws ReceiverThrowableType, IOException
                    {
                        try {
                            final Bucket bucket = riakClient.fetchBucket( bucketKey ).execute();
                            for ( String key : bucket.keys() ) {
                                String jsonState = bucket.fetch( key ).execute().getValueAsString();
                                receiver.receive( new StringReader( jsonState ) );
                            }
                        } catch ( RiakException ex ) {
                            throw new EntityStoreException( "Unable to apply entity changes.", ex );
                        }
                    }

                } );
            }

        };
    }

}