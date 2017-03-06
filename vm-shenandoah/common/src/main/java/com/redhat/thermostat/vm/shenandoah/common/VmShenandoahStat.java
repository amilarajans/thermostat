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

package com.redhat.thermostat.vm.shenandoah.common;

import com.redhat.thermostat.storage.core.Entity;
import com.redhat.thermostat.storage.core.Persist;
import com.redhat.thermostat.storage.model.BasePojo;
import com.redhat.thermostat.storage.model.TimeStampedPojo;

import java.util.Arrays;

@Entity
public class VmShenandoahStat extends BasePojo implements TimeStampedPojo {

    private String vmId;
    private long timeStamp;
    private long regionTimeStamp;
    private long maxRegions;
    private long regionSize;
    private long regionStatus;
    private long[] regionData;

    public VmShenandoahStat() {
        super(null);
    }

    public VmShenandoahStat(String writerId, String vmId, long timeStamp,
                            long regionTimeStamp, long maxRegions, long regionSize, long regionStatus, long[] regionData) {
        super(writerId);
        this.vmId = vmId;
        this.timeStamp = timeStamp;
        this.regionTimeStamp = regionTimeStamp;
        this.maxRegions = maxRegions;
        this.regionSize = regionSize;
        this.regionStatus = regionStatus;
        this.regionData = regionData;
    }

    @Persist
    public String getVmId() {
        return vmId;
    }

    @Persist
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    @Persist
    public long getTimeStamp() {
        return timeStamp;
    }

    @Persist
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Persist
    public long getRegionTimeStamp() {
        return regionTimeStamp;
    }

    @Persist
    public void setRegionTimeStamp(long regionTimeStamp) {
        this.regionTimeStamp = regionTimeStamp;
    }

    @Persist
    public long getMaxRegions() {
        return maxRegions;
    }

    @Persist
    public void setMaxRegions(long maxRegions) {
        this.maxRegions = maxRegions;
    }

    @Persist
    public long getRegionSize() {
        return regionSize;
    }

    @Persist
    public void setRegionSize(long regionSize) {
        this.regionSize = regionSize;
    }

    @Persist
    public long getRegionStatus() {
        return regionStatus;
    }

    @Persist
    public void setRegionStatus(long regionStatus) {
        this.regionStatus = regionStatus;
    }

    @Persist
    public long[] getRegionData() {
        return regionData;
    }

    @Persist
    public void setRegionData(long[] regionData) {
        this.regionData = regionData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        VmShenandoahStat that = (VmShenandoahStat) o;

        if (getTimeStamp() != that.getTimeStamp()) return false;
        if (getRegionTimeStamp() != that.getRegionTimeStamp()) return false;
        if (getMaxRegions() != that.getMaxRegions()) return false;
        if (getRegionSize() != that.getRegionSize()) return false;
        if (getRegionStatus() != that.getRegionStatus()) return false;
        if (!getVmId().equals(that.getVmId())) return false;
        return Arrays.equals(getRegionData(), that.getRegionData());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getVmId().hashCode();
        result = 31 * result + (int) (getTimeStamp() ^ (getTimeStamp() >>> 32));
        result = 31 * result + (int) (getRegionTimeStamp() ^ (getRegionTimeStamp() >>> 32));
        result = 31 * result + (int) (getMaxRegions() ^ (getMaxRegions() >>> 32));
        result = 31 * result + (int) (getRegionSize() ^ (getRegionSize() >>> 32));
        result = 31 * result + (int) (getRegionStatus() ^ (getRegionStatus() >>> 32));
        result = 31 * result + Arrays.hashCode(getRegionData());
        return result;
    }
}
