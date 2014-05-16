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

package com.redhat.thermostat.thread.model;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class ThreadStateTest {

    @Test
    public void testEqualsAndHashCode() throws Exception {

        ThreadHeader header1 = new ThreadHeader("1234");
        header1.setReferenceID("vm42");
        header1.setThreadName("main");
        header1.setThreadId(1);

        ThreadState state1 = new ThreadState("1234", header1);
        state1.setProbeStartTime(0l);
        state1.setProbeEndTime(1l);
        state1.setState("NEW");

        ThreadState state2 = new ThreadState("1234", header1);
        state2.setProbeStartTime(0l);
        state2.setProbeEndTime(1l);
        state2.setState("NEW");

        assertEquals(state1, state2);
        assertEquals(state1.hashCode(), state2.hashCode());

        ThreadHeader header2 = new ThreadHeader("12344");
        state2.setHeader(header2);

        assertFalse(state1.equals(state2));
        assertFalse(state1.hashCode() == state2.hashCode());

        state2.setHeader(header1);
        state2.setProbeStartTime(1l);
        state2.setProbeEndTime(1l);
        state2.setState("NEW");

        assertFalse(state1.equals(state2));
        assertFalse(state1.hashCode() == state2.hashCode());
    }
}
