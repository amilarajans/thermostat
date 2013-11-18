/*
 * Copyright 2012, 2013 Red Hat, Inc.
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

package com.redhat.thermostat.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.shared.config.CommonPaths;
import com.redhat.thermostat.shared.config.InvalidConfigurationException;
import com.redhat.thermostat.utils.keyring.Keyring;

public class ClientPreferences {

    static final String USERNAME = "username";
    static final String CONNECTION_URL = "connection-url";
    static final String SAVE_ENTITLEMENTS = "save-entitlements";
    static final String DEFAULT_CONNECTION_URL = "mongodb://127.0.0.1:27518";

    private static final Logger logger = LoggingUtils.getLogger(ClientPreferences.class);

    private Properties prefs;
    private Keyring keyring;
    private File userConfig;

    private String url;
    private String username;
    private char[] password;

    public ClientPreferences(Keyring keyring, CommonPaths files) {
        userConfig = files.getUserClientConfigurationFile();
        Properties props = new Properties();
        loadPrefs(props);
        init(props, keyring);
    }

    private void loadPrefs(Properties props) {
        try {
            if (userConfig.isFile()) {
                try {
                    try (InputStream fis = new FileInputStream(userConfig)) {
                        props.load(fis);
                    }
                } catch (IOException e) {
                    logger.log(Level.CONFIG, "unable to load client configuration", e);
                }
            }
        } catch (InvalidConfigurationException e) {
            logger.log(Level.CONFIG, "unable to load configuration", e);
        }
    }

    // Testing hook with injectable j.u.Properties
    ClientPreferences(Properties properties, Keyring keyring) {
        init(properties, keyring);
    }

    private void init(Properties properties, Keyring keyring) {
        this.prefs = properties;
        this.keyring = keyring;
        this.url = DEFAULT_CONNECTION_URL;
        this.username = "";
        this.password = new char[]{};
    }

    public boolean getSaveEntitlements() {
        return Boolean.valueOf(prefs.getProperty(SAVE_ENTITLEMENTS, "false"));
    }
    
    public void setSaveEntitlements(boolean save) {
        prefs.setProperty(SAVE_ENTITLEMENTS, Boolean.toString(save));
    }
    
    public String getConnectionUrl() {
        return prefs.getProperty(CONNECTION_URL, DEFAULT_CONNECTION_URL);
    }

    public char[] getPassword() {
        if (getSaveEntitlements()) {
            return keyring.getPassword(getConnectionUrl(), getUserName());
        }
        return password;
    }

    public String getUserName() {
        if (getSaveEntitlements()) {
            return prefs.getProperty(USERNAME, "");
        }
        return username;
    }

    public void setConnectionUrl(String url) {
        this.url = url;
        if (getSaveEntitlements()) {
            prefs.put(CONNECTION_URL, url);
        }
    }

    public void setCredentials(String userName, char[] password) {
        this.username = userName;
        this.password = password;

        if (getSaveEntitlements()) {
            prefs.put(USERNAME, userName);
            keyring.savePassword(getConnectionUrl(), userName, password);
        }
    }
    
    public void flush() throws IOException {
        prefs.store(new FileWriter(userConfig), "");
        if (getSaveEntitlements()) {
            keyring.savePassword(getConnectionUrl(), username, password);
        }
    }
}

