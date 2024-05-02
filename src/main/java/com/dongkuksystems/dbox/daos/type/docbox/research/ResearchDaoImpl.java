package com.dongkuksystems.dbox.daos.type.docbox.research;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchRepeatDto;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.docbox.ResearchRepeating;

@Primary
@Repository
public class ResearchDaoImpl implements ResearchDao {
	private ResearchMapper researchMapper;

	public ResearchDaoImpl(ResearchMapper researchMapper) {
		this.researchMapper = researchMapper;
	}

	@Override
	public Optional<Research> selectOne(String rObjectId) {
		return researchMapper.selectOne(rObjectId);
	}
	
	@Override
	public Optional<Research> selectDetailOne(String uRschCode, String orgId, String userId) {
		return researchMapper.selectDetailOne(uRschCode, orgId, userId);
	}
	
	@Override
	public Optional<Research> selectOneByURschCode(String uRschCode) {
		return researchMapper.selectOneByURschCode(uRschCode);
	}
	
	@Override
	public List<ResearchRepeating> selectRepeatingDetailList(String rObjectId) {
		return researchMapper.selectRepeatingDetailList(rObjectId);
	}
	
	@Override
	public List<ResearchRepeating> selectRepeatingListByCode(String rschCode) {
	  return researchMapper.selectRepeatingListByCode(rschCode);
	}
	
  @Override
  public List<Research> selectList(ResearchFilterDto researchFilterDto, String orgId) {
    return researchMapper.selectList(researchFilterDto, orgId);
  }
  
  @Override
  public List<Research> selectDetailList(ResearchFilterDto researchFilterDto, String orgId, String userId) {
  	return researchMapper.selectDetailList(researchFilterDto, orgId, userId);
  }
  
  @Override
  public int selectCount(ResearchFilterDto researchFilterDto, String orgId, String userId) {
  	return researchMapper.selectCount(researchFilterDto, orgId, userId);
  }
  
  @Override
  public Optional<Research> selectOneByCabinetCode(String cabinetCode) {
    return researchMapper.selectOneByCabinetCode(cabinetCode);
  }
  
  @Override
  public List<String> selectDeptCodeListByRschCode(String rschCode, String permitType) {
  	return researchMapper.selectDeptCodeListByRschCode(rschCode, permitType);
  }
  
  @Override
  public boolean checkResearchChief(String rschCode, String userId) {
    return researchMapper.checkResearchChief(rschCode, userId);
  }

  @Override
  /** NEXT 연구과제 코드 조회 **/
  public String selectNextRschNo() {
    return researchMapper.selectNextRschNo();
  }

  @Override
  public List<ResearchRepeatDto> selectRepeatListByCode(String rschCode) {
    return researchMapper.selectRepeatListByCode(rschCode);
  }
}
