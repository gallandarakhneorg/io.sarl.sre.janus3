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
package io.sarl.sre.tests.units.services.context;

import com.google.inject.Injector;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.sre.services.context.ContextFactory;
import io.sarl.sre.tests.units.services.context.AbstractContextServiceTest;
import io.sarl.tests.api.Nullable;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SarlSpecification("0.9")
@SarlElementType(10)
@SuppressWarnings("all")
public abstract class AbstractInjectionBasedContextServiceTest<T/*  extends AbstractInjectionBasedContextService */> extends AbstractContextServiceTest<T> {
  @Nullable
  private Injector injector;
  
  @Nullable
  private ContextFactory contextFactory;
  
  /* @Override
   */public void setUp() {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field mock is undefined for the type Class<Injector>"
      + "\nThe method or field mock is undefined for the type Class<ContextFactory>"
      + "\nThe method or field context is undefined for the type AbstractInjectionBasedContextServiceTest<T>"
      + "\nThe method or field context is undefined for the type AbstractInjectionBasedContextServiceTest<T>"
      + "\nThe method or field context is undefined for the type AbstractInjectionBasedContextServiceTest<T>"
      + "\nThe method or field setUp is undefined for the type AbstractContextServiceTest<T>"
      + "\nThe method or field service is undefined for the type AbstractInjectionBasedContextServiceTest<T>"
      + "\nThe method or field service is undefined for the type AbstractInjectionBasedContextServiceTest<T>"
      + "\nID cannot be resolved"
      + "\nisRootContext cannot be resolved"
      + "\ninjector cannot be resolved"
      + "\ncontextFactory cannot be resolved");
  }
  
  @Override
  @Pure
  @SyntheticMember
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }
  
  @Override
  @Pure
  @SyntheticMember
  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
  
  @SyntheticMember
  public AbstractInjectionBasedContextServiceTest() {
    super();
  }
}
