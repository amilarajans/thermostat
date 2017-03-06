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

import com.redhat.thermostat.client.core.views.BasicView;
import com.redhat.thermostat.common.ActionEvent;
import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.ApplicationService;
import com.redhat.thermostat.common.Timer;
import com.redhat.thermostat.common.TimerFactory;
import com.redhat.thermostat.shared.locale.Translate;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahView;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahViewProvider;
import com.redhat.thermostat.vm.shenandoah.client.core.locale.LocaleResources;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahDao;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VmShenandoahControllerTest {

    private static final Translate<LocaleResources> t = LocaleResources.createLocalizer();

    private ApplicationService appSvc;
    private VmRef ref;
    private VmShenandoahDao dao;
    private VmShenandoahViewProvider viewProvider;
    private VmShenandoahView view;
    private TimerFactory timerFactory;
    private Timer timer;

    private VmShenandoahController controller;

    @Before
    public void setup() {
        appSvc = mock(ApplicationService.class);
        ref = mock(VmRef.class);
        dao = mock(VmShenandoahDao.class);
        viewProvider = mock(VmShenandoahViewProvider.class);
        view = mock(VmShenandoahView.class);
        timerFactory = mock(TimerFactory.class);
        timer = mock(Timer.class);

        when(viewProvider.createView()).thenReturn(view);
        when(timerFactory.createTimer()).thenReturn(timer);
        when(appSvc.getTimerFactory()).thenReturn(timerFactory);

        controller = new VmShenandoahController(appSvc, ref, dao, viewProvider);
    }

    @Test
    public void testTabTitle() {
        assertThat(controller.getLocalizedName().getContents(), is(t.localize(LocaleResources.TAB_TITLE).getContents()));
    }

    @Test
    public void verifyCreatesView() {
        verify(viewProvider).createView();
    }

    @Test
    public void testReturnsCorrectView() {
        assertThat((VmShenandoahView) controller.getView(), is(view));
    }

    @Test
    public void verifyUsesTimerFactory() {
        verify(timerFactory).createTimer();
    }

    @Test
    public void verifyAddsViewActionListener() {
        verify(view).addActionListener(isA(ActionListener.class));
    }

    @Test
    public void testStopsOnViewHidden() {
        ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
        verify(view).addActionListener(captor.capture());
        ActionListener listener = captor.getValue();
        listener.actionPerformed(new ActionEvent(controller, BasicView.Action.HIDDEN));
        verify(timer).stop();
    }

    @Test
    public void testStartsOnViewVisible() {
        ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
        verify(view).addActionListener(captor.capture());
        ActionListener listener = captor.getValue();
        listener.actionPerformed(new ActionEvent(controller, BasicView.Action.VISIBLE));
        verify(timer).start();
    }

    @Test
    public void testUpdateActionWithNoDaoData() {
        when(dao.getCount(ref)).thenReturn(0L);
        when(dao.getLatestShenandoahStats(eq(ref), anyLong())).thenReturn(Collections.<VmShenandoahStat>emptyList());
        ArgumentCaptor<Runnable> actionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(timer).setAction(actionCaptor.capture());
        Runnable action = actionCaptor.getValue();
        action.run();

        InOrder inOrder = inOrder(dao, view);
        inOrder.verify(dao).getLatestShenandoahStats(eq(ref), anyLong());
        inOrder.verify(view).setEnabled(false);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testUpdateActionWithDaoData() {
        VmShenandoahStat stat1 = new VmShenandoahStat("writerID", "vmId", 150L, 50L, 2L, 1024L, 0L, new long[]{1L, 2L});
        VmShenandoahStat stat2 = new VmShenandoahStat("writerID", "vmId", 100L, 50L, 2L, 1024L, 1L, new long[]{3L, 4L});

        when(dao.getCount(ref)).thenReturn(2L);
        when(dao.getLatestShenandoahStats(eq(ref), anyLong())).thenReturn(Arrays.asList(stat1, stat2));
        ArgumentCaptor<Runnable> actionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(timer).setAction(actionCaptor.capture());
        Runnable action = actionCaptor.getValue();
        action.run();

        InOrder inOrder = inOrder(dao, view);
        inOrder.verify(dao).getLatestShenandoahStats(eq(ref), anyLong());
        inOrder.verify(view).setEnabled(true);
        inOrder.verify(view).setData(stat1);
        inOrder.verifyNoMoreInteractions();
    }

}
