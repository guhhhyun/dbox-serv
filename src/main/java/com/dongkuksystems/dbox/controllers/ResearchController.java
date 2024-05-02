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
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchCountDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchDetailDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchUpdateDto;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.research.ResearchService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "연구과제 APIs")
public class ResearchController extends AbstractCommonController {
	private final ResearchService researchService;

	public ResearchController(ResearchService researchService ) {
		this.researchService = researchService;
	}

	@GetMapping(value = "/researchs/{researchId}")
	@ApiOperation(value = "연구과제 조회")
	public ApiResult<ResearchDetailDto> getResearchOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "연구과제의  키값") @PathVariable String researchId,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);

    String userId = userSession.getUser().getUserId();
		String orgId = userSession.getUser().getOrgId();
    ResearchDetailDto result = researchService.selectResearch(researchId, orgId, userId);

    return OK(result);
	}

  @GetMapping("/researchs")
  @ApiOperation(value = "연구과제 리스트 조회")
  public ApiResult<List<Research>> getResearchList(@AuthenticationPrincipal JwtAuthentication authentication,
  		@ModelAttribute ResearchFilterDto researchFilterDto,
      @ApiParam(value = "하위 존재 여부", example = "false") @RequestParam(required = false) boolean checkHasChildren,
		  HttpServletRequest request) throws Exception {
    UserSession userSession = (UserSession)getRedisRepository().getObject(authentication.loginId, UserSession.class);

		String userId = userSession.getUser().getUserId();
		String orgId = userSession.getUser().getOrgId();
		if (StringUtils.isBlank(researchFilterDto.getRDeptCode())) researchFilterDto.setRDeptCode(orgId);

    List<Research> result = researchService.selectResearchList(researchFilterDto, orgId, userId);
		
    return OK(result);
  }

  @GetMapping("/researchs/count")
  @ApiOperation(value = "연구과제 리스트 개수 조회")
  public ApiResult<ResearchCountDto> getResearchCount(@AuthenticationPrincipal JwtAuthentication authentication,
  		@ModelAttribute ResearchFilterDto researchFilterDto,
		  HttpServletRequest request) throws Exception {
    UserSession userSession = (UserSession)getRedisRepository().getObject(authentication.loginId, UserSession.class);

    String userId = userSession.getUser().getUserId();
		String orgId = userSession.getUser().getOrgId();
		if (StringUtils.isBlank(researchFilterDto.getRDeptCode())) researchFilterDto.setRDeptCode(orgId);
        
		ResearchCountDto result = researchService.selectResearchCount(researchFilterDto, orgId, userId);
		
    return OK(result);
  }
	
	@PostMapping("/researchs")
	@ApiOperation(value = "연구과제 생성")
	public ApiResult<String> createProject(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "연구과제 정보") @RequestBody ResearchCreateDto researchInfo) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		
		String rst = researchService.createResearch(userSession, researchInfo, null);
		
		return OK(rst);
	}

	@PatchMapping("/researchs/{researchId}")
  @ApiOperation(value = "연구과제수정")
  public ApiResult<String> updateResearch(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "연구과제의  키값") @PathVariable String researchId,
      @ApiParam(value = "연구과제 정보") @RequestBody ResearchUpdateDto researchInfo) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    
    researchInfo.setURschCode(researchId);
    String rst = researchService.updateResearch(userSession, researchInfo, null);
    
    return OK(rst);
  }
  
  @PatchMapping("/researchs/{researchId}/fix")
  @ApiOperation(value = "연구과제 완료")
  public ApiResult<Boolean> researchComplete(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "연구과제의  키값") @PathVariable String researchId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    
    researchService.makeResearchFinished(userSession, researchId);
    
    return OK(true);
  }
}
