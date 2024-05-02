package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.config.support.SimpleOffsetPageRequest;
import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.daos.type.manager.manageid.ManageIdDao;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdCreateDto;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdDto;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdTreeDto;
import com.dongkuksystems.dbox.models.type.manager.manageid.ManageId;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.manageid.ManageIdService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "사용자 ID관리 APIs")
public class ManageIdController extends AbstractCommonController {
  private final ManageIdService manageIdService;
  private final ManageIdDao manageIdDao;

  public ManageIdController(ManageIdService manageIdService, ManageIdDao manageIdDao) {
    this.manageIdService = manageIdService;
    this.manageIdDao = manageIdDao;
  }

  @GetMapping("/manageId")
  @ApiOperation(value = "전체 사용자 id 조회")
  public ApiResult<List<ManageId>> selectUserId(@AuthenticationPrincipal JwtAuthentication authentication,
      ManageIdDto dto, Model model) {
    List<ManageId> list = manageIdService.selectUserId(dto);
    return OK(list);
  }

  @GetMapping("/manageIdLog/{uUserId}")
  @ApiOperation(value = "사용자 id접속현황 조회")
  public ApiResult<Map<String, Object>> selectUserIdLog(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uUserId, ManageIdDto dto, Model model) {
    Map<String, Object> result = new HashMap<String, Object>();
    int totalCnt = 0; 
    if(!("undefined").equals(uUserId)) {      
      totalCnt = manageIdDao.selectUserIdLogCount(uUserId);    
    } 
    SimpleOffsetPageRequest pageable = new SimpleOffsetPageRequest(Long.parseLong(dto.getOffset()),  Integer.parseInt(dto.getLimit()));
    List<ManageId> list = manageIdService.selectUserIdLog(uUserId, pageable.offset(), pageable.limit());            
    result.put("totalCnt", totalCnt);    
    result.put("list", list);
        
    return OK(result);
  }

  @PostMapping("/manageId/createUser")
  @ApiOperation(value = "edms_user 사용자 추가")
  public ApiResult<ManageId> createUserId(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody ManageIdCreateDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    manageIdService.createUserId(dto, userSession);
    return OK(null);
  }

  @PatchMapping("/manageId/updateStatus")
  @ApiOperation(value = "edms_user안에 사용자 사용여부 수정")
  public ApiResult<ManageId> updateIdStatus(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody ManageIdCreateDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    manageIdService.updateIdStatus(dto, userSession);
    return OK(null);
  }
  
  @PostMapping("/manageId/{userId}/userpreset/{comCode}/{orgId}")
  @ApiOperation(value = "edms_user안에 처음 들어갈 때 preset 추가")
  public ApiResult<String> createNewUserPreset(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String userId, @PathVariable String comCode, @PathVariable String orgId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = manageIdService.createNewUserPreset(userId, comCode, orgId, userSession);
    return OK(rst);
  }
  

  @GetMapping("/manageId/tree")
  @ApiOperation(value = "전체 사용자 tree 조회")
  public ApiResult<ManageIdTreeDto> selectManageIdTree(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestParam(value="comOrgId", defaultValue="DKG") String comOrgId ) throws Exception {
    ManageIdTreeDto dto = manageIdService.selectMangeIdTree(comOrgId);
    return OK(dto);
  }
  
}