/**
 * $Id$
 * 
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 * 
 * Copyright (C) 2014-2018 the original authors or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package io.sarl.sre.tests.units.services.executor;

import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ QuietThreadExecutorPolicyTest.NotShutDownTest.class, QuietThreadExecutorPolicyTest.ShutDownTest.class })
@SarlSpecification("0.9")
@SarlElementType(10)
@SuppressWarnings("all")
public class QuietThreadExecutorPolicyTest {
  public class NotShutDownTest {
  }
  
  public class ShutDownTest {
  }
  
  @SyntheticMember
  public QuietThreadExecutorPolicyTest() {
    super();
  }
}
