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
package io.sarl.sre.tests.runtime.bugs.bug543;

import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SarlSpecification("0.9")
@SarlElementType(21)
@SuppressWarnings("all")
class S2 extends Skill implements Capacity {
  private final Procedure1<? super String> proc;
  
  public S2(final Procedure1<? super String> fct) {
    this.proc = fct;
  }
  
  @Override
  public void fct() {
    this.proc.apply("2");
  }
  
  @Override
  @Pure
  @SyntheticMember
  public boolean equals(final Object obj) {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid implemented type: \'void\'. Only subtypes of \'io.sarl.lang.core.Capacity\' are allowed for \'S2\'."
      + "\nInvalid implemented type: \'void\'. Only subtypes of \'io.sarl.lang.core.Capacity\' are allowed for \'S2\'."
      + "\nMissing implemented type \'io.sarl.lang.core.Capacity\' for \'S2\'."
      + "\nMissing implemented type \'io.sarl.lang.core.Capacity\' for \'S2\'.");
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid implemented type: \'void\'. Only subtypes of \'io.sarl.lang.core.Capacity\' are allowed for \'S2\'."
      + "\nMissing implemented type \'io.sarl.lang.core.Capacity\' for \'S2\'.");
  }
}
