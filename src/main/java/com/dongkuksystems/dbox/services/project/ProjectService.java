package com.dongkuksystems.dbox.services.project;

import java.util.List;
import java.util.Optional;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCountDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectDetailDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectRepeatDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectUpdateDto;
import com.dongkuksystems.dbox.models.type.docbox.Project;

public interface ProjectService {
	ProjectDetailDto selectProject(String projectId, String orgId, String userId) throws Exception;
	Optional<Project> selectProjectByUPjtCode(String uPjtCode) throws Exception;
	
	/**
	 * 프로젝트 리스트 조회
	 *  
	 * @param projectFilterDto 검색조건
	 * @param orgId 사용자 부서코드
	 * @param userId 사용자 아이디 (하위 폴더/문서 여부 파악용, null일 경우 조회 안함)
	 * @return 프로젝트 리스트
	 */
	List<Project> selectProjectList(ProjectFilterDto projectFilterDto, String orgId, String userId) throws Exception;
	
	/**
	 * 프로젝트 개수 조회
	 * 
	 * @param projectFilterDto 검색조건
	 * @param orgId 사용자 부서코드
	 * @return 프로젝트 개수 (주관 진행, 주관 완료, 참여 진행, 잠여 완료)
	 */
	ProjectCountDto selectProjectCount(ProjectFilterDto projectFilterDto, String orgId, String userId) throws Exception;
	
	void makeProjectFinished(UserSession userSession, String projectCode) throws Exception;
	public String saveProject(UserSession userSession,IDfSession idfSess, ProjectCreateDto dto) throws Exception;
	public String deleteProject(String rObjectId, UserSession userSession,IDfSession idfSess) throws Exception ;
	String createProject(UserSession userSession, ProjectCreateDto dto, IDfSession session) throws Exception;
	String updateProject(UserSession userSession, ProjectUpdateDto dto, IDfSession session) throws Exception;
	public List<ProjectRepeatDto> selectRepeatListByCode(String pjtCode) throws Exception;
}
