/*
 * Copyright 2012-2016 Red Hat, Inc.
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

package com.redhat.thermostat.service.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.redhat.thermostat.service.process.UNIXProcessHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.service.internal.UnixProcessUtilities;
import com.redhat.thermostat.service.process.UNIXSignal;

public class UnixProcessUtilitiesTest {

    private static final boolean IS_UNIX = !System.getProperty("os.name").contains("Windows");

    private BufferedReader reader;
    private BufferedReader emptyReader;
    
    private List<String> processArguments = new ArrayList<>();
    private UNIXProcessHandler process;
    
    @Before
    public void setUp() {

        String data = IS_UNIX ? "123 fluff" : "headerline\r\n\"fluff.exe\",\"1868\",\"Console\",\"1\",\"25,952 K\"";
        reader = new BufferedReader(new StringReader(data));
        emptyReader = new BufferedReader(new StringReader(""));

        processArguments.clear();

        if (IS_UNIX) {
            process = new UnixProcessUtilities() {
                @Override
                public Process createAndRunProcess(List<String> args)
                        throws IOException {
                    processArguments.addAll(args);
                    return null;
                }

                @Override
                void exec(String command) {
                    processArguments.add(command);
                }

                public java.io.BufferedReader getProcessOutput(Process process) {
                    return reader;
                }
            };
        } else {
            process = new UnixProcessUtilities.WindowsProcessUtilities() {
                @Override
                public Process createAndRunProcess(List<String> args)
                        throws IOException {
                    processArguments.addAll(args);
                    return null;
                }

                @Override
                void exec(String command) {
                    processArguments.add(command);
                }

                public java.io.BufferedReader getProcessOutput(Process process) {
                    return reader;
                }
            };
        }
    }
    
    @Test
    public void sendKillSignalTest() {
        
        process.sendSignal(12345, UNIXSignal.KILL);
        
        Assert.assertTrue(processArguments.contains("kill -s kill 12345"));
        Assert.assertEquals(1, processArguments.size());
    }
    
    @Test
    public void sendTermSignalTest() {

        process.sendSignal(12345, UNIXSignal.TERM);

        Assert.assertTrue(processArguments.contains("kill -s term 12345"));
        Assert.assertEquals(1, processArguments.size());
    }

    @Test
    public void getProcessName() {

        String result = process.getProcessName(12345);
        Assert.assertEquals("fluff", result);

        if (IS_UNIX) {
            Assert.assertTrue(processArguments.contains("12345"));
            Assert.assertTrue(processArguments.contains("ps"));
            Assert.assertTrue(processArguments.contains("--no-heading"));
            Assert.assertTrue(processArguments.contains("-p"));
        }
        else {
            Assert.assertTrue(processArguments.contains("\"PID eq 12345\""));
            Assert.assertTrue(processArguments.contains("tasklist"));
        }
    }
    
    @Test
    public void getProcessNameNoOutput() {

        // redefine, since we need an empty reader
        final UNIXProcessHandler process;
        if (IS_UNIX) {
            process = new UnixProcessUtilities() {
                @Override
                public Process createAndRunProcess(List<String> args)
                        throws IOException {
                    processArguments.addAll(args);
                    return null;
                }

                public java.io.BufferedReader getProcessOutput(Process process) {
                    return emptyReader;
                }
            };
        }
        else {
            process = new UnixProcessUtilities.WindowsProcessUtilities() {
                @Override
                public Process createAndRunProcess(List<String> args)
                        throws IOException {
                    processArguments.addAll(args);
                    return null;
                }

                public java.io.BufferedReader getProcessOutput(Process process) {
                    return emptyReader;
                }
            };
        }
        
        String result = process.getProcessName(12345);
        Assert.assertNull(result);
    }    
}

