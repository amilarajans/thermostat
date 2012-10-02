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

package com.redhat.thermostat.client.internal;


import org.apache.commons.cli.Options;
import org.osgi.framework.BundleContext;

import com.redhat.thermostat.client.internal.osgi.ApplicationServiceProvider;
import com.redhat.thermostat.client.internal.osgi.ContextActionServiceProvider;
import com.redhat.thermostat.client.osgi.service.ApplicationService;
import com.redhat.thermostat.client.osgi.service.ContextAction;
import com.redhat.thermostat.common.cli.CommandContext;
import com.redhat.thermostat.common.cli.CommandException;
import com.redhat.thermostat.common.cli.OSGiContext;
import com.redhat.thermostat.common.cli.SimpleCommand;

public class GUIClientCommand extends SimpleCommand implements OSGiContext {

    private BundleContext context;
    private Main clientMain;

    public GUIClientCommand(Main clientMain) {
        this.clientMain = clientMain;
    }

    @Override
    public void setBundleContext(BundleContext context) {
        this.context = context;
    }
    
    @Override
    public void run(CommandContext ctx) throws CommandException {
        ApplicationService service = new ApplicationServiceProvider();
        
        context.registerService(ApplicationService.class.getName(), service, null);
        context.registerService(ContextAction.class.getName(), new ContextActionServiceProvider(), null);
        
        // this blocks, everything else needs to be done before
        clientMain.run();

        service.getApplicationExecutor().shutdown();
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public String getUsage() {
        return "thermostat gui";
    }

    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public boolean isStorageRequired() {
        return false;
    }

}
