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

package com.redhat.thermostat.common.dao;

import com.redhat.thermostat.common.dao.Connection.ConnectionListener;
import com.redhat.thermostat.common.dao.Connection.ConnectionStatus;
import com.redhat.thermostat.common.storage.MongoStorage;
import com.redhat.thermostat.common.storage.Storage;

public class MongoDAOFactory implements DAOFactory {

    private Storage storage;
    private Connection connection;

    public MongoDAOFactory(ConnectionProvider connProv) {

        connection = connProv.createConnection();
        final MongoStorage mongoStorage = new MongoStorage(connection);
        connection.addListener(new ConnectionListener() {

            @Override
            public void changed(ConnectionStatus newStatus) {
                if (newStatus == ConnectionStatus.CONNECTED) {
                    mongoStorage.connect(((MongoConnection) connection).getDB());
                }
            }
        });
        storage = mongoStorage;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public VmCpuStatDAO getVmCpuStatDAO(VmRef ref) {
        return new VmCpuStatDAOImpl(storage, ref);
    }

    @Override
    public VmClassStatDAO getVmClassStatsDAO(VmRef ref) {
        return new VmClassStatDAOImpl(storage, ref);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public HostInfoDAO getHostInfoDAO(HostRef ref) {
        return new HostInfoDAOImpl(storage, ref);
    }

    @Override
    public CpuStatDAO getCpuStatDAO(HostRef ref) {
        return new CpuStatDAOImpl(storage, ref);
    }

    @Override
    public MemoryStatDAO getMemoryStatDAO(HostRef ref) {
        return new MemoryStatDAOImpl(storage, ref);
    }

    @Override
    public NetworkInterfaceInfoDAO getNetworkInterfaceInfoDAO(HostRef ref) {
        return new NetworkInterfaceInfoDAOImpl(storage, ref);
    }

    @Override
    public VmGcStatDAO getVmGcStatDAO(VmRef ref) {
        return new VmGcStatDAOImpl(storage, ref);
    }

    @Override
    public VmInfoDAO getVmInfoDAO(VmRef ref) {
        return new VmInfoDAOImpl(storage, ref);
    }

    public VmMemoryStatDAO getVmMemoryStatDAO(VmRef ref) {
        return new VmMemoryStatDAOImpl(storage, ref);
    }
}
