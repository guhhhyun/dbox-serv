package com.dongkuksystems.dbox.daos.type.manager.attachpolicyuser;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.AttachPolicyUserDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicyUser;

public interface AttachPolicyUserMapper {
	
	public List<AttachPolicyUser> selectAll(@Param("attachpolicyuser") AttachPolicyUserDto dto);
	
	public List<AttachPolicyUser> selectEndAttachUser(@Param("uPolicyId") String uPolicyId);
	
	public List<AttachPolicyUser> selectDeletePolicyUser(@Param("uPolicyId") String uPolicyId);
	
}