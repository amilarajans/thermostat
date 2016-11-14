/*
 * Copyright 2012-2016 Red Hat, Inc.
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

package com.redhat.thermostat.agent.config;

import java.util.List;

import com.redhat.thermostat.common.utils.HostPortPair;
import com.redhat.thermostat.common.utils.HostPortsParser;
import com.redhat.thermostat.storage.config.StartupConfiguration;

public class AgentStartupConfiguration implements StartupConfiguration {

    private boolean purge;
    private String url;
    private long startTime;
    private HostPortPair listenAddr;
    private HostPortPair publishAddr;
    
    AgentStartupConfiguration() {
    }
    
    @Override
    public String getDBConnectionString() {
        return url;
    }

    public void setDatabaseURL(String url) {
        this.url = url;
    }
    
    // TODO: that should be a friend, we only want the Service to set this value
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getStartTime() {
        return startTime;
    }

    void setPurge(boolean purge) {
        this.purge = purge;
    }
    
    public boolean purge() {
        return purge;
    }

    public void setConfigListenAddress(String address) {
        this.listenAddr = parseAddress(address);
    }

    public HostPortPair getConfigListenAddress() {
        return listenAddr;
    }
    
    public void setConfigPublishAddress(String address) {
        this.publishAddr = parseAddress(address);
    }
    
    public HostPortPair getConfigPublishAddress() {
        if (publishAddr != null) {
            return publishAddr;
        }
        // Otherwise default to configured listen address
        // as the publish address for backwards compat reasons.
        return listenAddr;
    }

    private HostPortPair parseAddress(String address) throws AssertionError {
        HostPortsParser parser = new HostPortsParser(address);
        parser.parse();
        List<HostPortPair> list = parser.getHostsPorts();
        if (list.size() != 1) {
            throw new AssertionError("Multiple listen addresses not supported! Got: " + address);
        }
        return parser.getHostsPorts().get(0);
    }
}

