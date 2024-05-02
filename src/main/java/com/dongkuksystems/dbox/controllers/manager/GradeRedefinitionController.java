package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.graderedefinition.GradeRedefinition;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.manager.graderedefinition.GradeRedefinitionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "보안 등급 APIs")
public class GradeRedefinitionController extends AbstractCommonController {
  private final GradeRedefinitionService gradeRedefinitionService;
  
  public GradeRedefinitionController(GradeRedefinitionService gradeRedefinitionService) {
    this.gradeRedefinitionService = gradeRedefinitionService;  
  }

  @GetMapping("/grade/redefinition")
  @ApiOperation(value = "보안 등급 명칭 조회")
  public ApiResult<List<GradeRedefinition>> selectGradeRedefinition(
      @AuthenticationPrincipal JwtAuthentication authentication) {
    List<GradeRedefinition> tree = gradeRedefinitionService.selectGradeRedefinition();
    return OK(tree);
  }

  @PatchMapping("/grade/{rObjectId}/redefinition/{uCodeName1}")
  @ApiOperation(value = "보안 등급 명칭 수정")
  public ApiResult<GradeRedefinition> patchGradeRedefinition(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uCodeName1) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = gradeRedefinitionService.patchGradeRedefinition(rObjectId, uCodeName1, userSession);
        
    return OK(null);
  }
}