package com.dongkuksystems.dbox.daos.type.manager.attachpolicyuser;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.AttachPolicyUserDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicyUser;

public interface AttachPolicyUserDao {

	public List<AttachPolicyUser> selectAll(AttachPolicyUserDto dto);
	
	public List<AttachPolicyUser> selectEndAttachUser(String uPolicyId);
	
	public List<AttachPolicyUser> selectDeletePolicyUser(String uPolicyId);   
}
