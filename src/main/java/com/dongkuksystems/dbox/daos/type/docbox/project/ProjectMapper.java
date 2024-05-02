package com.dongkuksystems.dbox.daos.type.docbox.project;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectRepeatDto;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.ProjectRepeating;

public interface ProjectMapper {
  public Optional<Project> selectOne(@Param("rObjectId") String rObjectId);
  public Optional<Project> selectDetailOne(@Param("uPjtCode") String uPjtCode, @Param("orgId") String orgId, @Param("userId") String userId);
  public List<ProjectRepeating> selectRepeatingList(@Param("project") ProjectFilterDto projectFilterDto);
  public List<ProjectRepeating> selectRepeatingListByCode(@Param("pjtCode") String pjtCode);
  public List<ProjectRepeating> selectRepeatingDetailList(@Param("rObjectId") String rObjectId);
  public Optional<Project> selectOneByUPjtCode(@Param("uPjtCode") String uPjtCode);
  public Optional<Project> selectOneByCabinetCode(@Param("cabinetCode") String cabinetCode);
  public List<Project> selectList(@Param("project") ProjectFilterDto projectFilterDto, @Param("orgId") String orgId);
  public List<Project> selectDetailList(@Param("project") ProjectFilterDto projectFilterDto, @Param("orgId") String orgId, @Param("userId") String userId);
  public int selectCount(@Param("project") ProjectFilterDto projectFilterDto, @Param("orgId") String orgId, @Param("userId") String userId);
  public List<String> selectDeptCodeListByPjtCode(@Param("pjtCode") String pjtCode, @Param("permitType") String permitType);
  public boolean checkProjectChief(@Param("pjtCode") String pjtCode, @Param("userId") String userId);
  public List<ProjectRepeatDto> selectRepeatListByCode(@Param("pjtCode") String pjtCode);
  public String selectNextPjtNo();    /** NEXT 프로젝트 코드 조회 **/
}
