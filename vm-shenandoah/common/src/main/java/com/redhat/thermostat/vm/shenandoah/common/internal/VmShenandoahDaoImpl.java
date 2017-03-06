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

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.storage.core.Category;
import com.redhat.thermostat.storage.core.CategoryAdapter;
import com.redhat.thermostat.storage.core.Key;
import com.redhat.thermostat.storage.core.PreparedStatement;
import com.redhat.thermostat.storage.core.Storage;
import com.redhat.thermostat.storage.core.VmLatestPojoListGetter;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.dao.AbstractDao;
import com.redhat.thermostat.storage.dao.AbstractDaoQuery;
import com.redhat.thermostat.storage.dao.AbstractDaoStatement;
import com.redhat.thermostat.storage.model.AggregateCount;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;

import java.util.List;
import java.util.logging.Logger;

public class VmShenandoahDaoImpl extends AbstractDao implements VmShenandoahDao {

    private static final Logger logger = LoggingUtils.getLogger(VmShenandoahDaoImpl.class);

    static final String DESC_ADD_VM_SHENANDOAH_STAT = "ADD " + VM_SHENANDOAH_STAT_CATEGORY.getName() +
           " SET '" + Key.AGENT_ID.getName()               + "' = ?s , " +
                "'" + Key.VM_ID.getName()                  + "' = ?s , " +
                "'" + Key.TIMESTAMP.getName()              + "' = ?l , " +
                "'" + REGION_TIMESTAMP_KEY.getName()       + "' = ?l , " +
                "'" + MAX_REGIONS_KEY.getName()            + "' = ?l , " +
                "'" + REGION_SIZE_KEY.getName()            + "' = ?l , " +
                "'" + REGION_STATUS_KEY.getName()          + "' = ?l , " +
                "'" + REGION_DATA_KEY.getName()            + "' = ?l[";

    static final String QUERY_COUNT_SHENANDOAH_STATS_BY_VM_ID = ""
            + "QUERY-COUNT " + VM_SHENANDOAH_STAT_CATEGORY.getName() + " "
            + "WHERE '" + Key.VM_ID.getName() + "' = ?s";

    private final Storage storage;
    final Category<AggregateCount> aggregateCountCategory;
    final VmLatestPojoListGetter<VmShenandoahStat> getter;

    public VmShenandoahDaoImpl(Storage storage) {
        this.storage = storage;
        storage.registerCategory(VM_SHENANDOAH_STAT_CATEGORY);
        this.aggregateCountCategory = new CategoryAdapter<VmShenandoahStat, AggregateCount>(VM_SHENANDOAH_STAT_CATEGORY).getAdapted(AggregateCount.class);
        storage.registerCategory(aggregateCountCategory);
        this.getter = new VmLatestPojoListGetter<>(storage, VM_SHENANDOAH_STAT_CATEGORY);
    }

    @Override
    public void putShenandoahStat(final VmShenandoahStat vmShenandoahStat) {
        executeStatement(new AbstractDaoStatement<VmShenandoahStat>(storage, VM_SHENANDOAH_STAT_CATEGORY, DESC_ADD_VM_SHENANDOAH_STAT) {
            @Override
            public PreparedStatement<VmShenandoahStat> customize(PreparedStatement<VmShenandoahStat> preparedStatement) {
                preparedStatement.setString(0, vmShenandoahStat.getAgentId());
                preparedStatement.setString(1, vmShenandoahStat.getVmId());
                preparedStatement.setLong(2, vmShenandoahStat.getTimeStamp());
                preparedStatement.setLong(3, vmShenandoahStat.getRegionTimeStamp());
                preparedStatement.setLong(4, vmShenandoahStat.getMaxRegions());
                preparedStatement.setLong(5, vmShenandoahStat.getRegionSize());
                preparedStatement.setLong(6, vmShenandoahStat.getRegionStatus());
                preparedStatement.setLongList(7, vmShenandoahStat.getRegionData());
                return preparedStatement;
            }
        });
    }

    @Override
    public List<VmShenandoahStat> getLatestShenandoahStats(VmRef ref, long since) {
        return getter.getLatest(ref, since);
    }

    @Override
    public long getCount(final VmRef ref) {
        return executeQuery(new AbstractDaoQuery<AggregateCount>(storage, aggregateCountCategory, QUERY_COUNT_SHENANDOAH_STATS_BY_VM_ID) {
            @Override
            public PreparedStatement<AggregateCount> customize(PreparedStatement<AggregateCount> preparedStatement) {
                preparedStatement.setString(0, ref.getVmId());
                return preparedStatement;
            }
        }).head().getCount();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
