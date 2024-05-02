package com.dongkuksystems.dbox.daos.type.manager.duplication;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.duplication.DuplicationDto;
import com.dongkuksystems.dbox.models.type.manager.duplication.Duplication;

public interface DuplicationMapper {
	
	public List<Duplication> selectAll(@Param("duplication") DuplicationDto dto, @Param("offset") long offset, @Param("limit") int limit);
	
	public int selectAllCount(@Param("duplication") DuplicationDto dto);
	
	public List<Duplication> selectList(@Param("duplication") DuplicationDto dto);
	
	
}