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

package com.redhat.thermostat.setup.command.internal.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.thermostat.common.config.ClientPreferences;
import com.redhat.thermostat.common.utils.LoggingUtils;
import com.redhat.thermostat.utils.keyring.Keyring;
import com.redhat.thermostat.utils.keyring.KeyringException;

class KeyringWriter extends CredentialsWriter {
    
    private static final Logger logger = LoggingUtils.getLogger(KeyringWriter.class);
    private final ClientPreferences prefs;
    private final Keyring keyring;
    private String storageUrl;
    
    KeyringWriter(ClientPreferences prefs, Keyring keyring) {
        this.prefs = prefs;
        this.keyring = keyring;
    }
    
    void setStorageUrl(String customStorageUrl) {
        this.storageUrl = customStorageUrl;
    }
    
    public void write() throws IOException {
        String username = getUsername();
        char[] password = getPassword();
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        Objects.requireNonNull(storageUrl);
        try {
            prefs.setSaveEntitlements(true); // force writing on flush()
            prefs.setConnectionUrl(storageUrl);
            prefs.setUserName(username);
            // Unconditionally save credentials for the chosen username. If setup
            // runs again it will overwrite existing credentials.
            prefs.flush();
            keyring.savePassword(storageUrl, username, password);
        } catch (KeyringException e) {
            // Don't fail setup if storing to keyring fails. After all this is
            // for convenience only (so that thermostat gui works out of the box
            // after setup). If we failed, one would have to have a working
            // keyring setup which isn't always the case. Think thermostat agent.
            logger.log(Level.INFO, "Failed to store client credentials to keyring. Usability of client commands might suffer.", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}