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
package org.qi4j.sample.dcicargo.sample_b.data.structure.tracking;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Matches;

/**
 * TrackingId
 *
 * A TrackingId uniquely identifies a particular cargo.
 *
 * Suggested constraints:
 * - starts with a letter [a-zA-Z] or digit
 * - then allows underscore/dash
 * - is minimum 3 characters long
 * - is maximum 30 characters long.
 */
public interface TrackingId
    extends ValueComposite
{
    @Matches( "[a-zA-Z0-9]{1}[a-zA-Z0-9_-]{2,29}" )
    Property<String> id();
}
