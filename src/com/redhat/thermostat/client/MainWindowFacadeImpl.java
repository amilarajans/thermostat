package com.redhat.thermostat.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.redhat.thermostat.common.utils.LoggingUtils;

public class MainWindowFacadeImpl implements MainWindowFacade {

    private static final Logger logger = LoggingUtils.getLogger(MainWindowFacadeImpl.class);

    private DB db;
    private DBCollection agentConfigCollection;
    private DBCollection vmInfoCollection;

    public MainWindowFacadeImpl(DB db) {
        this.db = db;
        this.agentConfigCollection = db.getCollection("agent-config");
        this.vmInfoCollection = db.getCollection("vm-info");
    }

    @Override
    public HostRef[] getHosts() {
        List<HostRef> hostRefs = new ArrayList<HostRef>();

        DBCursor cursor = agentConfigCollection.find();
        while (cursor.hasNext()) {
            DBObject doc = cursor.next();
            String id = (String) doc.get("agent-id");
            if (id != null) {
                HostRef agent = new HostRef(id, id);
                hostRefs.add(agent);
            }
        }
        logger.log(Level.FINER, "found " + hostRefs.size() + " connected agents");
        return hostRefs.toArray(new HostRef[0]);
    }

    @Override
    public VmRef[] getVms(HostRef hostRef) {
        List<VmRef> vmRefs = new ArrayList<VmRef>();
        DBCursor cursor = vmInfoCollection.find(new BasicDBObject("agent-id", hostRef.getAgentId()));
        while (cursor.hasNext()) {
            DBObject vmObject = cursor.next();
            String id = (String) vmObject.get("vm-id");
            VmRef ref = new VmRef(hostRef, id, id);
            vmRefs.add(ref);
        }

        return vmRefs.toArray(new VmRef[0]);
    }


}
