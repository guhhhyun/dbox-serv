package com.dongkuksystems.dbox.daos.type.manager.approval;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.code.Code;

public interface ApprovalMapper {
	
	  public List<Code> selectApproval(@Param("uCodeVal1") String uCodeVal1); 


}
