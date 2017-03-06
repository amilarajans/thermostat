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

package com.redhat.thermostat.vm.shenandoah.common.internal;

import com.redhat.thermostat.storage.core.BasicBatchCursor;
import com.redhat.thermostat.storage.core.Cursor;
import com.redhat.thermostat.storage.core.DescriptorParsingException;
import com.redhat.thermostat.storage.core.HostRef;
import com.redhat.thermostat.storage.core.Key;
import com.redhat.thermostat.storage.core.PreparedStatement;
import com.redhat.thermostat.storage.core.StatementDescriptor;
import com.redhat.thermostat.storage.core.StatementExecutionException;
import com.redhat.thermostat.storage.core.Storage;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.model.AggregateCount;
import com.redhat.thermostat.storage.model.Pojo;
import com.redhat.thermostat.storage.model.VmInfo;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VmShenandoahDaoImplTest {

    private Storage storage;
    private PreparedStatement stmt;
    private VmShenandoahDaoImpl dao;

    @Before
    public void seup() throws DescriptorParsingException {
        storage = mock(Storage.class);
        stmt = mock(PreparedStatement.class);
        when(storage.prepareStatement(any(StatementDescriptor.class))).thenReturn(stmt);
        dao = new VmShenandoahDaoImpl(storage);
    }

    @Test
    public void testRegistersStatCategory() {
        verify(storage).registerCategory(VmShenandoahDao.VM_SHENANDOAH_STAT_CATEGORY);
    }

    @Test
    public void testRegistersAggregateCategory() {
        verify(storage).registerCategory(dao.aggregateCountCategory);
    }

    @Test
    public void testCategory() {
        assertEquals("vm-shenandoah-stats", VmShenandoahDao.VM_SHENANDOAH_STAT_CATEGORY.getName());
        Collection<Key<?>> keys = VmShenandoahDao.VM_SHENANDOAH_STAT_CATEGORY.getKeys();
        assertTrue(keys.contains(new Key<>("agentId")));
        assertTrue(keys.contains(new Key<Integer>("vmId")));
        assertTrue(keys.contains(new Key<Long>("timeStamp")));
        assertTrue(keys.contains(new Key<Long>("regionTimeStamp")));
        assertTrue(keys.contains(new Key<Long>("maxRegions")));
        assertTrue(keys.contains(new Key<Long>("regionSize")));
        assertTrue(keys.contains(new Key<Long>("regionStatus")));
        assertTrue(keys.contains(new Key<Long>("regionData")));
        assertEquals(8, keys.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPutShenandoahStat() throws DescriptorParsingException {
        VmShenandoahStat stat = new VmShenandoahStat("foo-writerId", "foo-vmId", 100L, 50L, 2L, 1024L, 1L, new long[]{1L, 2L});
        stat.setAgentId("foo-agentId");
        dao.putShenandoahStat(stat);

        ArgumentCaptor<StatementDescriptor> descriptorCaptor = ArgumentCaptor.forClass(StatementDescriptor.class);
        verify(storage).prepareStatement(descriptorCaptor.capture());
        StatementDescriptor<VmShenandoahStat> descriptor = descriptorCaptor.getValue();
        assertThat(descriptor.getCategory(), is(VmShenandoahDao.VM_SHENANDOAH_STAT_CATEGORY));
        assertThat(descriptor.getDescriptor(), is(VmShenandoahDaoImpl.DESC_ADD_VM_SHENANDOAH_STAT));

        verify(stmt).setString(0, stat.getAgentId());
        verify(stmt).setString(1, stat.getVmId());
        verify(stmt).setLong(2, stat.getTimeStamp());
        verify(stmt).setLong(3, stat.getRegionTimeStamp());
        verify(stmt).setLong(4, stat.getMaxRegions());
        verify(stmt).setLong(5, stat.getRegionSize());
        verify(stmt).setLong(6, stat.getRegionStatus());
        verify(stmt).setLongList(7, stat.getRegionData());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetCount() throws DescriptorParsingException, StatementExecutionException {
        AggregateCount aggregateCount = new AggregateCount();
        aggregateCount.setCount(2L);

        Cursor<AggregateCount> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true).thenReturn(false);
        when(cursor.next()).thenReturn(aggregateCount).thenThrow(NoSuchElementException.class);

        when(stmt.executeQuery()).thenReturn(cursor);

        HostRef hostRef = new HostRef("host-id", "host-name");
        VmInfo vmInfo = new VmInfo();
        dao.getCount(new VmRef(hostRef, vmInfo));

        ArgumentCaptor<StatementDescriptor> descriptorCaptor = ArgumentCaptor.forClass(StatementDescriptor.class);
        verify(storage).prepareStatement(descriptorCaptor.capture());
        StatementDescriptor<AggregateCount> descriptor = descriptorCaptor.getValue();
        assertThat(descriptor.getCategory(), is(dao.aggregateCountCategory));
        assertThat(descriptor.getDescriptor(), is(VmShenandoahDaoImpl.QUERY_COUNT_SHENANDOAH_STATS_BY_VM_ID));
    }

}
