package com.dongkuksystems.dbox.utils;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import lombok.extern.log4j.Log4j;

@Log4j
public class IDfAdminSessionResource implements AutoCloseable {

    private final IDfSession session;

    public IDfAdminSessionResource() throws Exception {
        session = DCTMUtils.getAdminSession();
    }

    public IDfSession getSession() {
        return session;
    }

    @Override
    public void close() throws Exception {
        if (session != null && session.isConnected()) {
            try {
                String sessionId = session.getSessionId();
                log.info(" - Disconnect session : " + sessionId);
                session.disconnect();
            } catch (DfException e) {
                e.printStackTrace();
            }
        }
    }
}
