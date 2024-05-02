package com.dongkuksystems.dbox.daos.type.manager.approval;

import java.util.List;

import com.dongkuksystems.dbox.models.type.code.Code;

public interface ApprovalDao {
	
	  public List<Code> selectApproval(String uCodeVal1);


}
