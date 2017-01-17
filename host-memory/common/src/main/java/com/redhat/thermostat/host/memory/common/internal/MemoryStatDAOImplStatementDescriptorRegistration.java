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

package com.redhat.thermostat.host.memory.common.internal;

import java.util.HashSet;
import java.util.Set;

import com.redhat.thermostat.host.memory.common.MemoryStatDAO;
import com.redhat.thermostat.storage.core.HostBoundaryPojoGetter;
import com.redhat.thermostat.storage.core.HostLatestPojoListGetter;
import com.redhat.thermostat.storage.core.HostTimeIntervalPojoListGetter;
import com.redhat.thermostat.storage.core.auth.StatementDescriptorRegistration;

/**
 * Registers the prepared query issued by this maven module via
 * {@link HostLatestPojoListGetter}.
 *
 */
public class MemoryStatDAOImplStatementDescriptorRegistration implements
        StatementDescriptorRegistration {

    static final String latestDescriptor = String.format(HostLatestPojoListGetter.HOST_LATEST_QUERY_FORMAT,
            MemoryStatDAO.memoryStatCategory.getName());
    static final String intervalDescriptor = String.format(HostTimeIntervalPojoListGetter.HOST_INTERVAL_QUERY_FORMAT,
            MemoryStatDAO.memoryStatCategory.getName());
    static final String latestStatDescriptor = String.format(HostBoundaryPojoGetter.DESC_NEWEST_HOST_STAT,
            MemoryStatDAO.memoryStatCategory.getName());
    static final String oldestStatDescriptor = String.format(HostBoundaryPojoGetter.DESC_OLDEST_HOST_STAT,
            MemoryStatDAO.memoryStatCategory.getName());
    
    @Override
    public Set<String> getStatementDescriptors() {
        Set<String> descs = new HashSet<>(5);
        descs.add(latestDescriptor);
        descs.add(intervalDescriptor);
        descs.add(latestStatDescriptor);
        descs.add(oldestStatDescriptor);
        descs.add(MemoryStatDAOImpl.DESC_ADD_MEMORY_STAT);
        return descs;
    }

}

