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

package com.redhat.thermostat.client.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.redhat.thermostat.client.Main;
import com.redhat.thermostat.client.UiFacadeFactory;
import com.redhat.thermostat.client.UiFacadeFactoryImpl;
import com.redhat.thermostat.client.osgi.service.ApplicationService;
import com.redhat.thermostat.client.osgi.service.ContextAction;
import com.redhat.thermostat.client.osgi.service.VmInformationService;

public class ThermostatActivator implements BundleActivator {

    private VmInformationServiceTracker vmInfoServiceTracker;
    private VMContextActionServiceTracker contextActionTracker;
    
    @Override
    public void start(final BundleContext context) throws Exception {
        UiFacadeFactory uiFacadeFactory = new UiFacadeFactoryImpl();
        
        vmInfoServiceTracker = new VmInformationServiceTracker(context, uiFacadeFactory);
        vmInfoServiceTracker.open();
        
        contextActionTracker =
                new VMContextActionServiceTracker(context, uiFacadeFactory);
        contextActionTracker.open();
        
        Main.main(uiFacadeFactory, new String[0]);
        context.registerService(ApplicationService.class.getName(), new ApplicationServiceProvider(), null);
        context.registerService(ContextAction.class.getName(), new ContextActionServiceProvider(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        vmInfoServiceTracker.close(); //context.removeServiceListener(vmInfoServiceTracker);
        contextActionTracker.close();
    }
}
