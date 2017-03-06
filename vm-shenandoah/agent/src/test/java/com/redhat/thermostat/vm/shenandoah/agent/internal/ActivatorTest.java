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

import com.redhat.thermostat.backend.Backend;
import com.redhat.thermostat.backend.BackendService;
import com.redhat.thermostat.storage.core.WriterID;
import com.redhat.thermostat.testutils.StubBundleContext;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivatorTest {

    private StubBundleContext context;
    private Activator activator;

    @Before
    public void setup() {
        context = new StubBundleContext();
        activator = new Activator();
    }

    @Test
    public void testActivatorDoesNothingWithMissingDeps() throws Exception {
        activator.start(context);

        assertTrue(context.getAllServices().isEmpty());
        assertNotSame(context.getServiceListeners().size(), 1);

        activator.stop(context);

        assertTrue(context.getAllServices().isEmpty());
        assertTrue(context.getServiceListeners().isEmpty());
    }

    @Test
    public void testRegistersBackend() throws Exception {
        context = new StubBundleContext() {
            @Override
            public Bundle getBundle() {
                Bundle result = mock(Bundle.class);
                when(result.getVersion()).thenReturn(Version.emptyVersion);
                return result;
            }
        };

        BackendService backendService = mock(BackendService.class);
        VmShenandoahDao vmShenandoahDao = mock(VmShenandoahDao.class);
        WriterID writerID = mock(WriterID.class);

        context.registerService(BackendService.class, backendService, null);
        context.registerService(VmShenandoahDao.class, vmShenandoahDao, null);
        context.registerService(WriterID.class, writerID, null);

        activator.start(context);

        assertTrue(context.isServiceRegistered(Backend.class.getName(), VmShenandoahBackend.class));

        activator.stop(context);

        assertFalse(context.isServiceRegistered(Backend.class.getName(), VmShenandoahBackend.class));
    }

}
