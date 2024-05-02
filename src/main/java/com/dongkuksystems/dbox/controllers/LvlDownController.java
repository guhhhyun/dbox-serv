package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.request.LvlDownDetail;
import com.dongkuksystems.dbox.models.dto.type.request.ReqRejectDto;
import com.dongkuksystems.dbox.models.type.request.ReqAuth;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.seclvl.LvlDownService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "제한문서 등급변경 APIs")
public class LvlDownController extends AbstractCommonController{
  private final LvlDownService lvlDownService;

  public LvlDownController(LvlDownService lvlDownService) {
    this.lvlDownService = lvlDownService;
  }
  
  @GetMapping("/lvl-requests")
  @ApiOperation(value = "제한문서 등급변경 조회")
  public ApiResult<List<LvlDownDetail>> reqAuthList(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
    
    List<LvlDownDetail> tree = lvlDownService.lvlDownList();
    return OK(tree);
  }
  
  @PostMapping("/lvl-requests/{lvlDownId}/approve")
  @ApiOperation(value = "제한문서 등급변경 승인")
  public ApiResult<String> approveTakeout(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String lvlDownId) throws Exception {

    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);

    String rst = lvlDownService.approveLvlDown(lvlDownId, userSession);
    return OK(null);
  }
  
  @PostMapping("/lvl-requests/{lvlDownId}/reject")
  @ApiOperation(value = "제한문서 등급변경 반려")
  public ApiResult<ReqAuth> rejectReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
       @PathVariable String lvlDownId, 
       String rejectReason)throws Exception {
    
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    
    String rst = lvlDownService.rejectLvlDown(lvlDownId, userSession, rejectReason);
    
    return OK(null);
  }
}
