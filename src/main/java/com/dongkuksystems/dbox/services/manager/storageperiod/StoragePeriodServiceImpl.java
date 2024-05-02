package com.dongkuksystems.dbox.services.manager.storageperiod;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.storageperiod.StoragePeriodDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.PatchDeleteScheduleDto;
import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.StoragePeriodLogListDto;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriod;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriodLogList;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class StoragePeriodServiceImpl extends AbstractCommonService implements StoragePeriodService {

  private final StoragePeriodDao storagePeriodDao;

  public StoragePeriodServiceImpl(StoragePeriodDao storagePeriodDao) {
    this.storagePeriodDao = storagePeriodDao;
  }

  @Override
  public List<StoragePeriod> selectStoragePeriod(String uCodeVal1) {
    return storagePeriodDao.selectStoragePeriod(uCodeVal1);
  }

  @Override
  public List<StoragePeriod> selectDeleteSchedule(String uCodeVal1) {
    return storagePeriodDao.selectDeleteSchedule(uCodeVal1);
  }

  @Override
  @CacheEvict(value = "getConfigDeletePeriodMap", allEntries = true)
  public String patchStoragePeriod(String rObjectId, String uCodeVal3, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setString("u_code_val3", uCodeVal3);
      idf_PObj.setString("u_update_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_update_date", (new DfTime()).toString());
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }

  @Override
  public void patchDeleteSchedule(UserSession userSession, PatchDeleteScheduleDto dto) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    String s_Dql = PatchDeleteScheduleDto.PatchDeleteSchedule(idfSession, dto);

    try {
      IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(idfAdminSession, s_Dql, DfQuery.DF_READ_QUERY);
      if (idf_Col != null)
        idf_Col.close();
    } catch (Exception e) {
      throw e;
    } finally {
      idfAdminSession.disconnect();
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
  }

  @Override
  public List<StoragePeriodLogList> selectRecycleLog(StoragePeriodLogListDto dto) {
    return storagePeriodDao.selectRecycleLog(dto);
  }

  @Override
  public List<StoragePeriodLogList> selectDeleteLog(StoragePeriodLogListDto dto) {
    return storagePeriodDao.selectDeleteLog(dto);
  }
}
