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

package org.qi4j.api.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Thread-associated composites. This is basically a ThreadLocal which maintains a reference
 * to a Composite instance for each thread. This can be used to implement various context
 * patterns without having to pass the context explicitly as a parameter to methods.
 */
public class CompositeContext<T extends Composite>
    extends ThreadLocal<T>
{
    private final TransientBuilder<T> builder;

    public CompositeContext( TransientBuilder<T> builder )
    {
        this.builder = builder;
    }

    @Override protected T initialValue()
    {
        return builder.newInstance();
    }

    @SuppressWarnings( "unchecked" )
    public T proxy()
    {
        Composite composite = get();

        return (T) Proxy.newProxyInstance( composite.getClass().getClassLoader(), new Class[]{ composite.type() }, new ContextInvocationhandler() );
    }

    private class ContextInvocationhandler
        implements InvocationHandler
    {

        public Object invoke( Object object, Method method, Object[] objects ) throws Throwable
        {
            try
            {
                return method.invoke( get(), objects );
            }
            catch( InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
    }
}
