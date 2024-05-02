package com.dongkuksystems.dbox.services.recycle;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.DocRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.RecycleDetailDto;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.recycle.Recycle;

public interface RecycleService {
  	public List<RecycleDetailDto> getDeletedDataByOrgId(UserSession userSession, String dataId) throws Exception;
	public List<RecycleDetailDto> getDeletedDataList(UserSession userSession, boolean isMobile) throws Exception;
	public String restoreDataByOrgId(UserSession userSession, String dataId, String ip, DeleteManageDto dto) throws Exception;
	public Map<String, Integer> restoreAllDataByIds(UserSession userSession, List<String> retoreAllList, String ip) throws Exception;
	public String deleteData(UserSession userSession, String dataId, String ip, DeleteManageDto dto) throws Exception;
	public Map<String, Integer> deleteAllData(UserSession userSession, List<String> deleteAllList, String ip) throws Exception;
	public Recycle getDeletedData(String dataId) throws Exception;
	public Optional<Recycle> selectRecycleCaCode(String orgId);
}
