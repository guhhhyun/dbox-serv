package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCountDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectDetailDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectUpdateDto;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.project.ProjectService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "프로젝트 APIs")
public class ProjectController extends AbstractCommonController {
	private final ProjectService projService;

	public ProjectController(ProjectService projService ) {
		this.projService = projService;
	}

	@GetMapping(value = "/projects/{projectId}")
	@ApiOperation(value = "프로젝트 조회")
	public ApiResult<ProjectDetailDto> getProjectOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "프로젝트의  키값") @PathVariable String projectId,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);

    String userId = userSession.getUser().getUserId();
		String orgId = userSession.getUser().getOrgId();
    ProjectDetailDto result = projService.selectProject(projectId, orgId, userId);

    return OK(result);
	}

  @GetMapping("/projects")
  @ApiOperation(value = "프로젝트 리스트 조회")
  public ApiResult<List<Project>> getProjectList(@AuthenticationPrincipal JwtAuthentication authentication,
  		@ModelAttribute ProjectFilterDto projectFilterDto,
      @ApiParam(value = "하위 존재 여부", example = "false") @RequestParam(required = false) boolean checkHasChildren,
		  HttpServletRequest request) throws Exception {
    UserSession userSession = (UserSession)getRedisRepository().getObject(authentication.loginId, UserSession.class);

		String userId = userSession.getUser().getUserId();
		String orgId = userSession.getUser().getOrgId();
		if (StringUtils.isBlank(projectFilterDto.getRDeptCode())) projectFilterDto.setRDeptCode(orgId);

    List<Project> result = projService.selectProjectList(projectFilterDto, orgId, userId);
		
    return OK(result);
  }

  @GetMapping("/projects/count")
  @ApiOperation(value = "프로젝트 리스트 개수 조회")
  public ApiResult<ProjectCountDto> getProjectCount(@AuthenticationPrincipal JwtAuthentication authentication,
  		@ModelAttribute ProjectFilterDto projectFilterDto,
		  HttpServletRequest request) throws Exception {
    UserSession userSession = (UserSession)getRedisRepository().getObject(authentication.loginId, UserSession.class);

    String userId = userSession.getUser().getUserId();
    String orgId = userSession.getUser().getOrgId();
		if (StringUtils.isBlank(projectFilterDto.getRDeptCode())) projectFilterDto.setRDeptCode(orgId);
        
		ProjectCountDto result = projService.selectProjectCount(projectFilterDto, orgId, userId);
		
    return OK(result);
  }
	
	@PostMapping("/projects")
	@ApiOperation(value = "프로젝트생성")
	public ApiResult<String> createProject(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "프로젝트 정보") @RequestBody ProjectCreateDto projectInfo) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		
		String rst = projService.createProject(userSession, projectInfo, null);
		
		return OK(rst);
	}

	@PatchMapping("/projects/{projectId}")
  @ApiOperation(value = "프로젝트수정")
  public ApiResult<String> updateProject(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "프로젝트의  키값") @PathVariable String projectId,
      @ApiParam(value = "프로젝트 정보") @RequestBody ProjectUpdateDto projectInfo) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    
    projectInfo.setUPjtCode(projectId);
    String rst = projService.updateProject(userSession, projectInfo, null);
    
    return OK(rst);
  }
  
  @PatchMapping("/projects/{projectId}/fix")
  @ApiOperation(value = "프로젝트 완료")
  public ApiResult<Boolean> projectComplete(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "프로젝트의  키값") @PathVariable String projectId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    
    projService.makeProjectFinished(userSession, projectId);
    
    return OK(true);
  }
}
