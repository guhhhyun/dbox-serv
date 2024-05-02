package com.dongkuksystems.dbox.services.manager.lockeddata;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.UploadFlag;
import com.dongkuksystems.dbox.daos.type.manager.lockeddata.LockedDataMapper;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.LockedDataDto;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class LockedDataService extends AbstractCommonService {

    @Autowired
    private LockedDataMapper lockedDataMapper;
    
    @Autowired
    private DocService docService;      

    public List<Map<String, Object>> selectDataLocked(LockedDataDto lockedDataDto) {
        return lockedDataMapper.selectLockedData(lockedDataDto);  
    }
    
    public void unlockData(String rObjectId, UserSession userSession, String ip, String userType) throws Exception {
      IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
      try {
        Doc doc = docService.selectOne(rObjectId, true).orElse(new Doc());
        cancelCheckout((IDfDocument) idfAdminSession.getObject(new DfId(doc.getRObjectId())), userSession, ip, userType);
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
        if (idfAdminSession != null && idfAdminSession.isConnected()) {
          idfAdminSession.disconnect();
        }
      }
    }

    private void cancelCheckout(IDfDocument iDfDocument, UserSession userSession, String ip, String userType) throws Exception {
      iDfDocument.cancelCheckout();
      insertLog(iDfDocument, userSession, ip, userType);
    }

    private void insertLog(IDfDocument iDfDocument, UserSession userSession, String ip, String userType) throws Exception {
        Doc doc = docService.selectOne(iDfDocument.getString("r_object_id")).orElseThrow(() -> new NotFoundException("Doc not found"));
    
        LogDoc logDoc = LogDoc.builder()
                .uJobCode(DocLogItem.CC.getValue())
                .uDocId(doc.getRObjectId())
                .uDocKey(doc.getUDocKey())
                .uDocName(doc.getObjectName())
                .uDocVersion(Integer.parseInt(iDfDocument.getVersionLabel(0).substring(0, iDfDocument.getVersionLabel(0).indexOf(".")))+"")
                .uFileSize(Long.parseLong(iDfDocument.getString("r_content_size")))
                .uOwnDeptcode(doc.getUDeptCode())
                .uActDeptCode(userSession.getUser().getOrgId())
                .uJobUser(userSession.getUser().getUserId())
                .uJobUserType(userType) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
                .uDocStatus(DocStatus.LIVE.getValue())
                .uSecLevel(iDfDocument.getString("u_sec_level"))
                .uCabinetCode(iDfDocument.getString("u_cabinet_code"))
                .uJobGubun(UploadFlag.NEW_D.getValue())
                .uUserIp(ip)
//                .uAttachSystem(syspath)
                .build();
        insertLog(logDoc);
    }
}
