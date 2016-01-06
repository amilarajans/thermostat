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

package com.redhat.thermostat.vm.compiler.client.core;

import java.util.List;

import com.redhat.thermostat.client.core.experimental.Duration;
import com.redhat.thermostat.client.core.views.BasicView;
import com.redhat.thermostat.client.core.views.UIComponent;
import com.redhat.thermostat.common.model.Range;
import com.redhat.thermostat.shared.locale.LocalizedString;
import com.redhat.thermostat.shared.locale.Translate;
import com.redhat.thermostat.storage.model.DiscreteTimeData;
import com.redhat.thermostat.vm.compiler.client.locale.LocaleResources;

public abstract class VmCompilerStatView extends BasicView implements UIComponent {

    private static final Translate<LocaleResources> t = LocaleResources.createLocalizer();

    public enum Type {
        TOTAL_COMPILES(LocaleResources.STATS_TOTAL_COMPILES),
        TOTAL_INVALIDATES(LocaleResources.STATS_TOTAL_INVALIDATES),
        TOTAL_BAILOUTS(LocaleResources.STATS_TOTAL_BAILOUTS),

        TOTAL_TIME(LocaleResources.STATS_COMPILATION_TIME),
        ;

        private final LocalizedString label;

        Type(LocaleResources resource) {
            this.label = t.localize(resource);
        }

        public LocalizedString getLabel() {
            return this.label;
        }
    }

    public static class ViewData {
        public String totalCompiles;
        public String totalBailouts;
        public String totalInvalidates;
        public String compilationTime;
        public String lastSize;
        public String lastType;
        public String lastMethod;
        public String lastFailedType;
        public String lastFailedMethod;
    }

    public abstract void setCurrentDisplay(ViewData stat);

    public abstract Duration getUserDesiredDuration();
    public abstract void setAvailableDataRange(Range<Long> availableDataRange);

    public abstract void addCompilerData(Type type, List<DiscreteTimeData<? extends Number>> data);
    public abstract void clearCompilerData(Type type);

}
