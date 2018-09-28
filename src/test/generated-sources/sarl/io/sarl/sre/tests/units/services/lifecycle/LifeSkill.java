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
import io.sarl.core.Lifecycle;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Skill;
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
class LifeSkill extends Skill implements Lifecycle {
  private final AtomicInteger orderSource;
  
  public Integer installOrder;
  
  public Integer uninstallOrderPre;
  
  public Integer uninstallOrderPost;
  
  public LifeSkill(final AtomicInteger s) {
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
  
  public void killMe() {
  }
  
  public UUID spawn(final Class<? extends Agent> agentType, final Object... params) {
    return null;
  }
  
  public Iterable<UUID> spawn(final int nbAgents, final Class<? extends Agent> agentType, final Object... params) {
    return null;
  }
  
  public UUID spawnInContext(final Class<? extends Agent> agentClass, final AgentContext context, final Object... params) {
    return null;
  }
  
  public Iterable<UUID> spawnInContext(final int nbAgents, final Class<? extends Agent> agentClass, final AgentContext context, final Object... params) {
    return null;
  }
  
  public UUID spawnInContextWithID(final Class<? extends Agent> agentClass, final UUID agentID, final AgentContext context, final Object... params) {
    return null;
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
