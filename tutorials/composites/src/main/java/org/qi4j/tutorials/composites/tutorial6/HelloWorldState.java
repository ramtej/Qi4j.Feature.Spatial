package org.qi4j.tutorials.composites.tutorial6;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.constraints.annotation.NotEmpty;

// START SNIPPET: solution

/**
 * This interface contains only the state
 * of the HelloWorld object.
 * <p/>
 * The parameters are declared as @NotEmpty, so the client cannot pass in empty strings
 * as values.
 */
@Mixins( HelloWorldStateMixin.class )
public interface HelloWorldState
{
    void setPhrase( @NotEmpty String phrase )
        throws IllegalArgumentException;

    String getPhrase();

    void setName( @NotEmpty String name )
        throws IllegalArgumentException;

    String getName();
}
// END SNIPPET: solution
