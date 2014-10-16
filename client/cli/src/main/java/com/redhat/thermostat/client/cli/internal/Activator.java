/*
 * Copyright 2012-2014 Red Hat, Inc.
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

package com.redhat.thermostat.client.cli.internal;

import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.redhat.thermostat.common.MultipleServiceTracker;
import com.redhat.thermostat.common.MultipleServiceTracker.Action;
import com.redhat.thermostat.common.cli.CommandRegistry;
import com.redhat.thermostat.common.cli.CommandRegistryImpl;
import com.redhat.thermostat.common.config.ClientPreferences;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.storage.dao.AgentInfoDAO;
import com.redhat.thermostat.utils.keyring.Keyring;

public class Activator implements BundleActivator {

    private CommandRegistry reg = null;
    private MultipleServiceTracker tracker;

    private MultipleServiceTracker agentTracker;
    private final ListAgentsCommand listAgentsCommand = new ListAgentsCommand();

    @Override
    public void start(final BundleContext context) throws Exception {
        reg = new CommandRegistryImpl(context);

        reg.registerCommand("list-vms", new ListVMsCommand());
        reg.registerCommand("vm-info", new VMInfoCommand());
        reg.registerCommand("vm-stat", new VMStatCommand());
        reg.registerCommand("disconnect", new DisconnectCommand());
        reg.registerCommand("clean-data", new CleanDataCommand(context));

        Class<?>[] classes = new Class[] {
            Keyring.class,
            CommonPaths.class,
        };
        tracker = new MultipleServiceTracker(context, classes, new Action() {

            @Override
            public void dependenciesAvailable(Map<String, Object> services) {
                Keyring keyring = (Keyring) services.get(Keyring.class.getName());
                CommonPaths paths = (CommonPaths) services.get(CommonPaths.class.getName());
                ClientPreferences prefs = new ClientPreferences(paths);
                reg.registerCommand("connect", new ConnectCommand(prefs, keyring));
                reg.registerCommand("shell", new ShellCommand(context, paths));
            }

            @Override
            public void dependenciesUnavailable() {
                reg.unregisterCommand("connect");
            }
            
        });
        tracker.open();

        Class<?>[] agentClasses = new Class[] {
                AgentInfoDAO.class,
        };
        agentTracker = new MultipleServiceTracker(context, agentClasses, new Action() {
            @Override
            public void dependenciesAvailable(Map<String, Object> services) {
                AgentInfoDAO agentInfoDAO = (AgentInfoDAO) services.get(AgentInfoDAO.class.getName());
                listAgentsCommand.setAgentInfoDAO(agentInfoDAO);
            }

            @Override
            public void dependenciesUnavailable() {
                listAgentsCommand.setAgentInfoDAO(null);
            }
        });
        agentTracker.open();

        reg.registerCommand("list-agents", listAgentsCommand);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        tracker.close();
        agentTracker.close();
        reg.unregisterCommands();
    }

}

