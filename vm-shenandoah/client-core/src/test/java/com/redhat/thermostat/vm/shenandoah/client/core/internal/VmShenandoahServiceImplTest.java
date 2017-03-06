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

package com.redhat.thermostat.vm.shenandoah.client.core.internal;

import com.redhat.thermostat.common.ApplicationService;
import com.redhat.thermostat.common.Filter;
import com.redhat.thermostat.common.Ordered;
import com.redhat.thermostat.storage.core.AgentId;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.storage.model.AgentInformation;
import com.redhat.thermostat.storage.model.VmInfo;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahViewProvider;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VmShenandoahServiceImplTest {

    private ApplicationService appSvc;
    private AgentInfoDAO agentInfoDAO;
    private VmInfoDAO vmInfoDAO;
    private VmShenandoahDao vmShenandoahDao;
    private VmShenandoahViewProvider viewProvider;
    private VmRef vmRef;

    private VmShenandoahServiceImpl service;

    @Before
    public void setup() {
        appSvc = mock(ApplicationService.class);
        agentInfoDAO = mock(AgentInfoDAO.class);
        vmInfoDAO = mock(VmInfoDAO.class);
        vmShenandoahDao = mock(VmShenandoahDao.class);
        viewProvider = mock(VmShenandoahViewProvider.class);
        vmRef = mock(VmRef.class);

        service = new VmShenandoahServiceImpl(appSvc, agentInfoDAO, vmInfoDAO, vmShenandoahDao, viewProvider);
    }

    @Test
    public void testFilterDeadVm() {
        Filter<VmRef> filter = service.getFilter();

        AgentInformation agentInformation = new AgentInformation();
        agentInformation.setAgentId("foo-agentId");
        agentInformation.setAlive(true);
        when(agentInfoDAO.getAgentInformation(isA(AgentId.class))).thenReturn(agentInformation);
        VmInfo vmInfo = new VmInfo();
        vmInfo.setAgentId(agentInformation.getAgentId());
        vmInfo.setStartTimeStamp(0L);
        vmInfo.setStopTimeStamp(100L);
        when(vmInfoDAO.getVmInfo(eq(vmRef))).thenReturn(vmInfo);

        assertFalse(filter.matches(vmRef));
    }

    @Test
    public void testFilterUnknownVm() {
        Filter<VmRef> filter = service.getFilter();

        AgentInformation agentInformation = new AgentInformation();
        agentInformation.setAgentId("foo-agentId");
        agentInformation.setAlive(false);
        when(agentInfoDAO.getAgentInformation(isA(AgentId.class))).thenReturn(agentInformation);
        VmInfo vmInfo = new VmInfo();
        vmInfo.setAgentId(agentInformation.getAgentId());
        vmInfo.setStartTimeStamp(0L);
        vmInfo.setStopTimeStamp(100L);
        when(vmInfoDAO.getVmInfo(eq(vmRef))).thenReturn(vmInfo);

        assertFalse(filter.matches(vmRef));
    }

    @Test
    public void testFilterAliveVmWithShenandoahStats() {
        Filter<VmRef> filter = service.getFilter();

        AgentInformation agentInformation = new AgentInformation();
        agentInformation.setAgentId("foo-agentId");
        agentInformation.setAlive(true);
        when(agentInfoDAO.getAgentInformation(isA(AgentId.class))).thenReturn(agentInformation);
        VmInfo vmInfo = new VmInfo();
        vmInfo.setAgentId(agentInformation.getAgentId());
        when(vmInfoDAO.getVmInfo(eq(vmRef))).thenReturn(vmInfo);

        when(vmShenandoahDao.getCount(eq(vmRef))).thenReturn(2L);

        assertTrue(filter.matches(vmRef));
    }

    @Test
    public void testFilterAliveVmWithoutShenandoahStats() {
        Filter<VmRef> filter = service.getFilter();

        AgentInformation agentInformation = new AgentInformation();
        agentInformation.setAgentId("foo-agentId");
        agentInformation.setAlive(true);
        when(agentInfoDAO.getAgentInformation(isA(AgentId.class))).thenReturn(agentInformation);
        VmInfo vmInfo = new VmInfo();
        vmInfo.setAgentId(agentInformation.getAgentId());
        when(vmInfoDAO.getVmInfo(eq(vmRef))).thenReturn(vmInfo);

        when(vmShenandoahDao.getCount(eq(vmRef))).thenReturn(0L);

        assertFalse(filter.matches(vmRef));
    }

    @Test
    public void testOrderValue() {
        assertThat(service.getOrderValue(), is(Ordered.ORDER_MEMORY_GROUP + 21));
    }

}
