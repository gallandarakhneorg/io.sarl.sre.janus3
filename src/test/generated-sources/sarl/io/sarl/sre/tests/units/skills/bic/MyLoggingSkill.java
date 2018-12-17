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
package io.sarl.sre.tests.units.skills.bic;

import io.sarl.core.Logging;
import io.sarl.lang.annotation.DefaultValue;
import io.sarl.lang.annotation.DefaultValueSource;
import io.sarl.lang.annotation.SarlElementType;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.annotation.SyntheticMember;
import io.sarl.lang.core.Skill;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SarlSpecification("0.10")
@SarlElementType(22)
@SuppressWarnings("all")
class MyLoggingSkill extends Skill implements Logging {
  private final Logger logger;
  
  public MyLoggingSkill(final Logger logger) {
    this.logger = logger;
  }
  
  @Override
  public void setLoggingName(final String name) {
  }
  
  @Override
  public void println(final Object message) {
  }
  
  @Override
  @DefaultValueSource
  public void error(final Object message, @DefaultValue("io.sarl.core.Logging#ERROR_0") final Throwable exception, final Object... parameters) {
  }
  
  @Override
  @DefaultValueSource
  public void warning(final Object message, @DefaultValue("io.sarl.core.Logging#WARNING_0") final Throwable exception, final Object... parameters) {
  }
  
  @Override
  public void info(final Object message, final Object... parameters) {
  }
  
  @Override
  public void debug(final Object message, final Object... parameters) {
  }
  
  @Override
  @Pure
  public boolean isErrorLogEnabled() {
    return false;
  }
  
  @Override
  @Pure
  public boolean isWarningLogEnabled() {
    return false;
  }
  
  @Override
  @Pure
  public boolean isInfoLogEnabled() {
    return false;
  }
  
  @Override
  @Pure
  public boolean isDebugLogEnabled() {
    return false;
  }
  
  @Override
  @Pure
  public int getLogLevel() {
    return 0;
  }
  
  @Override
  public void setLogLevel(final int level) {
  }
  
  @Override
  @Pure
  public Logger getLogger() {
    return this.logger;
  }
  
  @Override
  public void error(final Supplier<String> messageProvider) {
  }
  
  @Override
  public void warning(final Supplier<String> messageProvider) {
  }
  
  @Override
  public void info(final Supplier<String> messageProvider) {
  }
  
  @Override
  public void debug(final Supplier<String> messageProvider) {
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
}
