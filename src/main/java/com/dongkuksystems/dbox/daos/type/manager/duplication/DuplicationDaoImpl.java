package com.dongkuksystems.dbox.daos.type.manager.duplication;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.duplication.DuplicationDto;
import com.dongkuksystems.dbox.models.type.manager.duplication.Duplication;

@Primary
@Repository
public class DuplicationDaoImpl implements DuplicationDao {

	private DuplicationMapper duplicationMapper;

	public DuplicationDaoImpl(DuplicationMapper duplicationMapper) {
		this.duplicationMapper = duplicationMapper;
	}
	
	@Override
	public List<Duplication> selectAll(DuplicationDto dto, long offset, int limit) {		
		return duplicationMapper.selectAll(dto, offset, limit);
		
	}
	
	@Override
  public int selectAllCount(DuplicationDto dto) {
    return duplicationMapper.selectAllCount(dto);
  }
	
	@Override
	public List<Duplication> selectList(DuplicationDto dto) {		
		return duplicationMapper.selectList(dto);
	}
	

	
}
