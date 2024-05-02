package com.dongkuksystems.dbox.daos.type.manager.manageid;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdDto;
import com.dongkuksystems.dbox.models.type.manager.manageid.ManageId;

public interface ManageIdMapper {
	
	public List<ManageId> selectUserId(@Param("manageId") ManageIdDto dto);
	
	public List<ManageId> selectUserIdLog(@Param("uUserId") String uUserId, @Param("offset") long offset, @Param("limit") int limit);
	
  public int selectUserIdLogCount(@Param("uUserId") String uUserId);

	public List<ManageId> selectCabinetCode(@Param("orgId") String orgId);
	
	public List<ManageId> selectRObjectId(@Param("socialPerId") String socialPerId);
	
	public List<ManageId> selectComCabinet(@Param("comCode") String comCode);

	public List<ManageId> selectUserIdByDeptCode(@Param("deptCode") String deptCode);
	
	public List<ManageId> selectGwUserData(@Param("socialPerId") String socialPerId);
}
