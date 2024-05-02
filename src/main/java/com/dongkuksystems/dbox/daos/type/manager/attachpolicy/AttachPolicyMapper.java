package com.dongkuksystems.dbox.daos.type.manager.attachpolicy;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;

public interface AttachPolicyMapper {
	
	public List<AttachPolicy> selectAll(); 
	public Optional<AttachPolicy> selectOneByObjectId(@Param("rObjectId") String systemKey);
	public Optional<AttachPolicy> selectOneBySystemKey(@Param("uSystemKey") String systemKey);
	public List<AttachPolicy> selectAllAgentPolicy(@Param("uUserId") String uUserId);
	
}