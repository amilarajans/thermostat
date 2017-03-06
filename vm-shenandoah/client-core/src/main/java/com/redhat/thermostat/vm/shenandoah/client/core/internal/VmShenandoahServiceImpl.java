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

import com.redhat.thermostat.client.core.controllers.InformationServiceController;
import com.redhat.thermostat.common.ApplicationService;
import com.redhat.thermostat.common.Filter;
import com.redhat.thermostat.storage.core.AgentId;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.storage.model.AgentInformation;
import com.redhat.thermostat.storage.model.VmInfo;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahService;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahViewProvider;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VmShenandoahServiceImpl implements VmShenandoahService {

    private final Filter<VmRef> filter;
    private final ApplicationService appSvc;
    private final VmShenandoahDao vmShenandoahDao;
    private final VmShenandoahViewProvider viewProvider;
    private final Map<VmRef, VmShenandoahController> controllers = new ConcurrentHashMap<>();

    public VmShenandoahServiceImpl(ApplicationService appSvc, final AgentInfoDAO agentInfoDAO, final VmInfoDAO vmInfoDAO,
                                   final VmShenandoahDao vmShenandoahDao, VmShenandoahViewProvider viewProvider) {
        this.appSvc = appSvc;
        this.vmShenandoahDao = vmShenandoahDao;
        this.viewProvider = viewProvider;
        this.filter = new Filter<VmRef>() {
            @Override
            public boolean matches(VmRef toMatch) {
                return isAlive(toMatch) && hasStats(toMatch);
            }

            private boolean isAlive(VmRef ref) {
                VmInfo vmInfo = vmInfoDAO.getVmInfo(ref);
                AgentInformation agentInformation = agentInfoDAO.getAgentInformation(new AgentId(vmInfo.getAgentId()));
                return VmInfo.AliveStatus.RUNNING.equals(vmInfo.isAlive(agentInformation));
            }

            private boolean hasStats(VmRef ref) {
                return vmShenandoahDao.getCount(ref) > 0;
            }
        };
    }

    @Override
    public InformationServiceController<VmRef> getInformationServiceController(VmRef ref) {
        if (controllers.get(ref) == null) {
            controllers.put(ref, new VmShenandoahController(appSvc, ref, vmShenandoahDao, viewProvider));
        }
        return controllers.get(ref);
    }

    @Override
    public Filter<VmRef> getFilter() {
        return filter;
    }

    @Override
    public int getOrderValue() {
        return ORDER_MEMORY_GROUP + 21;
    }
}
