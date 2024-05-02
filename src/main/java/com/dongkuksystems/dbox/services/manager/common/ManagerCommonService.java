package com.dongkuksystems.dbox.services.manager.common;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.IDfAdminSessionResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerCommonService extends AbstractCommonService {

    private IDfSession idfSession;

    public void deleteObjectWithAdminSession(String rObjectId) {
        if (StringUtils.isEmpty(rObjectId)) {
            // TODO throw exception.
            return;
        }
        try (IDfAdminSessionResource resource = new IDfAdminSessionResource()) {
            IDfPersistentObject idf_PObj = resource.getSession().getObject(new DfId(rObjectId));
            idf_PObj.destroy();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteObjects(UserSession userSession, List<String> rObjectIds) {
        try {
            setIdfSession(this.getIdfSession(userSession));
            rObjectIds.forEach(this::deleteObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteObject(String rObjectId) {
        try {
            if (idfSession == null) {
                throw new DfException("There's no session for destroying object.");
            }
            IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
            idf_PObj.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIdfSession(IDfSession idfSession) {
        this.idfSession = idfSession;
    }

}
