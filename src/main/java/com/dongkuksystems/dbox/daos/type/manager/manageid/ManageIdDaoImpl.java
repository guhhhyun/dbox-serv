package com.dongkuksystems.dbox.daos.type.manager.manageid;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdDto;
import com.dongkuksystems.dbox.models.type.manager.manageid.ManageId;

@Primary
@Repository
public class ManageIdDaoImpl implements ManageIdDao {
	private ManageIdMapper manageIdMapper;
	
	public ManageIdDaoImpl(ManageIdMapper manageIdMapper) {
		this.manageIdMapper = manageIdMapper;
	}

	
	@Override
	  public List<ManageId> selectUserId(ManageIdDto dto) {
	    return manageIdMapper.selectUserId(dto);
	  }
	
	@Override
	  public List<ManageId> selectUserIdLog(String uUserId, long offset, int limit) {
	    return manageIdMapper.selectUserIdLog(uUserId, offset, limit);
	  }
	
	 @Override
   public int selectUserIdLogCount(String uUserId) {
     return manageIdMapper.selectUserIdLogCount(uUserId);
   }
	
	@Override
	  public List<ManageId> selectCabinetCode(String orgId) {
	    return manageIdMapper.selectCabinetCode(orgId);
	  }

	@Override
	  public List<ManageId> selectRObjectId(String socialPerId) {
	    return manageIdMapper.selectRObjectId(socialPerId);
	  }
	
  @Override
  public List<ManageId> selectComCabinet(String comCode) {
    return manageIdMapper.selectComCabinet(comCode);
  }

  @Override
  public List<ManageId> selectGwUserData(String socialPerId) {
    return manageIdMapper.selectGwUserData(socialPerId);
  }  
  
  @Override
  public List<ManageId> selectUserIdByDeptCode(String deptCode) {
    return manageIdMapper.selectUserIdByDeptCode(deptCode);
  }
}
