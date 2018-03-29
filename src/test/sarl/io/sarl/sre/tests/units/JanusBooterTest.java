/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014-2015 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.sre.tests.units;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.arakhne.afc.vmutil.FileSystem;
import org.arakhne.afc.vmutil.Resources;
import org.eclipse.xtext.xbase.lib.CollectionExtensions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Strings;

import io.sarl.lang.SARLVersion;
import io.sarl.sre.Exiter;
import io.sarl.sre.JanusBooter;
import io.sarl.sre.JanusConfig;
import io.sarl.sre.JanusVersion;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusBooterTest extends AbstractBooterTest<JanusBooter> {

	@Override
	protected JanusBooter newBooter() {
		return new JanusBooter();
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		System.clearProperty("io.janusproject.tests.MY_PROPERTY_0");
		System.clearProperty("io.janusproject.tests.MY_PROPERTY_1");
		System.clearProperty("io.janusproject.tests.MY_PROPERTY_2");
	}
	
	@Override
	public void tearDown() {
		super.tearDown();
		System.clearProperty("io.janusproject.tests.MY_PROPERTY_0");
		System.clearProperty("io.janusproject.tests.MY_PROPERTY_1");
		System.clearProperty("io.janusproject.tests.MY_PROPERTY_2");
	}
	
	private void verifyCli(String... text) throws IOException {
		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		verify(this.logger, times(text.length)).println(arg.capture());
		List<String> list = arg.getAllValues();
		assertEquals("invalid list size", text.length, list.size());
		for (int i = 0; i < text.length; ++i) {
			assertEquals("invalid element #" + i, i + ": " + text[i], list.get(i));
		}
	}

	@Test
	public void getOptions_all() throws Exception {
		Options janusOptions = this.booter.getOptions();
		DefaultParser parser = new DefaultParser();
		List<String> optNames = new ArrayList<>();
		CollectionExtensions.addAll(optNames, IterableExtensions.map(janusOptions.getOptions(), (it) -> {
			String o = it.getOpt();
			if (Strings.isNullOrEmpty(o)) {
				o = it.getLongOpt();
			}
			return o;
		}));
		
		optNames.remove("h");
		CommandLine cmd = parser.parse(janusOptions, args(""));
		assertFalse(cmd.hasOption('h'));
		cmd = parser.parse(janusOptions, args("-h"));
		assertTrue(cmd.hasOption('h'));
		cmd = parser.parse(janusOptions, args("-help"));
		assertTrue(cmd.hasOption('h'));
		cmd = parser.parse(janusOptions, args("--help"));
		assertTrue(cmd.hasOption('h'));

		optNames.remove("f");
		cmd = parser.parse(janusOptions, args("-f", "thefile"));
		assertTrue(cmd.hasOption('f'));
		assertEquals("thefile", cmd.getOptionValue('f'));
		cmd = parser.parse(janusOptions, args("-file", "thefile"));
		assertTrue(cmd.hasOption('f'));
		assertEquals("thefile", cmd.getOptionValue('f'));
		cmd = parser.parse(janusOptions, args("--file", "thefile"));
		assertTrue(cmd.hasOption('f'));
		assertEquals("thefile", cmd.getOptionValue('f'));

		optNames.remove("B");
		cmd = parser.parse(janusOptions, args("-B", "uid"));
		assertTrue(cmd.hasOption('B'));
		assertEquals("uid", cmd.getArgs()[0]);
		cmd = parser.parse(janusOptions, args("-bootid", "uid"));
		assertTrue(cmd.hasOption('B'));
		assertEquals("uid", cmd.getArgs()[0]);
		cmd = parser.parse(janusOptions, args("--bootid", "uid"));
		assertTrue(cmd.hasOption('B'));
		assertEquals("uid", cmd.getArgs()[0]);

		optNames.remove("R");
		cmd = parser.parse(janusOptions, args("-R", "uid"));
		assertTrue(cmd.hasOption('R'));
		assertEquals("uid", cmd.getArgs()[0]);
		cmd = parser.parse(janusOptions, args("-randomid", "uid"));
		assertTrue(cmd.hasOption('R'));
		assertEquals("uid", cmd.getArgs()[0]);
		cmd = parser.parse(janusOptions, args("--randomid", "uid"));
		assertTrue(cmd.hasOption('R'));
		assertEquals("uid", cmd.getArgs()[0]);

		optNames.remove("W");
		cmd = parser.parse(janusOptions, args("-W", "uid"));
		assertTrue(cmd.hasOption('W'));
		assertEquals("uid", cmd.getArgs()[0]);
		cmd = parser.parse(janusOptions, args("-worldid", "uid"));
		assertTrue(cmd.hasOption('W'));
		assertEquals("uid", cmd.getArgs()[0]);
		cmd = parser.parse(janusOptions, args("--worldid", "uid"));
		assertTrue(cmd.hasOption('W'));
		assertEquals("uid", cmd.getArgs()[0]);

		optNames.remove("D");
		cmd = parser.parse(janusOptions, args("-D", "name=value"));
		assertTrue(cmd.hasOption('D'));
		assertArrayEquals(new String[] { "name", "value" }, cmd.getOptionValues('D'));
		cmd = parser.parse(janusOptions, args("-Dname=value"));
		assertTrue(cmd.hasOption('D'));
		assertArrayEquals(new String[] { "name", "value" }, cmd.getOptionValues('D'));
		cmd = parser.parse(janusOptions, args("-D", "name", "value"));
		assertTrue(cmd.hasOption('D'));
		assertArrayEquals(new String[] { "name", "value" }, cmd.getOptionValues('D'));
		try {
			parser.parse(janusOptions, args("-D", "name"));
			fail("Expecting failure");
		} catch (Throwable exception) {
			//
		}
		try {
			parser.parse(janusOptions, args("-D"));
			fail("Expecting failure");
		} catch (Throwable exception) {
			//
		}
		
		optNames.remove("cp");
		cmd = parser.parse(janusOptions, args("-cp", "/mypath1"));
		assertTrue(cmd.hasOption("cp"));
		assertArrayEquals(new String[] { "/mypath1" }, cmd.getOptionValues("cp"));
		cmd = parser.parse(janusOptions, args("--classpath", "/mypath2"));
		assertTrue(cmd.hasOption("cp"));
		assertArrayEquals(new String[] { "/mypath2" }, cmd.getOptionValues("cp"));

		optNames.remove("version");
		cmd = parser.parse(janusOptions, args("-version"));
		assertTrue(cmd.hasOption("version"));

		optNames.remove("e");
		cmd = parser.parse(janusOptions, args("-e"));
		assertTrue(cmd.hasOption('e'));
		cmd = parser.parse(janusOptions, args("-embedded"));
		assertTrue(cmd.hasOption('e'));
		
		optNames.remove("nologo");
		cmd = parser.parse(janusOptions, args("-nologo"));
		assertTrue(cmd.hasOption("nologo"));

		optNames.remove("o");
		cmd = parser.parse(janusOptions, args("-o"));
		assertTrue(cmd.hasOption('o'));
		cmd = parser.parse(janusOptions, args("-offline"));
		assertTrue(cmd.hasOption('o'));

		optNames.remove("q");
		cmd = parser.parse(janusOptions, args("-q"));
		assertTrue(cmd.hasOption('q'));
		cmd = parser.parse(janusOptions, args("-quiet"));
		assertTrue(cmd.hasOption('q'));

		optNames.remove("v");
		cmd = parser.parse(janusOptions, args("-v"));
		assertTrue(cmd.hasOption('v'));
		cmd = parser.parse(janusOptions, args("-verbose"));
		assertTrue(cmd.hasOption('v'));
		cmd = parser.parse(janusOptions, args("-vvvv"));
		assertTrue(cmd.hasOption('v'));
		cmd = parser.parse(janusOptions, args("-vvvv", "-v"));
		assertTrue(cmd.hasOption('v'));

		optNames.remove("l");
		cmd = parser.parse(janusOptions, args("-l", "1"));
		assertTrue(cmd.hasOption('l'));
		assertArrayEquals(new String[] { "1" }, cmd.getOptionValues("l"));
		cmd = parser.parse(janusOptions, args("-log", "1"));
		assertTrue(cmd.hasOption('l'));
		assertArrayEquals(new String[] { "1" }, cmd.getOptionValues("l"));
		cmd = parser.parse(janusOptions, args("-l", "error"));
		assertTrue(cmd.hasOption('l'));
		assertArrayEquals(new String[] { "error" }, cmd.getOptionValues("l"));
		cmd = parser.parse(janusOptions, args("-log", "warning"));
		assertTrue(cmd.hasOption('l'));
		assertArrayEquals(new String[] { "warning" }, cmd.getOptionValues("l"));
		try {
			parser.parse(janusOptions, args("-l"));
			fail("Expecting failure");
		} catch (Throwable exception) {
			//
		}
		try {
			parser.parse(janusOptions, args("-log"));
			fail("Expecting failure");
		} catch (Throwable exception) {
			//
		}

		optNames.remove("s");
		cmd = parser.parse(janusOptions, args("-s"));
		assertTrue(cmd.hasOption('s'));
		cmd = parser.parse(janusOptions, args("-showdefaults"));
		assertTrue(cmd.hasOption('s'));

		optNames.remove("showclasspath");
		cmd = parser.parse(janusOptions, args("-showclasspath"));
		assertTrue(cmd.hasOption("showclasspath"));

		optNames.remove("cli");
		cmd = parser.parse(janusOptions, args("-cli"));
		assertTrue(cmd.hasOption("cli"));
		
		// Ensure that all options are tested
		assertContains(optNames);
	}

	@Test
	public void getOptions_agentArg() throws Exception {
		Options janusOptions = this.booter.getOptions();
		DefaultParser parser = new DefaultParser();
		
		CommandLine cmd = parser.parse(janusOptions, null);
		assertEquals(0, cmd.getArgs().length);

		cmd = parser.parse(janusOptions, args("-h", "main.Agent"));
		assertEquals(1, cmd.getArgs().length);
		assertEquals("main.Agent", cmd.getArgs()[0]);

		cmd = parser.parse(janusOptions, args("-h", "main.Agent", "12"));
		assertEquals(2, cmd.getArgs().length);
		assertEquals("main.Agent", cmd.getArgs()[0]);
		assertEquals("12", cmd.getArgs()[1]);

		cmd = parser.parse(janusOptions, args("-h", "main.Agent", "12", "hola"));
		assertEquals(3, cmd.getArgs().length);
		assertEquals("main.Agent", cmd.getArgs()[0]);
		assertEquals("12", cmd.getArgs()[1]);
		assertEquals("hola", cmd.getArgs()[2]);
	}
	
	@Test
	public void option_invalidOption() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-B", "-x"));
		assertNull(freeArgs);
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.VERBOSE_LEVEL_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		verify(this.logger, times(2)).write(any(byte[].class), anyInt(), anyInt());
		verify(this.logger, times(2)).flush();
		verify(this.logger, times(1)).close();
		verifyNoMoreInteractions(this.logger);
		verify(this.exiter, only()).exit();
	}

	@Test
	public void option_noOptionGiven() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_B_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-B", "--", "-x", "-y"));
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertTrueProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertFalseProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_B_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-B", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-B", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_R_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-R", "--", "-x", "-y"));
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertFalseProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertTrueProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_R_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-R", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-R", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_W_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-W", "--", "-x", "-y"));
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertFalseProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertFalseProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_W_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-W", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-W", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_nologo_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-nologo", "--", "-x", "-y"));
		assertFalseProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_nologo_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-nologo", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-nologo", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_o_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-o", "--", "-x", "-y"));
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertTrueProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_o_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-o", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-o", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_e_valid() {
		Exiter old = this.booter.getExiter();
		Object[] freeArgs = this.booter.parseCommandLine(args("-e", "--", "-x", "-y"));
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
		assertNotSame(old, this.booter.getExiter());
	}

	@Test
	public void option_e_asArg() {
		Exiter old = this.booter.getExiter();
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-e", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-e", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
		assertSame(old, this.booter.getExiter());
	}

	@Test
	public void option_s_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-s", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertNullProperty(JanusConfig.VERBOSE_LEVEL_NAME);
		assertNull(freeArgs);
		verify(this.logger, times(1)).write(any(byte[].class), anyInt(), anyInt());
		verify(this.logger, times(3)).flush();
		verify(this.logger, times(1)).close();
		verifyNoMoreInteractions(this.logger);
		verify(this.exiter, only()).exit();
	}

	@Test
	public void option_s_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-s", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-s", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_cli_valid() throws IOException {
		Object[] freeArgs = this.booter.parseCommandLine(args("arg1", "--cli", "--", "-x", "arg2", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertNullProperty(JanusConfig.VERBOSE_LEVEL_NAME);
		assertNull(freeArgs);
		verifyCli("arg1", "--cli", "--", "-x", "arg2", "-y");
		verify(this.logger, times(1)).close();
		verify(this.logger, times(1)).flush();
		verifyNoMoreInteractions(this.logger);
		verify(this.exiter, only()).exit();
	}

	@Test
	public void option_cli_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("arg1", "--", "--cli", "-x", "arg2", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "arg1", "--cli", "-x", "arg2", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_f_valid() throws Exception {
		URL propertyURL = Resources.getResource("io/janusproject/sre/tests/Test1.properties");
		assumeNotNull(propertyURL);
		File propertyFile = FileSystem.convertURLToFile(propertyURL);
		assumeNotNull(propertyFile);
		//
		Object[] freeArgs = this.booter.parseCommandLine(args("-f", propertyFile.getAbsolutePath(), "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertProperty("io.janusproject.tests.MY_PROPERTY_0", "my value 0");
		assertProperty("io.janusproject.tests.MY_PROPERTY_1", "my value 1");
		assertProperty("io.janusproject.tests.MY_PROPERTY_2", "my value 2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_f_asArg() throws Exception {
		URL propertyURL = Resources.getResource("io/janusproject/sre/tests/Test1.properties");
		assumeNotNull(propertyURL);
		File propertyFile = FileSystem.convertURLToFile(propertyURL);
		assumeNotNull(propertyFile);
		//
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-f", propertyFile.getAbsolutePath(), "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-f", propertyFile.getAbsolutePath(), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_D_valid_withSeparation() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-D", "io.janusproject.tests.MY_PROPERTY_1=the value", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertProperty("io.janusproject.tests.MY_PROPERTY_1", "the value");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_D_valid_withoutSeparation() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-Dio.janusproject.tests.MY_PROPERTY_1=the value", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertProperty("io.janusproject.tests.MY_PROPERTY_1", "the value");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_D_asArg_withSeparation() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-D", "io.janusproject.var0=value1", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-D", "io.janusproject.var0=value1", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_D_asArg_withoutSeparation() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-Dio.janusproject.var0=value1", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-Dio.janusproject.var0=value1", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_q_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-q", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "2");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_q_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-q", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-q", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_qq_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-q", "-q", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "1");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_v_valid() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-v", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "4");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_v_asArg() {
		Object[] freeArgs = this.booter.parseCommandLine(args("--", "-v", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "3");
		assertContains(Arrays.asList(freeArgs), "-v", "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_vv() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-v", "-v", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "5");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_vqv() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-v", "-q", "-v", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "4");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_qvq() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-q", "-v", "-q", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertNullProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "2");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_qqq_forNoLogo() {
		Object[] freeArgs = this.booter.parseCommandLine(args("-q", "-q", "-q", "--", "-x", "-y"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		assertFalseProperty(JanusConfig.JANUS_LOGO_SHOW_NAME);
		assertNullProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		assertNullProperty(JanusConfig.OFFLINE);
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_0");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_1");
		assertNullProperty("io.janusproject.tests.MY_PROPERTY_2");
		assertProperty(JanusConfig.VERBOSE_LEVEL_NAME, "0");
		assertContains(Arrays.asList(freeArgs), "-x", "-y");
		verifyZeroInteractions(this.logger);
		verifyZeroInteractions(this.exiter);
	}

	@Test
	public void option_version() {
		this.booter.parseCommandLine(args("-version"));
		// The properties are null since resetProperties() is invoked for resetting the properties in
		// the start-up function inherited from AbstractJanusTest
		ArgumentCaptor<byte[]> array = ArgumentCaptor.forClass(byte[].class);
		ArgumentCaptor<Integer> offset = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> length = ArgumentCaptor.forClass(Integer.class);
		verify(this.logger, times(1)).write(array.capture(), offset.capture(), length.capture());
		final String message = new String(array.getValue(), offset.getValue(), length.getValue());
		assertEquals("Janus: " + JanusVersion.JANUS_RELEASE_VERSION + getLineSeparator() + "SARL specification: "
				+ SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING + getLineSeparator(), message);
		verify(this.logger, times(1)).flush();
		verify(this.logger, times(1)).close();
		verifyNoMoreInteractions(this.logger);
		verify(this.exiter, only()).exit();
	}

}
