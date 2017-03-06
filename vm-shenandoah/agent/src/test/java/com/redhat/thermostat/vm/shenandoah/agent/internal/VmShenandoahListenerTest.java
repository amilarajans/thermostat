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
import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class VmShenandoahListenerTest {

    private String writerID;
    private String vmId;
    private VmShenandoahDao dao;
    private Clock clock;
    private VmUpdate vmUpdate;

    private VmShenandoahListener listener;

    @Before
    public void setup() throws VmUpdateException {
        writerID = "foo-writerID";
        vmId = "foo-vmId";
        dao = mock(VmShenandoahDao.class);
        clock = mock(Clock.class);
        when(clock.getRealTimeMillis()).thenReturn(100L);
        vmUpdate = mock(VmUpdate.class);
        when(vmUpdate.getPerformanceCounterLong("sun.gc.shenandoah.regions.timestamp")).thenReturn(50L);
        when(vmUpdate.getPerformanceCounterLong("sun.gc.shenandoah.regions.max_regions")).thenReturn(2L);
        when(vmUpdate.getPerformanceCounterLong("sun.gc.shenandoah.regions.region_size")).thenReturn(1024L);
        when(vmUpdate.getPerformanceCounterLong("sun.gc.shenandoah.regions.status")).thenReturn(4L);
        when(vmUpdate.getPerformanceCounterLong("sun.gc.shenandoah.regions.region.0.data")).thenReturn(10L);
        when(vmUpdate.getPerformanceCounterLong("sun.gc.shenandoah.regions.region.1.data")).thenReturn(10L);

        listener = new VmShenandoahListener(writerID, vmId, dao, clock);
    }

    @Test
    public void testCountersUpdated() throws VmUpdateException {
        listener.countersUpdated(vmUpdate);

        verify(clock).getRealTimeMillis();
        verify(vmUpdate).getPerformanceCounterLong("sun.gc.shenandoah.regions.timestamp");
        verify(vmUpdate, times(2)).getPerformanceCounterLong("sun.gc.shenandoah.regions.max_regions");
        verify(vmUpdate).getPerformanceCounterLong("sun.gc.shenandoah.regions.region_size");
        verify(vmUpdate).getPerformanceCounterLong("sun.gc.shenandoah.regions.status");
        verify(vmUpdate).getPerformanceCounterLong("sun.gc.shenandoah.regions.region.0.data");
        verify(vmUpdate).getPerformanceCounterLong("sun.gc.shenandoah.regions.region.1.data");
        verifyNoMoreInteractions(vmUpdate);

        ArgumentCaptor<VmShenandoahStat> daoCaptor = ArgumentCaptor.forClass(VmShenandoahStat.class);
        verify(dao).putShenandoahStat(daoCaptor.capture());
        VmShenandoahStat stat = daoCaptor.getValue();
        assertThat(stat.getTimeStamp(), is(100L));
        assertThat(stat.getMaxRegions(), is(2L));
        assertThat(stat.getRegionSize(), is(1024L));
        assertThat(stat.getRegionStatus(), is(4L));
        assertThat(stat.getRegionData(), is(equalTo(new long[]{10L, 10L})));
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void testDisablesIfNotRunningShenandoah() throws VmUpdateException {
        when(vmUpdate.getPerformanceCounterLong(anyString())).thenThrow(NullPointerException.class);
        when(vmUpdate.getPerformanceCounterString(anyString())).thenThrow(NullPointerException.class);
        listener.countersUpdated(vmUpdate);
        verify(vmUpdate).getPerformanceCounterLong("sun.gc.shenandoah.regions.timestamp");
        listener.countersUpdated(vmUpdate);
        verify(vmUpdate).getPerformanceCounterLong("sun.gc.shenandoah.regions.timestamp"); // still just once

        verifyZeroInteractions(dao);
    }

}
