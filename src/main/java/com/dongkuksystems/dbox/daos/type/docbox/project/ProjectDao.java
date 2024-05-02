package com.dongkuksystems.dbox.daos.type.docbox.project;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectRepeatDto;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.ProjectRepeating;

public interface ProjectDao {
  Optional<Project> selectOne(String rObjectId);
  Optional<Project> selectDetailOne(String uPjtCode, String orgId, String userId);
  Optional<Project> selectOneByUPjtCode(String uPjtCode);
  Optional<Project> selectOneByCabinetCode(String cabinetCode);
  List<ProjectRepeating> selectRepeatingDetailList(String rObjectId);
  List<ProjectRepeating> selectRepeatingListByCode(String pjtCode);
  List<Project> selectList(ProjectFilterDto projectFilterDto, String orgId);
  List<Project> selectDetailList(ProjectFilterDto projectFilterDto, String orgId, String userId);
  int selectCount(ProjectFilterDto projectFilterDto, String orgId, String userId);
  List<String> selectDeptCodeListByPjtCode(String pjtCode, String permitType);
  boolean checkProjectChief(String pjtCode, String userId);
  List<ProjectRepeatDto> selectRepeatListByCode(String pjtCode);
  String selectNextPjtNo();    /** NEXT 프로젝트 코드 조회 **/ 
}
