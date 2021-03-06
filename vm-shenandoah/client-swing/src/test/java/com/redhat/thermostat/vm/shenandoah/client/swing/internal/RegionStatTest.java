/*
 * Copyright 2012-2017 Red Hat, Inc.
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

package com.redhat.thermostat.vm.shenandoah.client.swing.internal;

import org.junit.Test;

import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RegionStatTest {

    private RegionStat stat;

    @Test
    public void testConstructor() {
        stat = new RegionStat(0xfffffff, 0xffff0000);
        assertThat(stat.used(), is(1.0));
        assertThat(stat.live(), is(1.0));
        EnumSet<RegionFlag> flags = stat.flags();
        assertTrue(flags.contains(RegionFlag.UNUSED));
        assertTrue(flags.contains(RegionFlag.IN_COLLECTION_SET));
        assertTrue(flags.contains(RegionFlag.HUMONGOUS));
        assertTrue(flags.contains(RegionFlag.RECENTLY_ALLOCATED));
        assertTrue(flags.contains(RegionFlag.PINNED));
    }

    @Test
    public void testConstructor2() {
        stat = new RegionStat(0x0f, 0x00f);
        assertThat(stat.used(), is(1.0));
        assertThat(stat.live(), is(0.0));
        EnumSet<RegionFlag> flags = stat.flags();
        assertFalse(flags.contains(RegionFlag.UNUSED));
        assertFalse(flags.contains(RegionFlag.IN_COLLECTION_SET));
        assertFalse(flags.contains(RegionFlag.HUMONGOUS));
        assertFalse(flags.contains(RegionFlag.RECENTLY_ALLOCATED));
        assertFalse(flags.contains(RegionFlag.PINNED));
    }

}
