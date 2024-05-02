package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dongkuksystems.dbox.models.dto.table.gwdept.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.dongkuksystems.dbox.constants.EntCode;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.securities.AuthenticationRequest;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
@Api(tags = "그룹웨어 부서 APIs")
public class GwDeptController extends AbstractCommonController {
  private final GwDeptService gwDeptService;
  private final CodeService codeService;

  public GwDeptController(GwDeptService gwDeptService, CodeService codeService) {
    this.gwDeptService = gwDeptService;
    this.codeService = codeService;
  }

  @GetMapping("/depts/{deptId}")
  @ApiOperation(value = "부서 조회")
  public ApiResult<VDept> selectDept(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String deptId) throws Exception {
    logger.info("loginId: " + authentication.loginId.toString());
    VDept rst = gwDeptService.selectDeptByOrgId(deptId);
    return OK(rst);
  }

  @GetMapping("/depts")
  @ApiOperation(value = "전체 부서 조회")
  public ApiResult<List<VDept>> selectDepts(@AuthenticationPrincipal JwtAuthentication authentication,
      HttpServletRequest request,
      @RequestParam(value="isAuthenticated", defaultValue="false") boolean isAuthenticated) throws Exception{
    logger.info("loginId: " + authentication.loginId.toString());
    UserSession userSession = getUserSession(authentication);
    boolean isMobile = chkIsMobile(request);
    String deptId = EntCode.DKG.name();
    if (isAuthenticated) {
      deptId = userSession.getUser().getComOrgId();
      Map<String, String> vips = codeService.getAllAcessUserMap();
      for (String vipId : vips.keySet()) {
        if (vipId.equals(authentication.loginId)) {
          deptId = EntCode.DKG.name();
          break;
        }
      } 
    }
    
    List<VDept> tree = gwDeptService.selectDepts(deptId, isMobile?"Y":"N");
  
    return OK(tree);
  }

  @GetMapping("/depts/{deptId}/children")
  @ApiOperation(value = "부서 하위 조회(하위부서, 소속인원)")
  public ApiResult<GwDeptChildrenDto> selectDeptChildren(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String deptId, boolean userYn, boolean addJobYn) throws Exception {
    logger.info("loginId: " + authentication.loginId.toString());
    return OK(gwDeptService.selectDeptChildren(deptId, userYn, addJobYn));
  }
  
  @GetMapping("/depts/authenticated-root")
  @ApiOperation(value = "부서 하위 조회(하위부서, 소속인원)")
  public ApiResult<GwDeptChildrenDto> selectDeptRoot(@AuthenticationPrincipal JwtAuthentication authentication,
      boolean userYn, boolean addJobYn) throws Exception {
    logger.info("loginId: " + authentication.loginId.toString());
    UserSession userSession = getUserSession(authentication);
    String deptId = userSession.getUser().getComOrgId();
    Map<String, String> vips = codeService.getAllAcessUserMap();
    for (String vipId : vips.keySet()) {
      if (vipId.equals(authentication.loginId)) {
        deptId = EntCode.DKG.name();
        break;
      }
    } 
    return OK(gwDeptService.selectDeptChildren(deptId, userYn, addJobYn));
  }
//  getAllAcessUserMap

  // TODO: /depts/{orgId}/path
  @GetMapping("/depts/{orgId}/path")
  @ApiOperation(value = "부서 경로조회")
  public ApiResult<GwDeptPathDto> selectDeptPath(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String orgId) throws Exception {
    logger.info("loginId: " + authentication.loginId.toString());
    return OK(gwDeptService.selectDeptPath(orgId));
  }

  @PatchMapping("/depts/{orgId}")
  @ApiOperation(value = "부서수정")
  public ApiResult<VDept> patchDept(@RequestBody AuthenticationRequest authRequest) {
    return OK(null);
  }
  
  @GetMapping("/depts/tree")
  @ApiOperation(value = "전체 부서 조회(트리)")
  public ApiResult<GwDeptTreeDto> selectTree(@AuthenticationPrincipal JwtAuthentication authentication, 
		  @ModelAttribute GwDeptFilterDto dto) throws Exception {
    GwDeptTreeDto tree = gwDeptService.selectDeptTree(dto);
    return OK(tree);
  }

  @GetMapping("/depts/{deptId}/children/all")
  @ApiOperation(value = "하위 모든 부서 조회(1레벨로 리턴)")
  public ApiResult<List<VDept>> selectChildrenAll(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String deptId) throws Exception {
    List<VDept> rst = gwDeptService.selectDeptChildrenByOrgId(deptId);
    return OK(rst);
  }

  @GetMapping("/depts/{deptId}/admin-users")
  @ApiOperation(value = "부서 문서관리자 리스트 조회")
  public ApiResult<List<GwDeptListManagerDto>> selectDeptDocManagerList(@PathVariable String deptId) {
    List<GwDeptListManagerDto> managerList = gwDeptService.selectDeptMemberList(deptId);
    return OK(managerList);
  }

  @PostMapping("/depts/{deptId}/admin-users")
  @ApiOperation(value = "부서 문서관리자 일괄저장")
  public ApiResult<List<DeptMgrs>> postDeptDocManagerList(
          @AuthenticationPrincipal JwtAuthentication authentication,
          @PathVariable String deptId,
          @RequestBody GwDeptListManagerDtoWrapper payload) throws Exception {
    UserSession userSession = getUserSession(authentication);
    gwDeptService.postDeptManager(deptId, userSession, payload.getMembers());
    return OK(null);
  }
  
  @GetMapping("/depts/manager/{managerPerId}")
  @ApiOperation(value = "부서장 조회")
  public ApiResult<List<GwDept>> selectDeptMng(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String managerPerId) throws Exception {
    List<GwDept> rst = gwDeptService.selectDeptMng(managerPerId);
    return OK(rst);
  }

  private UserSession getUserSession(JwtAuthentication authentication) {
    return (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
  }

}
