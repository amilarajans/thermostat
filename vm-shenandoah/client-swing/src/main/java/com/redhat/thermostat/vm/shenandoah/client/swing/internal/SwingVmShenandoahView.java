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

import com.redhat.thermostat.client.swing.SwingComponent;
import com.redhat.thermostat.client.swing.components.HeaderPanel;
import com.redhat.thermostat.client.swing.experimental.ComponentVisibilityNotifier;
import com.redhat.thermostat.shared.locale.LocalizedString;
import com.redhat.thermostat.shared.locale.Translate;
import com.redhat.thermostat.vm.shenandoah.client.core.VmShenandoahView;
import com.redhat.thermostat.vm.shenandoah.client.core.locale.LocaleResources;
import com.redhat.thermostat.vm.shenandoah.common.VmShenandoahStat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;

public class SwingVmShenandoahView extends VmShenandoahView implements SwingComponent {

    private static final Translate<LocaleResources> t = LocaleResources.createLocalizer();

    private final HeaderPanel headerPanel = new HeaderPanel(t.localize(LocaleResources.TAB_DETAIL));
    private final ShenandoahVisualizer visualizer = new ShenandoahVisualizer();

    public SwingVmShenandoahView() {
        super();

        new ComponentVisibilityNotifier().initialize(headerPanel, notifier);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            headerPanel.setContent(visualizer);
        } else {
            JPanel statusPanel = new JPanel(new BorderLayout());

            String wrappedText = "<html>" + t.localize(LocaleResources.SHENANDOAH_UNAVAILABLE).getContents() + "</html>";
            JLabel descriptionLabel = new JLabel(wrappedText);
            statusPanel.add(descriptionLabel, BorderLayout.PAGE_START);

            headerPanel.setContent(statusPanel);
        }
    }

    @Override
    public void setData(final VmShenandoahStat vmShenandoahStat) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                visualizer.setStat(vmShenandoahStat);
            }
        });
    }

    @Override
    public Component getUiComponent() {
        return headerPanel;
    }
}
