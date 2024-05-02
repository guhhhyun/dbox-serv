package com.dongkuksystems.dbox.daos.type.docbox.research;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchRepeatDto;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.docbox.ResearchRepeating; 

public interface ResearchMapper {
  public Optional<Research> selectOne(@Param("rObjectId") String rObjectId);
  public Optional<Research> selectDetailOne(@Param("uRschCode") String uRschCode, @Param("orgId") String orgId, @Param("userId") String userId);
  public Optional<Research> selectOneByURschCode(@Param("uRschCode") String uRschCode);
  public Optional<Research> selectOneByCabinetCode(@Param("cabinetCode") String cabinetCode);
  public List<ResearchRepeating> selectRepeatingDetailList(@Param("rObjectId") String rObjectId);
  public List<ResearchRepeating> selectRepeatingListByCode(@Param("rschCode") String rschCode);
  public List<Research> selectList(@Param("research") ResearchFilterDto researchFilterDto, @Param("orgId") String orgId);
  public List<Research> selectDetailList(@Param("research") ResearchFilterDto researchFilterDto, @Param("orgId") String orgId, @Param("userId") String userId);
  public int selectCount(@Param("research") ResearchFilterDto researchFilterDto, @Param("orgId") String orgId, @Param("userId") String userId);
  public List<String> selectDeptCodeListByRschCode(@Param("rschCode") String rschCode, @Param("permitType") String permitType);
  boolean checkResearchChief(@Param("rschCode") String rschCode, @Param("userId") String userId);
  public List<ResearchRepeatDto> selectRepeatListByCode(@Param("rschCode") String rschCode);
  public String selectNextRschNo();   /** NEXT 연구과제 코드 조회 **/
}
