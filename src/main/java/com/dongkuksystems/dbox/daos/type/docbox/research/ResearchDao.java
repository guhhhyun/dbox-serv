package com.dongkuksystems.dbox.daos.type.docbox.research;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchRepeatDto;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.docbox.ResearchRepeating;

public interface ResearchDao {
  public Optional<Research> selectOne(String rObjectId);
  public Optional<Research> selectDetailOne(String uRschCode, String orgId, String userId);
  public Optional<Research> selectOneByURschCode(String uRschCode);
  public Optional<Research> selectOneByCabinetCode(String cabinetCode);
  public List<ResearchRepeating> selectRepeatingDetailList(String rObjectId);
  public List<ResearchRepeating> selectRepeatingListByCode(String rschCode);
  public List<Research> selectList(ResearchFilterDto researchFilterDto, String orgId);
  public List<Research> selectDetailList(ResearchFilterDto researchFilterDto, String orgId, String userId);
  public int selectCount(ResearchFilterDto researchFilterDto, String orgId, String userId);
  public List<String> selectDeptCodeListByRschCode(String rschCode, String permitType);
  boolean checkResearchChief(String rschCode, String userId);
  public List<ResearchRepeatDto> selectRepeatListByCode( String rschCode);
  public String selectNextRschNo();   /** NEXT 연구과제 코드 조회 **/
}
