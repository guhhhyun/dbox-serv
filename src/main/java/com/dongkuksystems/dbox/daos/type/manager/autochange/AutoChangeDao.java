package com.dongkuksystems.dbox.daos.type.manager.autochange;

import java.util.List;

import com.dongkuksystems.dbox.models.type.code.Code;

public interface AutoChangeDao {
	
	  public List<Code> selectAutoChange(String uCodeVal1);


}
