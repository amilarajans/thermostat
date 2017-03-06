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

package com.redhat.thermostat.vm.shenandoah.client.core.internal;

import com.redhat.thermostat.client.core.controllers.InformationServiceController;
import com.redhat.thermostat.client.core.views.BasicView;
import com.redhat.thermostat.client.core.views.UIComponent;
import com.redhat.thermostat.common.ActionEvent;
import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.ApplicationService;
import com.redhat.thermostat.common.NotImplementedException;
import com.redhat.thermostat.common.Timer;
import com.redhat.thermostat.common.Timers;
import com.redhat.thermostat.shared.locale.LocalizedString;
import com.redhat.thermostat.shared.locale.Translate;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.model.TimeStampedPojoComparator;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahView;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahViewProvider;
import com.redhat.thermostat.vm.shenandoah.client.core.locale.LocaleResources;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VmShenandoahController implements InformationServiceController<VmRef> {

    private static final Translate<LocaleResources> t = LocaleResources.createLocalizer();

    private final VmRef ref;
    private final VmShenandoahDao dao;
    private final VmShenandoahView view;
    private final Timer timer;

    private long lastSeenTimestamp;

    public VmShenandoahController(ApplicationService appSvc, VmRef ref, VmShenandoahDao dao, VmShenandoahViewProvider viewProvider) {
        this.ref = ref;
        this.dao = dao;
        this.view = viewProvider.createView();

        timer = Timers.createDataRefreshTimer(appSvc.getTimerFactory(), new Runnable() {
            @Override
            public void run() {
                doUpdateData();
            }
        });
        timer.setDelay(1L);

        view.addActionListener(new ActionListener<BasicView.Action>() {
            @Override
            public void actionPerformed(ActionEvent<BasicView.Action> actionEvent) {
                switch (actionEvent.getActionId()) {
                    case HIDDEN:
                        stop();
                        break;
                    case VISIBLE:
                        start();
                        break;
                    default:
                        throw new NotImplementedException("unknown action: " + actionEvent.getActionId());
                }
            }
        });
    }

    private void start() {
        timer.start();
    }

    private void stop() {
        timer.stop();
    }

    private void doUpdateData() {
        List<VmShenandoahStat> stats = dao.getLatestShenandoahStats(ref, lastSeenTimestamp);
        boolean hasStats = !stats.isEmpty();
        view.setEnabled(hasStats);
        if (hasStats) {
            Collections.sort(stats, new TimeStampedPojoComparator<VmShenandoahStat>());
            VmShenandoahStat stat = stats.get(stats.size() - 1);
            lastSeenTimestamp = stat.getTimeStamp();
            view.setData(stat);
        }
    }

    @Override
    public UIComponent getView() {
        return view;
    }

    @Override
    public LocalizedString getLocalizedName() {
        return t.localize(LocaleResources.TAB_TITLE);
    }
}
