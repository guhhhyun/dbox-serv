package com.dongkuksystems.dbox.models.dto.type.manager.managerconfig;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.models.common.UserSession;
import org.apache.commons.lang3.StringUtils;

public class EnhancedUser extends UserSession {

    private String ipAddress;
    private String rObjectId;
    private IDfDocument idfDoc;

    public EnhancedUser idfDoc(IDfDocument idfDoc) {
        this.idfDoc = idfDoc;
        return this;
    }

    public EnhancedUser rObjectId(String dataId) {
        this.rObjectId = dataId;
        return this;
    }

    public EnhancedUser ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public EnhancedUser idfDoc(IDfSession idfAdminSession) throws DfException {
        if(StringUtils.isEmpty(rObjectId)) {
            // TODO Throw exception.
        }
        this.idfDoc = (IDfDocument) idfAdminSession.getObject(new DfId(rObjectId));
        return this;
    }

    public IDfDocument getIdfDoc() {
        return this.idfDoc;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }
}
