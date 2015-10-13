/*
 * Copyright 2012-2015 Red Hat, Inc.
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

package com.redhat.thermostat.thread.client.controller.impl;

import com.redhat.thermostat.client.core.progress.ProgressNotifier;
import com.redhat.thermostat.common.ActionEvent;
import com.redhat.thermostat.common.ActionListener;
import com.redhat.thermostat.common.ApplicationCache;
import com.redhat.thermostat.common.ApplicationService;
import com.redhat.thermostat.common.Timer;
import com.redhat.thermostat.common.TimerFactory;
import com.redhat.thermostat.storage.core.HostRef;
import com.redhat.thermostat.storage.core.VmRef;
import com.redhat.thermostat.storage.dao.VmInfoDAO;
import com.redhat.thermostat.storage.model.VmInfo;
import com.redhat.thermostat.thread.client.common.ThreadTableBean;
import com.redhat.thermostat.thread.client.common.ThreadViewProvider;
import com.redhat.thermostat.thread.client.common.collector.ThreadCollector;
import com.redhat.thermostat.thread.client.common.collector.ThreadCollectorFactory;
import com.redhat.thermostat.thread.client.common.view.ThreadCountView;
import com.redhat.thermostat.thread.client.common.view.ThreadTableView;
import com.redhat.thermostat.thread.client.common.view.ThreadTableView.ThreadSelectionAction;
import com.redhat.thermostat.thread.client.common.view.ThreadTimelineView;
import com.redhat.thermostat.thread.client.common.view.ThreadView;
import com.redhat.thermostat.thread.client.common.view.VmDeadLockView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;

public class ThreadInformationControllerTest {

    private ThreadView view;

    private ActionListener<ThreadTableView.ThreadSelectionAction> threadTableActionListener;

    private ThreadViewProvider viewFactory;
    private ThreadInformationController controller;
    
    private ApplicationService appService;
    private ExecutorService appExecutor;

    private VmInfo vmInfo;
    private VmInfoDAO vmInfoDao;

    private ThreadTableView threadTableView;
    private VmDeadLockView deadLockView;
    private ThreadTimelineView threadTimelineView;
    private ThreadCountView threadCountView;

    @Before
    public void setUp() {

        appService = mock(ApplicationService.class);
        vmInfo = mock(VmInfo.class);
        when(vmInfo.isAlive()).thenReturn(true);
        vmInfoDao = mock(VmInfoDAO.class);
        when(vmInfoDao.getVmInfo(isA(VmRef.class))).thenReturn(vmInfo);
        setUpTimers();
        setupCache();
        setupExecutor();
        setUpView();
    }

    private void setUpView() {
        deadLockView = mock(VmDeadLockView.class);
        threadTableView = mock(ThreadTableView.class);
        threadTimelineView = mock(ThreadTimelineView.class);
        threadCountView = mock(ThreadCountView.class);
        
        view = mock(ThreadView.class);
        viewFactory = mock(ThreadViewProvider.class);
        when(viewFactory.createView()).thenReturn(view);
        
        when(view.createDeadLockView()).thenReturn(deadLockView);
        when(view.createThreadTableView()).thenReturn(threadTableView);
        when(view.createThreadTimelineView()).thenReturn(threadTimelineView);
        when(view.createThreadCountView()).thenReturn(threadCountView);

    }
    
    private void setUpTimers() {
        Timer timer = mock(Timer.class);

        TimerFactory timerFactory = mock(TimerFactory.class);
        when(timerFactory.createTimer()).thenReturn(timer);
        when(appService.getTimerFactory()).thenReturn(timerFactory);
    }
    
    private void setupCache() {
        ApplicationCache cache = mock(ApplicationCache.class);
        when(appService.getApplicationCache()).thenReturn(cache);
    }

    private void setupExecutor() {
        appExecutor = mock(ExecutorService.class);
        when(appService.getApplicationExecutor()).thenReturn(appExecutor);
    }

    private void setUpListeners() {        
        doNothing().when(view).addActionListener(any(ActionListener.class));
        
        ArgumentCaptor<ActionListener> threadTableViewCaptor = ArgumentCaptor.forClass(ActionListener.class);
        doNothing().when(threadTableView).addThreadSelectionActionListener(threadTableViewCaptor.capture());
        
        createController();
        
        threadTableActionListener = threadTableViewCaptor.getValue();
    }
    
    private void createController() {

        VmRef ref = mock(VmRef.class);
        HostRef agent = mock(HostRef.class);
        when(ref.getHostRef()).thenReturn(agent);
        when(agent.getAgentId()).thenReturn("0xcafe");

        ThreadCollectorFactory collectorFactory = mock(ThreadCollectorFactory.class);
        ThreadCollector collector = mock(ThreadCollector.class);
        when(collectorFactory.getCollector(ref)).thenReturn(collector);

        ProgressNotifier notifier = mock(ProgressNotifier.class);

        controller = new ThreadInformationController(ref, appService, vmInfoDao,
                                                     collectorFactory,
                                                     viewFactory, notifier);
    }
    
    @Test
    public void verifyViewCreateSubViewCalled() {
        
        createController();
        
        verify(view).createThreadTableView();
        verify(view).createDeadLockView();
        verify(view).createThreadTimelineView();
        verify(view).createThreadCountView();
    }
    
    @Test
    public void verifyLiveRecording() {
        
        ActionListener<ThreadView.ThreadAction> threadActionListener;
        ArgumentCaptor<ActionListener> viewArgumentCaptor = ArgumentCaptor.forClass(ActionListener.class);
        doNothing().when(view).addThreadActionListener(viewArgumentCaptor.capture());

        VmRef ref = mock(VmRef.class);
        when(ref.getVmId()).thenReturn("42");
        HostRef agent = mock(HostRef.class);
        when(ref.getHostRef()).thenReturn(agent);
        when(agent.getAgentId()).thenReturn("0xcafe");

        ThreadCollector collector = mock(ThreadCollector.class);
        when(collector.isHarvesterCollecting()).thenReturn(false).thenReturn(true);
        when(collector.startHarvester()).thenReturn(true);
        when(collector.stopHarvester()).thenReturn(true).thenReturn(false);

        ThreadCollectorFactory collectorFactory = mock(ThreadCollectorFactory.class);
        when(collectorFactory.getCollector(ref)).thenReturn(collector);

        ProgressNotifier notifier = mock(ProgressNotifier.class);

        ArgumentCaptor<Runnable> longRunningTaskCaptor = ArgumentCaptor.forClass(Runnable.class);
        doNothing().when(appExecutor).execute(longRunningTaskCaptor.capture());

        controller = new ThreadInformationController(ref, appService, vmInfoDao,
                                                     collectorFactory,
                                                     viewFactory, notifier);

        verify(collector).isHarvesterCollecting();
        verify(view, times(1)).setRecording(ThreadView.MonitoringState.STOPPED, false);

        // each action event posts a task to the executor.
        // make sure the task is posted and execute it manually in tests to see its effects.

        threadActionListener = viewArgumentCaptor.getValue();
        threadActionListener.actionPerformed(new ActionEvent<>(view, ThreadView.ThreadAction.START_LIVE_RECORDING));

        verify(appExecutor, times(1)).execute(isA(Runnable.class));
        longRunningTaskCaptor.getValue().run();

        verify(view, times(1)).setRecording(ThreadView.MonitoringState.STARTING, false);
        verify(view, times(1)).setRecording(ThreadView.MonitoringState.STARTED, false);
        verify(collector).startHarvester();

        threadActionListener.actionPerformed(new ActionEvent<>(view, ThreadView.ThreadAction.STOP_LIVE_RECORDING));

        verify(appExecutor, times(2)).execute(isA(Runnable.class));
        longRunningTaskCaptor.getValue().run();

        verify(collector).stopHarvester();
        verify(view, times(1)).setRecording(ThreadView.MonitoringState.STOPPING, false);
        verify(view, times(2)).setRecording(ThreadView.MonitoringState.STOPPED, false);

        threadActionListener.actionPerformed(new ActionEvent<>(view, ThreadView.ThreadAction.STOP_LIVE_RECORDING));

        verify(appExecutor, times(3)).execute(isA(Runnable.class));
        longRunningTaskCaptor.getValue().run();

        verify(collector, times(2)).stopHarvester();
        verify(view, times(2)).setRecording(ThreadView.MonitoringState.STOPPING, false);
        verify(view, times(3)).setRecording(ThreadView.MonitoringState.STOPPED, false);
    }

    @Test
    public void verifyRecordingControlDisabledForDeadVms() {
        when(vmInfo.isAlive()).thenReturn(false);

        createController();

        verify(view).setEnableRecordingControl(false);
    }
    
    @Test
    public void verifyTableViewLinksToDetailsView() {
        setUpListeners();

        ThreadTableBean bean = mock(ThreadTableBean.class);

        ActionEvent<ThreadSelectionAction> event =
                new ActionEvent<>(threadTableView, ThreadSelectionAction.SHOW_THREAD_DETAILS);
        event.setPayload(bean);
        
        threadTableActionListener.actionPerformed(event);
        verify(view).displayThreadDetails(bean);
    }
}

