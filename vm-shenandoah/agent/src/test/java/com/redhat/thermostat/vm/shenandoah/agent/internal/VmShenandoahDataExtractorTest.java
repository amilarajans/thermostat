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

package com.redhat.thermostat.vm.shenandoah.agent.internal;

import com.redhat.thermostat.backend.VmUpdate;
import com.redhat.thermostat.backend.VmUpdateException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VmShenandoahDataExtractorTest {

    private VmShenandoahDataExtractor extractor;
    private VmUpdate update;

    @Before
    public void setup() {
        update = mock(VmUpdate.class);
        extractor = new VmShenandoahDataExtractor(update);
    }

    @Test
    public void testGetTimestamp() throws VmUpdateException {
        extractor.getTimestamp();
        verify(update).getPerformanceCounterLong("sun.gc.shenandoah.regions.timestamp");
    }

    @Test
    public void testGetMaxRegions() throws VmUpdateException {
        extractor.getMaxRegions();
        verify(update).getPerformanceCounterLong("sun.gc.shenandoah.regions.max_regions");
    }

    @Test
    public void testGetRegionSize() throws VmUpdateException {
        extractor.getRegionSize();
        verify(update).getPerformanceCounterLong("sun.gc.shenandoah.regions.region_size");
    }

    @Test
    public void testGetStatus() throws VmUpdateException {
        extractor.getStatus();
        verify(update).getPerformanceCounterLong("sun.gc.shenandoah.regions.status");
    }

    @Test
    public void testGetRegionData() throws VmUpdateException {
        when(update.getPerformanceCounterLong("sun.gc.shenandoah.regions.max_regions")).thenReturn(2L);
        extractor.getRegionData();
        InOrder inOrder = inOrder(update);
        inOrder.verify(update).getPerformanceCounterLong("sun.gc.shenandoah.regions.max_regions");
        inOrder.verify(update).getPerformanceCounterLong("sun.gc.shenandoah.regions.region.0.data");
        inOrder.verify(update).getPerformanceCounterLong("sun.gc.shenandoah.regions.region.1.data");
    }

}
