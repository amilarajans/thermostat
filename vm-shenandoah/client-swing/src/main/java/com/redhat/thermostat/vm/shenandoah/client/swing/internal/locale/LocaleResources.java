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

package com.redhat.thermostat.vm.shenandoah.client.swing.internal.locale;

import com.redhat.thermostat.shared.locale.Translate;

public enum LocaleResources  {

    UNUSED_REGION_LABEL,
    EMPTY_REGION_LABEL,
    HALF_USED_REGION_LABEL,
    FULLY_USED_REGION_LABEL,
    FULLY_USED_RECENTLY_ALLOCATED_REGION_LABEL,
    FULLY_LIVE_REGION_LABEL,
    FULLY_LIVE_HUMONGOUS_REGION_LABEL,
    THIRD_LIVE_REGION_LABEL,
    THIRD_LIVE_COLLECITON_SET_REGION_LABEL,
    THIRD_LIVE_PINNED_REGION_LABEL,

    MARKING_STATUS,
    EVACUATING_STATUS,
    MARKING_AND_EVACUATING_STATUS,
    IDLE_STATUS,

    TIME_STAT_LABEL,
    STATUS_STAT_LABEL,
    TOTAL_STAT_LABEL,
    USED_STAT_LABEL,
    LIVE_STAT_LABEL,
    ;

    static final String RESOURCE_BUNDLE =
            "com.redhat.thermostat.vm.shenandoah.client.swing.internal.locale.strings";

    public static Translate<LocaleResources> createLocalizer() {
        return new Translate<>(RESOURCE_BUNDLE, LocaleResources.class);
    }
}
