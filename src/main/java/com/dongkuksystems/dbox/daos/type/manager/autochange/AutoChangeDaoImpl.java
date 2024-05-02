package com.dongkuksystems.dbox.daos.type.manager.autochange;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.code.Code;

@Primary
@Repository
public class AutoChangeDaoImpl implements AutoChangeDao {

private AutoChangeMapper autoChangeMapper;
	
	public AutoChangeDaoImpl(AutoChangeMapper autoChangeMapper) {
		this.autoChangeMapper = autoChangeMapper;
	}
	
	@Override
	  public List<Code> selectAutoChange(String uCodeVal1) {
	    return autoChangeMapper.selectAutoChange(uCodeVal1);
	  }
}
