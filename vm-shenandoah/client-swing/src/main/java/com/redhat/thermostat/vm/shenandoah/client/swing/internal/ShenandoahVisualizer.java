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

package com.redhat.thermostat.vm.shenandoah.client.swing.internal;

import com.redhat.thermostat.common.Clock;
import com.redhat.thermostat.shared.locale.Translate;
import com.redhat.thermostat.vm.shenandoah.client.swing.internal.locale.LocaleResources;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.RegionFlag.HUMONGOUS;
import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.RegionFlag.IN_COLLECTION_SET;
import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.RegionFlag.PINNED;
import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.RegionFlag.RECENTLY_ALLOCATED;
import static com.redhat.thermostat.vm.shenandoah.client.swing.internal.RegionFlag.UNUSED;

/**
 * See http://icedtea.classpath.org/people/rkennke/shenandoahvisualizer
 */
class ShenandoahVisualizer extends JPanel {

    private static final Translate<LocaleResources> t = LocaleResources.createLocalizer();

    private final Render render;
    private VmShenandoahStat stat;

    ShenandoahVisualizer() {
        setLayout(new GridBagLayout());

        DataProvider dataProvider = new DataProvider();

        this.render = new Render(dataProvider, this);

        JPanel regionsPanel = new JPanel() {
            public void paint(Graphics g) {
                render.renderRegions(g);
            }
        };

        JPanel legendPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                render.renderLegend(g);
            }
        };

        JPanel statusPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                render.renderStats(g);
            }
        };

        JPanel graphPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                render.renderGraph(g);
            }
        };

        regionsPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ev) {
                render.notifyRegionResized(ev.getComponent().getWidth(), ev.getComponent().getHeight());
            }
        });

        graphPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ev) {
                render.notifyGraphResized(ev.getComponent().getWidth(), ev.getComponent().getHeight());
            }
        });

        Insets pad = new Insets(8, 2, 8, 2);

        {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 3;
            c.weighty = 1;
            c.insets = pad;
            this.add(graphPanel, c);
        }

        {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 3;
            c.weighty = 5;
            c.insets = pad;
            this.add(regionsPanel, c);
        }

        {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 2;
            c.weighty = 1;
            c.insets = pad;
            this.add(statusPanel, c);
        }

        {
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 2;
            c.weighty = 1;
            c.insets = pad;
            this.add(legendPanel, c);
        }
    }

    public void setStat(VmShenandoahStat stat) {
        this.stat = stat;
        render.run();
    }

    private static class Render implements Runnable {
        static final Map<TextAttribute, Object> TEXT_ATTRIBUTES = new HashMap<>();

        static {
            TEXT_ATTRIBUTES.put(TextAttribute.FAMILY, Font.SANS_SERIF);
            TEXT_ATTRIBUTES.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_LIGHT);
            TEXT_ATTRIBUTES.put(TextAttribute.SIZE, 11);
        }

        final DataProvider data;
        final Container container;

        int regionWidth, regionHeight;
        int graphWidth, graphHeight;

        final Deque<SnapshotView> lastSnapshots;
        volatile Snapshot snapshot;

        public Render(DataProvider data, Container container) {
            this.data = data;
            this.container = container;
            this.lastSnapshots = new LinkedList<>();
            this.snapshot = data.snapshot();
        }

        @Override
        public synchronized void run() {
            Snapshot cur = data.snapshot();
            if (!cur.equals(snapshot)) {
                snapshot = cur;
                lastSnapshots.add(new SnapshotView(cur));
                if (lastSnapshots.size() > graphWidth) {
                    lastSnapshots.removeFirst();
                }
                container.repaint();
            }
        }

        public synchronized void renderGraph(Graphics g) {
            if (lastSnapshots.size() < 2) return;

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, graphWidth, graphHeight);

            double stepY = 1D * graphHeight / snapshot.total();
            long firstTime = lastSnapshots.getFirst().time();
            long lastTime = lastSnapshots.getLast().time();
            double stepX = 1D * Math.min(lastSnapshots.size(), graphWidth) / (lastTime - firstTime);
            for (SnapshotView s : lastSnapshots) {
                int x = (int) Math.round((s.time() - firstTime) * stepX);

                if (s.isMarking()) {
                    g.setColor(new Color(100, 100, 0));
                    g.drawRect(x, 0, 1, graphHeight);
                }

                if (s.isEvacuating()) {
                    g.setColor(new Color(100, 0, 0));
                    g.drawRect(x, 0, 1, graphHeight);
                }

                g.setColor(Colors.USED.getColor());
                g.drawRect(x, (int) Math.round(graphHeight - s.used() * stepY), 1, 1);
                g.setColor(Colors.USED_ALLOC.getColor());
                g.drawRect(x, (int) Math.round(graphHeight - s.recentlyAllocated() * stepY), 1, 1);
                g.setColor(Colors.HUMONGOUS.getColor());
                g.drawRect(x, (int) Math.round(graphHeight - s.humongous() * stepY), 1, 1);
                g.setColor(Colors.LIVE.getColor());
                g.drawRect(x, (int) Math.round(graphHeight - s.live() * stepY), 1, 1);
                g.setColor(Colors.CSET.getColor());
                g.drawRect(x, (int) Math.round(graphHeight - s.collectionSet() * stepY), 1, 1);
            }
        }

        public synchronized void renderLegend(Graphics g) {
            g.setFont(Font.getFont(TEXT_ATTRIBUTES));
            final int sqSize = line();

            Map<String, RegionStat> items = new LinkedHashMap<>();

            items.put(t.localize(LocaleResources.UNUSED_REGION_LABEL).getContents(),
                    new RegionStat(0.0, 0.0, EnumSet.of(UNUSED)));

            items.put(t.localize(LocaleResources.EMPTY_REGION_LABEL).getContents(),
                    new RegionStat(0.0, 0.0, EnumSet.noneOf(RegionFlag.class)));

            items.put(t.localize(LocaleResources.HALF_USED_REGION_LABEL).getContents(),
                    new RegionStat(0.5, 0.0, EnumSet.noneOf(RegionFlag.class)));

            items.put(t.localize(LocaleResources.FULLY_USED_REGION_LABEL).getContents(),
                    new RegionStat(1.0, 0.0, EnumSet.noneOf(RegionFlag.class)));

            items.put(t.localize(LocaleResources.FULLY_USED_RECENTLY_ALLOCATED_REGION_LABEL).getContents(),
                    new RegionStat(1.0, 0.0, EnumSet.of(RECENTLY_ALLOCATED)));

            items.put(t.localize(LocaleResources.FULLY_LIVE_REGION_LABEL).getContents(),
                    new RegionStat(1.0, 1.0, EnumSet.noneOf(RegionFlag.class)));

            items.put(t.localize(LocaleResources.FULLY_LIVE_HUMONGOUS_REGION_LABEL).getContents(),
                    new RegionStat(1.0, 1.0, EnumSet.of(HUMONGOUS)));

            items.put(t.localize(LocaleResources.THIRD_LIVE_REGION_LABEL).getContents(),
                    new RegionStat(1.0, 0.3, EnumSet.noneOf(RegionFlag.class)));

            items.put(t.localize(LocaleResources.THIRD_LIVE_COLLECITON_SET_REGION_LABEL).getContents(),
                    new RegionStat(1.0, 0.3, EnumSet.of(IN_COLLECTION_SET)));

            items.put(t.localize(LocaleResources.THIRD_LIVE_PINNED_REGION_LABEL).getContents(),
                    new RegionStat(1.0, 0.3, EnumSet.of(PINNED)));

            int i = 1;
            for (String key : items.keySet()) {
                int y = (int) (i * sqSize * 1.5);
                items.get(key).render(g, 0, y, sqSize, sqSize);
                g.drawString(key, (int) (sqSize * 1.5), y + sqSize);
                i++;
            }
        }

        public synchronized void renderRegions(Graphics g) {
            try {
                int area = regionWidth * regionHeight;
                int sqSize = Math.max(1, (int) Math.sqrt(1D * area / snapshot.regionCount()));
                int cols = regionWidth / sqSize;

                for (int i = 0; i < snapshot.regionCount(); i++) {
                    int rectx = (i % cols) * sqSize;
                    int recty = (i / cols) * sqSize;

                    RegionStat s = snapshot.get(i);
                    s.render(g, rectx, recty, sqSize, sqSize);
                }
            } catch (ArithmeticException ignored) {
            }
        }

        public synchronized void renderStats(Graphics g) {
            g.setFont(Font.getFont(TEXT_ATTRIBUTES));

            String status;
            if (snapshot.isMarking() && snapshot.isEvacuating()) {
                status = t.localize(LocaleResources.MARKING_AND_EVACUATING_STATUS).getContents();
            } else if (snapshot.isMarking()) {
                status = t.localize(LocaleResources.MARKING_STATUS).getContents();
            } else if (snapshot.isEvacuating()) {
                status = t.localize(LocaleResources.EVACUATING_STATUS).getContents();
            } else {
                status = t.localize(LocaleResources.IDLE_STATUS).getContents();
            }

            g.setColor(Color.BLACK);
            g.drawString(t.localize(LocaleResources.TIME_STAT_LABEL, Clock.DEFAULT_DATE_FORMAT.format(new Date(snapshot.time()))).getContents(),
                    0, 1 * line());
            g.drawString(t.localize(LocaleResources.STATUS_STAT_LABEL, status).getContents(), 0, 2 * line());
            g.drawString(t.localize(LocaleResources.TOTAL_STAT_LABEL, Long.toString(snapshot.total())).getContents(), 0, 3 * line());
            g.drawString(t.localize(LocaleResources.USED_STAT_LABEL, Long.toString(snapshot.used())).getContents(), 0, 4 * line());
            g.drawString(t.localize(LocaleResources.LIVE_STAT_LABEL, Long.toString(snapshot.live())).getContents(), 0, 5 * line());
        }

        public synchronized void notifyRegionResized(int width, int height) {
            this.regionWidth = width;
            this.regionHeight = height;
        }

        public synchronized void notifyGraphResized(int width, int height) {
            this.graphWidth = width;
            this.graphHeight = height;
        }

        private int line() {
            int result = (int) (container.getSize().getHeight() / 30);
            if (result % 2 == 1) {
                result--;
            }
            return result;
        }
    }

    private class DataProvider {
        public Snapshot snapshot() {
            if (stat == null) {
                return new Snapshot(0L, 0L, Collections.<RegionStat>emptyList(), false, false);
            }
            long maxSize = stat.getRegionSize();
            long[] data = stat.getRegionData();
            long timestamp = stat.getTimeStamp();
            long status = stat.getRegionStatus();

            List<RegionStat> stats = new ArrayList<>();
            for (long l : data) {
                stats.add(new RegionStat(maxSize, l));
            }
            boolean isMarking = (status & 0x1) > 0;
            boolean isEvacuating = (status & 0x2) > 0;

            return new Snapshot(timestamp, maxSize, stats, isMarking, isEvacuating);
        }
    }
}


