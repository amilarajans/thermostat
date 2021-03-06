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
import com.redhat.thermostat.backend.VmUpdateListener;
import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.common.SystemClock;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VmShenandoahListener implements VmUpdateListener {

    private static final Logger logger = LoggingUtils.getLogger(VmShenandoahListener.class);

    private final String writerID;
    private final String vmId;
    private final VmShenandoahDao dao;
    private final Clock clock;
    private boolean isActive = true;

    public VmShenandoahListener(String writerID, String vmId, VmShenandoahDao dao) {
        this(writerID, vmId, dao, new SystemClock());
    }

    // Package-private for testing
    VmShenandoahListener(String writerID, String vmId, VmShenandoahDao dao, Clock clock) {
        this.writerID = writerID;
        this.vmId = vmId;
        this.dao = dao;
        this.clock = clock;
    }

    @Override
    public void countersUpdated(VmUpdate update) {
        if (!isActive) {
            return;
        }
        try {
            VmShenandoahDataExtractor extractor = new VmShenandoahDataExtractor(update);
            VmShenandoahStat stat = new VmShenandoahStat(writerID, vmId, clock.getRealTimeMillis(),
                    extractor.getTimestamp(), extractor.getMaxRegions(), extractor.getRegionSize(), extractor.getStatus(), extractor.getRegionData());
            dao.putShenandoahStat(stat);
        } catch (VmUpdateException e) {
            logger.log(Level.WARNING, "Error gathering Shenandoah statistics for VM " + vmId, e);
        } catch (NullPointerException e) {
            // VM is not running Shenandoah
            isActive = false;
        }
    }
}