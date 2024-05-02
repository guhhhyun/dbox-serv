package com.dongkuksystems.dbox.daos.type.manager.manageid;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdDto;
import com.dongkuksystems.dbox.models.type.manager.manageid.ManageId;

public interface ManageIdDao {
	
	public List<ManageId> selectUserId(ManageIdDto dto);
	
	public List<ManageId> selectUserIdLog(String uUserId, long offset, int limit);

	public int selectUserIdLogCount(String uUserId);
	
	public List<ManageId> selectCabinetCode(String orgId);
	
	public List<ManageId> selectRObjectId(String socialPerId);
	
	public List<ManageId> selectComCabinet(String comCode);	
	
	public List<ManageId> selectUserIdByDeptCode(String deptCode);
	
	public List<ManageId> selectGwUserData(String socialPerId);
}
