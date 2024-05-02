package com.dongkuksystems.dbox.services.manager.preservationperiod;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodMapper;
import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PreservationPeriodService extends AbstractCommonService {

    @Autowired
    private PreservationPeriodMapper preservationPeriodMapper;

    public Map<String, Object> selectPreservationPeriodByComCode(String comCode) {
        return preservationPeriodMapper.selectPreservationPeriodByComCode(comCode);
    }

    public List<Map<String, Object>> selectDepts() {
        return preservationPeriodMapper.selectDepts();
    }

    // TODO Refactoring. 엉망이다. 수정하자.
    public void patchDeptNotToUseAutoExtend(PreservationPeriodDto dto) {
        try {
            IDfPersistentObject idf_PObj = getIDfPersistentObject(dto);
            int index = idf_PObj.findString("u_no_ext_dept", dto.getUNoExtDept());
            boolean doesNotExistObject = index < 0;
            if (doesNotExistObject) {
                setObjectToInsert(idf_PObj, dto.getUNoExtDept());
            } else {
                setObjectToUpdate(idf_PObj, index);
            }
            idf_PObj.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setObjectToInsert(IDfPersistentObject idf_PObj, String value) throws DfException {
        idf_PObj.appendString("u_no_ext_dept", value);
        idf_PObj.appendTime("u_no_ext_reg_date", new DfTime());
        idf_PObj.appendString("u_no_ext_unreg_date", "");
    }

    private void setObjectToUpdate(IDfPersistentObject idf_PObj, int index) throws DfException {
        idf_PObj.setRepeatingTime("u_no_ext_reg_date", index, new DfTime());
        idf_PObj.setRepeatingString("u_no_ext_unreg_date", index, "");
    }

    public void patchDeptToUseAutoExtend(PreservationPeriodDto dto) {
        try {
            IDfPersistentObject idf_PObj = getIDfPersistentObject(dto);
            int index = idf_PObj.findString("u_no_ext_dept", dto.getUNoExtDept());
            idf_PObj.setRepeatingTime("u_no_ext_unreg_date", index, new DfTime());
            idf_PObj.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO Refactoring.
    public void patchPreservationPeriod(PreservationPeriodDto dto) {
        try {
            IDfPersistentObject idf_PObj = getIDfPersistentObject(dto);
            idf_PObj.setString("u_sec_s_year", dto.getUSecSYear());
            idf_PObj.setString("u_sec_t_year", dto.getUSecTYear());
            idf_PObj.setString("u_sec_c_year", dto.getUSecCYear());
            idf_PObj.setString("u_sec_g_year", dto.getUSecGYear());
            idf_PObj.setString("u_pjt_ever_flag", dto.getUPjtEverFlag());
            idf_PObj.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void patchAutoExtend(PreservationPeriodDto dto) {
        try {
            IDfPersistentObject idf_PObj = getIDfPersistentObject(dto);
            idf_PObj.setString("u_auto_extend", dto.getUAutoExtend());
            idf_PObj.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IDfPersistentObject getIDfPersistentObject(PreservationPeriodDto dto) throws Exception {
        return this.getIdfSession(dto.getUserSession()).getObject(new DfId(dto.getRObjectId()));
    }

}
