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

package com.redhat.thermostat.backend.system.internal;

import com.redhat.thermostat.common.portability.PortableHost;
import com.redhat.thermostat.backend.system.internal.models.HostInfoBuilder;
import com.redhat.thermostat.common.portability.PortableHostFactory;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.storage.model.HostInfo;

/**
 * Build Host information via helper classes
 */
class HostInfoBuilderImpl implements HostInfoBuilder {

    private final WriterID writerID;
    private final PortableHost helper;

    HostInfoBuilderImpl(final WriterID writerID) {
        this(writerID, PortableHostFactory.getInstance());
    }

    HostInfoBuilderImpl(final WriterID writerID, PortableHost helper) {
        this.writerID = writerID;
        this.helper = helper;
    }

    @Override
    public HostInfo build() {
        String wId = writerID.getWriterID();
        return new HostInfo(wId,
                helper.getHostName(),
                helper.getOSName(),
                helper.getOSVersion(),
                helper.getCPUModel(),
                helper.getCPUCount(),
                helper.getTotalMemory());
    }
}
