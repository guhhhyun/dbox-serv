package com.dongkuksystems.dbox.services.manager.autoclosing;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.autoclosing.AutoClosingMapper;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.AutoClosingDto;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.manager.common.ManagerCommonService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AutoClosingService extends AbstractCommonService {

    @Autowired
    private AutoClosingMapper autoClosingMapper;

    @Autowired
    private ManagerCommonService managerCommonService;

    public Map<String, Object> selectPeriodToCloseByComCode(String comCode) {
        return autoClosingMapper.selectPeriodToCloseByComCode(comCode);
    }

    public List<Map<String, Object>> selectDataToClose(AutoClosingDto autoClosingDto) {
        return autoClosingMapper.selectDataToClose(autoClosingDto);
    }

    public List<Map<String, Object>> selectDataByDocKeyToClose(String docKey) {
        return autoClosingMapper.selectDataByDocKeyToClose(docKey);
    }

    // FIXME Refactoring.
    @CacheEvict(value = "getConfigVerDelPeriodMap", allEntries = true)
    public void patchPeriodToCloseByComCode(UserSession userSession, AutoClosingDto autoClosingDto) {
        try {
            IDfSession idfSession = this.getIdfSession(userSession);
            IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(autoClosingDto.getRObjectId()));
            idf_PObj.setString("u_code_val2", autoClosingDto.getUCodeVal2());
            idf_PObj.setString("u_code_val3", autoClosingDto.getUCodeVal3());
            idf_PObj.setString("u_update_user", idfSession.getLoginUserName());
            idf_PObj.setString("u_update_date", (new DfTime()).toString());
            idf_PObj.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteObject(String objectId) {
        managerCommonService.deleteObjectWithAdminSession(objectId);
    }

}
