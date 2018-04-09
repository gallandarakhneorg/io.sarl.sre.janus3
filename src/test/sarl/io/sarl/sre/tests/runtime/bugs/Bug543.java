/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2018 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package io.sarl.sre.tests.runtime.bugs;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

import io.sarl.core.Lifecycle;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.UnimplementedCapacityException;
import io.sarl.lang.util.ClearableReference;
import io.sarl.sre.tests.testutils.AbstractSreRunTest;

/**
 * Unit test for the issue #543: Incomplete reset of the hidden buffers to skills.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/sarl/sarl/issues/543
 */
@SuppressWarnings("all")
public class Bug543 extends AbstractSreRunTest {

	@Test
	public void setSkill() throws Exception {
		runSre(TAgent1.class, false);
		assertEquals(2, getNumberOfResults());
		assertEquals("1", getResult(String.class, 0));
		assertEquals("2", getResult(String.class, 1));
	}

	@Test
	public void clearSkill() throws Exception {
		runSre(TAgent2.class, false);
		assertEquals(2, getNumberOfResults());
		assertEquals("1", getResult(String.class, 0));
		UnimplementedCapacityException ex = getResult(UnimplementedCapacityException.class, 1);
		assertNotNull(ex);
		assertEquals(C1.class, ex.getUnimplementedCapacity());
	}

	@Test
	public void doubleHiddenGetters() throws Exception {
		runSre(TAgent3.class, false);
		assertEquals(1, getNumberOfResults());
		assertEquals(Boolean.TRUE, getResult(Boolean.class, 0));
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static interface C1 extends Capacity {
		void fct();
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class S1 extends Skill implements C1 {
		private final Procedure1<String> proc;
		public S1(Procedure1<String> fct) {
			this.proc = fct;
		}
		@Override
		public void fct() {
			this.proc.apply("1");
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class S2 extends Skill implements C1 {
		private final Procedure1<String> proc;
		public S2(Procedure1<String> fct) {
			this.proc = fct;
		}
		@Override
		public void fct() {
			this.proc.apply("2");
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class TAgent1 extends TestingAgent {

		public TAgent1(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		private transient ClearableReference<Skill> $CAPACITY_USE$C1;

		private C1 $CAPACITY_USE$C1$CALLER() {
			if (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) {
				this.$CAPACITY_USE$C1 = $getSkill(C1.class);
			}
			return $castSkill(C1.class, this.$CAPACITY_USE$C1);
		}

		@Override
		protected boolean runAgentTest() {
			setSkill(new S1((it) -> addResult(it)), C1.class);
			//
			C1 _$CAPACITY_USE$C1$CALLER = this.$castSkill(C1.class, (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) ? (this.$CAPACITY_USE$C1 = $getSkill(C1.class)) : this.$CAPACITY_USE$C1);
			_$CAPACITY_USE$C1$CALLER.fct();
			//
			setSkill(new S2((it) -> addResult(it)), C1.class);
			//
			C1 _$CAPACITY_USE$C1$CALLER1 = this.$castSkill(C1.class, (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) ? (this.$CAPACITY_USE$C1 = $getSkill(C1.class)) : this.$CAPACITY_USE$C1);
			_$CAPACITY_USE$C1$CALLER1.fct();
			//
			getSkill(Lifecycle.class).killMe();
			return false;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class TAgent2 extends TestingAgent {

		public TAgent2(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		private transient ClearableReference<Skill> $CAPACITY_USE$C1;

		private C1 $CAPACITY_USE$C1$CALLER() {
			if (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) {
				this.$CAPACITY_USE$C1 = $getSkill(C1.class);
			}
			return $castSkill(C1.class, this.$CAPACITY_USE$C1);
		}

		@Override
		protected boolean runAgentTest() {
			setSkill(new S1((it) -> addResult(it)), C1.class);
			//
			C1 _$CAPACITY_USE$C1$CALLER = this.$castSkill(C1.class, (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) ? (this.$CAPACITY_USE$C1 = $getSkill(C1.class)) : this.$CAPACITY_USE$C1);
			_$CAPACITY_USE$C1$CALLER.fct();
			//
			clearSkill(C1.class);
			//
			C1 _$CAPACITY_USE$C1$CALLER1 = this.$castSkill(C1.class, (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) ? (this.$CAPACITY_USE$C1 = $getSkill(C1.class)) : this.$CAPACITY_USE$C1);
			//
			getSkill(Lifecycle.class).killMe();
			return false;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class TAgent3 extends TestingAgent {

		public TAgent3(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		private transient ClearableReference<Skill> $CAPACITY_USE$C1;

		private C1 $CAPACITY_USE$C1$CALLER() {
			if (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) {
				this.$CAPACITY_USE$C1 = $getSkill(C1.class);
			}
			return $castSkill(C1.class, this.$CAPACITY_USE$C1);
		}

		@Override
		protected boolean runAgentTest() {
			setSkill(new S1((it) -> addResult(it)), C1.class);
			//
			C1 _$CAPACITY_USE$C1$CALLER = this.$castSkill(C1.class, (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) ? (this.$CAPACITY_USE$C1 = $getSkill(C1.class)) : this.$CAPACITY_USE$C1);
			C1 _$CAPACITY_USE$C1$CALLER1 = this.$castSkill(C1.class, (this.$CAPACITY_USE$C1 == null || this.$CAPACITY_USE$C1.get() == null) ? (this.$CAPACITY_USE$C1 = $getSkill(C1.class)) : this.$CAPACITY_USE$C1);
			addResult(Boolean.valueOf(_$CAPACITY_USE$C1$CALLER == _$CAPACITY_USE$C1$CALLER1));
			//
			getSkill(Lifecycle.class).killMe();
			return false;
		}

	}

}
