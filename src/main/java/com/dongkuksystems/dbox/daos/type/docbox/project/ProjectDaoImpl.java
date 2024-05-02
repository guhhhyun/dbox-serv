package com.dongkuksystems.dbox.daos.type.docbox.project;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectRepeatDto;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.ProjectRepeating;

@Primary
@Repository
public class ProjectDaoImpl implements ProjectDao {
	private ProjectMapper projectMapper;

	public ProjectDaoImpl(ProjectMapper projectMapper) {
		this.projectMapper = projectMapper;
	}

	@Override
	public Optional<Project> selectOne(String rObjectId) {
		return projectMapper.selectOne(rObjectId);
	}
	
	@Override
	public Optional<Project> selectDetailOne(String uPjtCode, String orgId, String userId) {
		return projectMapper.selectDetailOne(uPjtCode, orgId, userId);
	}
	
	@Override
	public List<ProjectRepeating> selectRepeatingDetailList(String rObjectId) {
		return projectMapper.selectRepeatingDetailList(rObjectId);
	}
	
	@Override
	public List<ProjectRepeating> selectRepeatingListByCode(String pjtCode) {
	  return projectMapper.selectRepeatingListByCode(pjtCode);
	}
	
  @Override
  public List<Project> selectList(ProjectFilterDto projectFilterDto, String orgId) {
    return projectMapper.selectList(projectFilterDto, orgId);
  }
	
  @Override
  public List<Project> selectDetailList(ProjectFilterDto projectFilterDto, String orgId, String userId) {
    return projectMapper.selectDetailList(projectFilterDto, orgId, userId);
  }
  
  @Override
  public int selectCount(ProjectFilterDto projectFilterDto, String orgId, String userId) {
    return projectMapper.selectCount(projectFilterDto, orgId, userId);
  }
  
  @Override
  public Optional<Project> selectOneByUPjtCode(String uPjtCode) {
    return projectMapper.selectOneByUPjtCode(uPjtCode);
  }
  

  @Override
  public Optional<Project> selectOneByCabinetCode(String cabinetCode) {
    return projectMapper.selectOneByCabinetCode(cabinetCode);
  }
  
  @Override
  public List<String> selectDeptCodeListByPjtCode(String pjtCode, String permitType) {
    return projectMapper.selectDeptCodeListByPjtCode(pjtCode, permitType);
  }
  
  @Override
  public boolean checkProjectChief(String pjtCode, String userId) {
    return projectMapper.checkProjectChief(pjtCode, userId);
  }
  
  @Override
  public List<ProjectRepeatDto> selectRepeatListByCode(String pjtCode) {
    return projectMapper.selectRepeatListByCode(pjtCode);
  }

  @Override
  /** NEXT 프로젝트 코드 조회 **/
  public String selectNextPjtNo() {
    return projectMapper.selectNextPjtNo();
  }
}
