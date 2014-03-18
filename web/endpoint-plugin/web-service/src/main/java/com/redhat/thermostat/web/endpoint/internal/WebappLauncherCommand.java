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

package com.redhat.thermostat.web.endpoint.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.redhat.thermostat.common.ActionEvent;
import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.cli.AbstractStateNotifyingCommand;
import com.redhat.thermostat.common.cli.Command;
import com.redhat.thermostat.common.cli.CommandContext;
import com.redhat.thermostat.common.cli.CommandException;
import com.redhat.thermostat.common.tools.ApplicationState;
import com.redhat.thermostat.launcher.Launcher;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.SSLConfiguration;
import com.redhat.thermostat.shared.locale.LocalizedString;

class WebappLauncherCommand implements Command {

    // The time to wait after the agent finished and before the web endpoint
    // goes down. This increases the chance of emptying the queue.
    private static final long AGENT_SHUTDOWN_PAUSE = 200;
    
    private final BundleContext context;
    private final List<ActionListener<ApplicationState>> listeners;
    private final EmbeddedServletContainerConfiguration config;
    
    WebappLauncherCommand(BundleContext context) {
        this(context, null);
    }
    
    WebappLauncherCommand(BundleContext context, EmbeddedServletContainerConfiguration config) {
        this.context = context;
        listeners = new ArrayList<>();
        this.config = config;
    }
    
    // PRE: 1. Storage set up with credentials and credentials appropriately
    //         updated in the web archive's web.xml
    //      2. agent.auth file appropriately set up
    //      3. agent/client users appropriately defined in thermostat-users/
    //         thermostat-roles.properties files.
    @Override
    public void run(CommandContext ctx) throws CommandException {
        ServiceReference commonPathsRef = context.getServiceReference(CommonPaths.class.getName());
        if (commonPathsRef == null) {
            throw new CommandException(new LocalizedString("CommonPaths unavailable"));
        }
        CommonPaths paths = (CommonPaths)context.getService(commonPathsRef);
        ServiceReference launcherRef = context.getServiceReference(Launcher.class.getName());
        if (launcherRef == null) {
            throw new CommandException(new LocalizedString("Launcher Unavailable"));
        }
        Launcher launcher = (Launcher) context.getService(launcherRef);
        // start storage
        final CountDownLatch storageLatch = new CountDownLatch(1);
        StorageStartedListener storageListener = new StorageStartedListener(storageLatch);
        listeners.add(storageListener);
        String[] storageArgs = new String[] {
                "storage", "--start"
        };
        launcher.run(storageArgs, listeners, false);
        listeners.clear();
        try {
            storageLatch.await();
        } catch (InterruptedException e) {
            throw new CommandException(new LocalizedString("Interrupted waiting for storage"));
        }
        if (storageListener.startupFailed) {
            throw new CommandException(new LocalizedString("Starting mongodb storage failed"));
        }
        EmbeddedServletContainerConfiguration config = getConfiguration(paths);
        
        ServiceReference sslConfigRef = context.getServiceReference(SSLConfiguration.class.getName());
        if (sslConfigRef == null) {
            throw new CommandException(new LocalizedString("SSLConfiguration Unavailable"));
        }
        SSLConfiguration sslConfig = (SSLConfiguration) context.getService(sslConfigRef);
        JettyContainerLauncher jettyLauncher = new JettyContainerLauncher(config, sslConfig);
        CountDownLatch webStartedLatch = new CountDownLatch(1);
        // start web container with the web archive deployed
        jettyLauncher.startContainer(webStartedLatch);
        try {
            webStartedLatch.await();
        } catch (InterruptedException e) {
            // ignore
        }
        if (!jettyLauncher.isStartupSuccessFul()) {
            stopStorage(launcher);
            throw new CommandException(new LocalizedString("Failed to start embedded jetty instance"));
        }
        
        // start agent
        try {
            String[] agentArgs = new String[] {
                "agent", "-d", config.getConnectionUrl()
            };
            // This blocks
            launcher.run(agentArgs, false);
            
            ctx.getConsole().getOutput().println("Waiting " + AGENT_SHUTDOWN_PAUSE + "MS before exiting in order to empty queues");
            // Give the agent some time to finish it's work. Requests are
            // queued and it increases the chance of finishing requests before
            // exiting for sure.
            try {
                Thread.sleep(AGENT_SHUTDOWN_PAUSE);
            } catch (InterruptedException e) { } // ignore
        } finally {
            jettyLauncher.stopContainer();
            stopStorage(launcher);
        };
    }
    
    // testing hook
    private EmbeddedServletContainerConfiguration getConfiguration(CommonPaths paths) {
        if (this.config != null) {
            return config;
        }
        return new EmbeddedServletContainerConfiguration(paths);
    }

    // This fires up the web endpoint. No need to automatically firing up
    // storage.
    @Override
    public boolean isStorageRequired() {
        return false;
    }
    
    private void stopStorage(Launcher launcher) {
        String[] storageStopArgs = new String[] {
                "storage", "--stop"
        };
        launcher.run(storageStopArgs, false);
    }

    private static class StorageStartedListener implements ActionListener<ApplicationState> {

        private final CountDownLatch storageStarted;
        private boolean startupFailed;
        
        private StorageStartedListener(CountDownLatch latch) {
            storageStarted = latch;
        }
        
        @Override
        public void actionPerformed(ActionEvent<ApplicationState> actionEvent) {
            if (actionEvent.getSource() instanceof AbstractStateNotifyingCommand) {
                ApplicationState state = (ApplicationState) actionEvent.getActionId();
                switch(state) {
                case FAIL:
                    startupFailed = true;
                    // fall-through
                default:
                    storageStarted.countDown();
                    break;
                }
            }
        }
        
    }

}
