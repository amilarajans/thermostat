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

import com.redhat.thermostat.agent.VmStatusListenerRegistrar;
import com.redhat.thermostat.common.Ordered;
import com.redhat.thermostat.common.Version;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VmShenandoahBackendTest {

    private VmShenandoahBackend backend;

    @Before
    public void setup() {
        VmShenandoahDao dao = mock(VmShenandoahDao.class);

        Version version = mock(Version.class);
        when(version.getVersionNumber()).thenReturn("0.0.0");

        VmStatusListenerRegistrar registrar = mock(VmStatusListenerRegistrar.class);

        WriterID id = mock(WriterID.class);
        backend = new VmShenandoahBackend(dao, version, registrar, id);
    }

    @Test
    public void testOrderValue() {
        int order = backend.getOrderValue();
        assertTrue(order >= Ordered.ORDER_MEMORY_GROUP);
        assertTrue(order < Ordered.ORDER_NETWORK_GROUP);
    }

    @Test
    public void testCreateVmListener() {
        assertNotNull(backend.createVmListener("foo-writerID", "foo-vmId", 100));
    }

}
