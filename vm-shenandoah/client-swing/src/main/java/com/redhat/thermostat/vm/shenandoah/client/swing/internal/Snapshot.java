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

import java.util.List;
import java.util.Objects;

public class Snapshot {

    private final long time;
    private final long regionSize;
    private final List<RegionStat> stats;
    private final boolean isMarking;
    private final boolean isEvacuating;

    public Snapshot(long time, long regionSize, List<RegionStat> stats, boolean isMarking, boolean isEvacuating) {
        this.time = time;
        this.regionSize = regionSize;
        this.stats = stats;
        this.isMarking = isMarking;
        this.isEvacuating = isEvacuating;
    }

    public boolean isMarking() {
        return isMarking;
    }

    public boolean isEvacuating() {
        return isEvacuating;
    }

    public RegionStat get(int i) {
        return stats.get(i);
    }

    public long time() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Snapshot snapshot = (Snapshot) o;

        if (time != snapshot.time) return false;
        if (isMarking != snapshot.isMarking) return false;
        if (isEvacuating != snapshot.isEvacuating) return false;
        return Objects.equals(stats, snapshot.stats);
    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + (stats != null ? stats.hashCode() : 0);
        result = 31 * result + (isMarking ? 1 : 0);
        result = 31 * result + (isEvacuating ? 1 : 0);
        return result;
    }

    public int regionCount() {
        return stats.size();
    }

    public long total() {
        return regionSize * regionCount();
    }

    public long used() {
        long used = 0L;
        for (RegionStat rs : stats) {
            used += regionSize * rs.used();
        }
        return used;
    }

    public long recentlyAllocated() {
        long used = 0L;
        for (RegionStat rs : stats) {
            if (rs.flags().contains(RegionFlag.RECENTLY_ALLOCATED)) {
                used += regionSize * rs.used();
            }
        }
        return used;
    }

    public long collectionSet() {
        long used = 0L;
        for (RegionStat rs : stats) {
            if (rs.flags().contains(RegionFlag.IN_COLLECTION_SET)) {
                used += regionSize * rs.used();
            }
        }
        return used;
    }

    public long humongous() {
        long used = 0L;
        for (RegionStat rs : stats) {
            if (rs.flags().contains(RegionFlag.HUMONGOUS)) {
                used += regionSize * rs.used();
            }
        }
        return used;
    }

    public long live() {
        long live = 0L;
        for (RegionStat rs : stats) {
            live += regionSize * rs.live();
        }
        return live;
    }
}
