package com.dongkuksystems.dbox.daos.type.agree;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.agree.AgreeFilter;
import com.dongkuksystems.dbox.models.type.agree.Agree;

@Primary
@Repository
public class AgreeDaoImpl implements AgreeDao{
	private AgreeMapper agreeMapper;
	
	public AgreeDaoImpl(AgreeMapper agreeMapper) {
		this.agreeMapper = agreeMapper;
	}


	@Override
	public Optional<Agree> agreementByUserId(String uUserId) {
		return agreeMapper.agreementByUserId(uUserId);
	}


  @Override
  public List<Agree> selectListByUserId(String uUserId) {
    return agreeMapper.selectListByUserId(uUserId);
  }


  @Override
  public List<Agree> selectListByOrgId(String orgId) {
    return agreeMapper.selectListByOrgId(orgId);
  }


  @Override
  public List<Agree> selectList(AgreeFilter filter) {
    return agreeMapper.selectList(filter);
  }


  @Override
  public List<Agree> selectListUserId(String userId) {
    return agreeMapper.selectListUserId(userId);
  }
	
}
