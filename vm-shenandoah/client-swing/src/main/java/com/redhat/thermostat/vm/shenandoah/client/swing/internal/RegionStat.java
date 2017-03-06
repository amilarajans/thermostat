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

import java.awt.Color;
import java.awt.Graphics;
import java.util.EnumSet;

import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.Colors.LIVE;
import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.Colors.LIVE_BORDER;
import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.Colors.USED;
import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.Colors.USED_ALLOC;

public class RegionStat {

    private static final int USED_MASK = 0x1fffffff;
    private static final int USED_SHIFT = 0;
    private static final int LIVE_MASK = 0x1fffffff;
    private static final int LIVE_SHIFT = 29;
    private static final int FLAGS_MASK = 0x3f;
    private static final int FLAGS_SHIFT = 58;

    private final EnumSet<RegionFlag> flags;
    private final double liveLvl;
    private final double usedLvl;

    public RegionStat(double usedLvl, double liveLvl, EnumSet<RegionFlag> flags) {
        this.usedLvl = usedLvl;
        this.liveLvl = liveLvl;
        this.flags = flags;
    }

    public RegionStat(long maxSize, long data) {
        long used = (data >>> USED_SHIFT) & USED_MASK;
        usedLvl = Math.min(1D, 1D * used / maxSize);

        long live = (data >>> LIVE_SHIFT) & LIVE_MASK;
        liveLvl = Math.min(1D, 1D * live / maxSize);

        long stat = (data >>> FLAGS_SHIFT) & FLAGS_MASK;

        flags = EnumSet.noneOf(RegionFlag.class);

        if ((stat & 1)  > 0) flags.add(RegionFlag.UNUSED);
        if ((stat & 2)  > 0) flags.add(RegionFlag.IN_COLLECTION_SET);
        if ((stat & 4)  > 0) flags.add(RegionFlag.HUMONGOUS);
        if ((stat & 8)  > 0) flags.add(RegionFlag.RECENTLY_ALLOCATED);
        if ((stat & 16) > 0) flags.add(RegionFlag.PINNED);
    }

    public void render(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(x, y, width, height);

        int usedWidth = (int) (width * usedLvl);
        g.setColor(
                flags.contains(RegionFlag.RECENTLY_ALLOCATED) ?
                        USED_ALLOC.getColor() : USED.getColor()
        );
        g.fillRect(x, y, usedWidth, height);

        if (!flags.contains(RegionFlag.RECENTLY_ALLOCATED)) {
            int liveWidth = (int) (width * liveLvl);
            g.setColor(LIVE.getColor());
            g.fillRect(x, y, liveWidth, height);

            g.setColor(LIVE_BORDER.getColor());
            g.drawLine(x + liveWidth, y, x + liveWidth, y + height);
        }

        if (flags.contains(RegionFlag.IN_COLLECTION_SET)) {
            g.setColor(Colors.CSET.getColor());
            g.fillRect(x, y, width, height / 3);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height / 3);
        }

        if (flags.contains(RegionFlag.HUMONGOUS)) {
            g.setColor(Colors.HUMONGOUS.getColor());
            g.fillRect(x, y, width, height / 3);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height / 3);
        }

        if (flags.contains(RegionFlag.UNUSED)) {
            g.setColor(Color.BLACK);
            g.drawLine(x, y, x + width, y + height);
            g.drawLine(x, y + height, x + width, y);
        }

        if (flags.contains(RegionFlag.PINNED)) {
            g.setColor(Color.RED);
            g.fillOval(x + width/2, y + height/2, width/4, height/4);
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegionStat that = (RegionStat) o;

        if (Double.compare(that.liveLvl, liveLvl) != 0) return false;
        if (Double.compare(that.usedLvl, usedLvl) != 0) return false;
        return flags.equals(that.flags);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = flags.hashCode();
        temp = Double.doubleToLongBits(liveLvl);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(usedLvl);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public double live() {
        return liveLvl;
    }

    public double used() {
        return usedLvl;
    }

    public EnumSet<RegionFlag> flags() {
        return flags;
    }

}
