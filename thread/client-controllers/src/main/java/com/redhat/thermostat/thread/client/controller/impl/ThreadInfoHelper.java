/*
 * Copyright 2012 Red Hat, Inc.
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

package com.redhat.thermostat.thread.client.controller.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.thermostat.thread.model.ThreadInfoData;

public class ThreadInfoHelper {

    /**
     * Creates a {@link Map} whose keys are {@link ThreadInfoData} in the input
     * list and whose values are all the {@link ThreadInfoData} equals to the
     * key.
     *
     * <br /><br />
     * 
     * Preserves the order of the input list.
     * 
     * <br /><br />
     * 
     * <strong>NOTE</strong>: The current invariant is that
     * {@link ThreadInfoData} are equals if they have same thread id and name.
     */
    public static Map<ThreadInfoData, List<ThreadInfoData>> getThreadInfoDataMap(List<ThreadInfoData> infos) {
        Map<ThreadInfoData, List<ThreadInfoData>> stats = new HashMap<>();
        for (ThreadInfoData info : infos) {
            List<ThreadInfoData> beanList = stats.get(info);
            if (beanList == null) {
                beanList = new ArrayList<ThreadInfoData>();
                stats.put(info, beanList);
            }                    
            beanList.add(info);
        }
        return stats;
    }
}
