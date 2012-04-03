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

package com.redhat.thermostat.client.ui;

import static com.redhat.thermostat.client.locale.Translate.localize;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.redhat.thermostat.client.AsyncUiFacade;
import com.redhat.thermostat.client.appctx.ApplicationContext;
import com.redhat.thermostat.client.locale.LocaleResources;
import com.redhat.thermostat.common.dao.VmInfoDAO;
import com.redhat.thermostat.common.dao.VmRef;
import com.redhat.thermostat.common.model.VmInfo;

public class VmOverviewController implements AsyncUiFacade {

    private final VmRef ref;
    private final VmInfoDAO dao;
    private final DateFormat vmRunningTimeFormat;

    private final Timer timer;

    private final VmOverviewView view;

    public VmOverviewController(VmRef vmRef) {
        this.ref = vmRef;
        this.view = createView();

        dao = ApplicationContext.getInstance().getDAOFactory().getVmInfoDAO();

        vmRunningTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL);
        timer = new Timer();
    }

    @Override
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                VmInfo info = dao.getVmInfo(ref);
                view.setVmPid(((Integer) info.getVmPid()).toString());
                long actualStartTime = info.getStartTimeStamp();
                view.setVmStartTimeStamp(vmRunningTimeFormat.format(new Date(actualStartTime)));
                long actualStopTime = info.getStopTimeStamp();
                if (actualStopTime >= actualStartTime) {
                    // Only show a stop time if we have actually stopped.
                    view.setVmStopTimeStamp(vmRunningTimeFormat.format(new Date(actualStopTime)));
                } else {
                    view.setVmStopTimeStamp(localize(LocaleResources.VM_INFO_RUNNING));
                }
                view.setJavaVersion(info.getJavaVersion());
                view.setJavaHome(info.getJavaHome());
                view.setMainClass(info.getMainClass());
                view.setJavaCommandLine(info.getJavaCommandLine());
                String actualVmName = info.getVmName();
                view.setVmInfo(info.getVmInfo());
                String actualVmVersion = info.getVmInfo();
                view.setVmNameAndVersion(localize(LocaleResources.VM_INFO_VM_NAME_AND_VERSION,
                        actualVmName, actualVmVersion));
                view.setVmArguments(info.getVmArguments());
            }

        }, 0, TimeUnit.SECONDS.toMillis(5));

    }

    @Override
    public void stop() {
        timer.cancel();
    }

    protected VmOverviewView createView() {
        return new VmOverviewPanel();
    }

    public Component getComponent() {
        return view.getUiComponent();
    }
}
