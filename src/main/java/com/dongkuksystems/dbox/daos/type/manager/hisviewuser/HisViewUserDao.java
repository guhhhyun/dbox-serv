package com.dongkuksystems.dbox.daos.type.manager.hisviewuser;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserFilterDto;
import com.dongkuksystems.dbox.models.type.manager.hisviewuser.HisViewUser; 


public interface HisViewUserDao {
	 public List<HisViewUser> selectAll(HisViewUserFilterDto hisViewUserFilterDto); 

}
