/*
 * Copyright 2012-2014 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.client.cli.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jline.TerminalFactory;
import jline.TerminalFactory.Flavor;
import jline.TerminalFactory.Type;
import jline.UnixTerminal;
import jline.console.history.PersistentHistory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.redhat.thermostat.client.cli.internal.ShellCommand.HistoryProvider;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.common.cli.Arguments;
import com.redhat.thermostat.common.cli.CommandContext;
import com.redhat.thermostat.common.cli.CommandException;
import com.redhat.thermostat.common.cli.SimpleArguments;
import com.redhat.thermostat.launcher.Launcher;
import com.redhat.thermostat.test.TestCommandContextFactory;

public class ShellCommandTest {

    static private final String VERSION = "Thermostat some version";
    static private final String VERSION_OUTPUT = VERSION + "\n";

    private ShellCommand cmd;

    private BundleContext bundleContext;
    private HistoryProvider historyProvider;
    private Version version;

    @Before
    public void setUp() {
        bundleContext = mock(BundleContext.class);
        historyProvider = mock(HistoryProvider.class);
        version = mock(Version.class);
        when(version.getVersionInfo()).thenReturn(VERSION);

        cmd = new ShellCommand(bundleContext, version, historyProvider);
    }

    @After
    public void tearDown() {
        cmd = null;
        bundleContext = null;
        TerminalFactory.registerFlavor(Flavor.UNIX, UnixTerminal.class);
        TerminalFactory.reset();
    }

    @Test
    public void testBasic() throws CommandException {
        ServiceReference ref = mock(ServiceReference.class);
        
        when(bundleContext.getServiceReference(Launcher.class.getName())).thenReturn(ref);
        Launcher launcher = mock(Launcher.class);
        when(bundleContext.getService(ref)).thenReturn(launcher);
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory(bundleContext);
        ctxFactory.setInput("help\nexit\n");
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);
        verify(launcher).run(new String[]{"help"}, true);
    }

    @Test
    public void testQuitAlsoExits() throws CommandException {
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory();
        ctxFactory.setInput("quit\n");
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);
        assertEquals(VERSION_OUTPUT + "Thermostat > quit\n", ctxFactory.getOutput());
        assertEquals("", ctxFactory.getError());
    }

    @Test
    public void testQAlsoExits() throws CommandException {
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory();
        ctxFactory.setInput("q\n");
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);
        assertEquals(VERSION_OUTPUT + "Thermostat > q\n", ctxFactory.getOutput());
        assertEquals("", ctxFactory.getError());
    }

    @Test
    public void testEofExits() throws CommandException {
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory();
        ctxFactory.setInput("\u0004"); // EOF
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);
        assertEquals(VERSION_OUTPUT + "Thermostat > ", ctxFactory.getOutput());
        assertEquals("", ctxFactory.getError());
    }

    @Test
    public void testDoNothingWithoutInput() throws CommandException {
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory();
        ctxFactory.setInput("\nexit\n");
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);
        assertEquals(VERSION_OUTPUT + "Thermostat > \nThermostat > exit\n", ctxFactory.getOutput());
    }

    @Test
    public void testHistoryIsQueried() throws CommandException {
        PersistentHistory history = mock(PersistentHistory.class);
        when(history.previous()).thenReturn(true);
        when(history.current()).thenReturn("old-history-value");

        when(historyProvider.get()).thenReturn(history);

        ServiceReference ref = mock(ServiceReference.class);
        
        when(bundleContext.getServiceReference(Launcher.class.getName())).thenReturn(ref);
        Launcher launcher = mock(Launcher.class);
        when(bundleContext.getService(ref)).thenReturn(launcher);
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory(bundleContext);

        // "\u001b[A" is the escape code for up-arrow. use xxd -p to generate
        ctxFactory.setInput("\u001b[A\nexit\n");
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);

        assertEquals(VERSION_OUTPUT + "Thermostat > old-history-value\nThermostat > exit\n", ctxFactory.getOutput());
        assertEquals("", ctxFactory.getError());

        verify(launcher).run(new String[] {"old-history-value"}, true);
    }

    @Test
    public void testHistoryIsUpdated() throws CommandException, IOException {
        PersistentHistory mockHistory = mock(PersistentHistory.class);
        when(historyProvider.get()).thenReturn(mockHistory);

        ServiceReference ref = mock(ServiceReference.class);
        when(bundleContext.getServiceReference(Launcher.class.getName())).thenReturn(ref);
        Launcher launcher = mock(Launcher.class);
        when(bundleContext.getService(ref)).thenReturn(launcher);
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory(bundleContext);
        
        ctxFactory.setInput("add-to-history\nexit\n");
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);

        verify(launcher).run(new String[] {"add-to-history"}, true);
        verify(mockHistory).add("add-to-history");
        verify(mockHistory).flush();

        assertEquals(VERSION_OUTPUT + "Thermostat > add-to-history\nThermostat > exit\n", ctxFactory.getOutput());
        assertEquals("", ctxFactory.getError());
    }

    @Test(expected=CommandException.class)
    public void testIOException() throws CommandException {
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory();
        ctxFactory.setInputThrowsException(new IOException());
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);
    }

    @Test(expected=CommandException.class)
    public void testTerminalRestoreException() throws CommandException {
        TerminalFactory.configure(Type.UNIX);
        TerminalFactory.registerFlavor(Flavor.UNIX, TestTerminal.class);
        TestCommandContextFactory ctxFactory = new TestCommandContextFactory();
        ctxFactory.setInputThrowsException(new IOException());
        Arguments args = new SimpleArguments();
        CommandContext ctx = ctxFactory.createContext(args);
        cmd.run(ctx);
    }

    @Test
    public void testStorageRequired() {
        assertFalse(cmd.isStorageRequired());
    }
}

