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
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.sre.tests.units.services.lifecycle;

import com.google.common.base.Objects;
import io.sarl.core.InnerContextAccess;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedIterable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("potential_field_synchronization_problem")
@SarlSpecification("0.9")
@SarlElementType(21)
class InnerSkill extends Skill implements InnerContextAccess {
  private final AtomicInteger orderSource;
  
  public Integer installOrder;
  
  public Integer uninstallOrderPre;
  
  public Integer uninstallOrderPost;
  
  public InnerSkill(final AtomicInteger s) {
    this.orderSource = s;
  }
  
  protected void install() {
    this.installOrder = Integer.valueOf(this.orderSource.getAndIncrement());
  }
  
  protected void uninstall(final Skill.UninstallationStage stage) {
    boolean _equals = Objects.equal(stage, Skill.UninstallationStage.PRE_DESTROY_EVENT);
    if (_equals) {
      this.uninstallOrderPre = Integer.valueOf(this.orderSource.getAndIncrement());
    } else {
      this.uninstallOrderPost = Integer.valueOf(this.orderSource.getAndIncrement());
    }
  }
  
  @Pure
  public int getInstallationOrder() {
    return 0;
  }
  
  @Pure
  public AgentContext getInnerContext() {
    return null;
  }
  
  @Pure
  public int getMemberAgentCount() {
    return 0;
  }
  
  @Pure
  public SynchronizedIterable<UUID> getMemberAgents() {
    return null;
  }
  
  @Pure
  public boolean hasMemberAgent() {
    return false;
  }
  
  @Pure
  public boolean isInInnerDefaultSpace(final Event event) {
    return false;
  }
  
  @Pure
  public boolean isInnerDefaultSpace(final Space space) {
    return false;
  }
  
  @Pure
  public boolean isInnerDefaultSpace(final SpaceID spaceID) {
    return false;
  }
  
  @Pure
  public boolean isInnerDefaultSpace(final UUID spaceID) {
    return false;
  }
  
  @Override
  @Pure
  @SyntheticMember
  public boolean equals(final Object obj) {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid supertype. Expecting a class."
      + "\nInvalid supertype. Expecting a class.");
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    throw new Error("Unresolved compilation problems:"
      + "\nInvalid supertype. Expecting a class.");
  }
}
