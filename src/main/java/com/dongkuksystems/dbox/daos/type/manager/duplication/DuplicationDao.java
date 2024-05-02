package com.dongkuksystems.dbox.daos.type.manager.duplication;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.duplication.DuplicationDto;
import com.dongkuksystems.dbox.models.type.manager.duplication.Duplication;

public interface DuplicationDao {

	public List<Duplication> selectAll(DuplicationDto dto, long offset, int limit); 
	
	public int selectAllCount(DuplicationDto dto);
	
	public List<Duplication> selectList(DuplicationDto dto);


	
}
