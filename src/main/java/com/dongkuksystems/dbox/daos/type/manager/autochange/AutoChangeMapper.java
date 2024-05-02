package com.dongkuksystems.dbox.daos.type.manager.autochange;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.code.Code;

public interface AutoChangeMapper {

	  public List<Code> selectAutoChange(@Param("uCodeVal1") String uCodeVal1); 

}
