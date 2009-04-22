/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.qi4j.scripting.common;

import java.io.PrintStream;
import org.qi4j.api.mixin.Mixins;

/**
 * JAVADOC
 */
@Mixins( ScriptUtilImpl.class )
public interface ScriptUtil
{
    PrintStream getOut();
}
