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
import com.dongkuksystems.dbox.models.type.manager.gradePreservation.GradePreservation;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.gradePreservation.GradePreservationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "보안등급 별 보존연한 관리 APIs")
public class GradePreservationController extends AbstractCommonController {
  private final GradePreservationService gradePreservationService;

  public GradePreservationController(GradePreservationService gradePreservationService) {
    this.gradePreservationService = gradePreservationService;
  }

  @GetMapping("/grade/preservation/{uComCode}")
  @ApiOperation(value = "보안등급 별 보존연한 조회")
  public ApiResult<List<GradePreservation>> selectGradePreservation(
      @AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String uComCode) {
    List<GradePreservation> list = gradePreservationService.selectGradePreservation(uComCode);
    return OK(list);
  }

  @PatchMapping("/grade/preservation/{rObjectId}/params/{uLimitValue}/{uTeamValue}/{uCompValue}/{uGroupValue}/{uPjtEverFlag}")
  @ApiOperation(value = "보안등급 별 보존연한 수정")
  public ApiResult<GradePreservation> patchGradePreservation(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uLimitValue, @PathVariable String uTeamValue,
      @PathVariable String uCompValue, @PathVariable String uGroupValue, @PathVariable String uPjtEverFlag)
      throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = gradePreservationService.patchGradePreservation(rObjectId.trim(), uLimitValue.trim(),
        uTeamValue.trim(), uCompValue.trim(), uGroupValue.trim(), uPjtEverFlag.trim(), userSession);
    return OK(null);
  }

  @PatchMapping("/grade/preservation/{rObjectId}/autoExtend/{uAutoExtendValue}")
  @ApiOperation(value = "보존연한 자동 연장 기간 수정")
  public ApiResult<GradePreservation> patchAutoExtend(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uAutoExtendValue) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = gradePreservationService.patchGradeAutoExtend(rObjectId.trim(), uAutoExtendValue.trim(), userSession);
    return OK(null);
  }

  @PatchMapping("/grade/preservation/{rObjectId}/saveDept/{uDeptCodeValue}")
  @ApiOperation(value = "보존연한 자동 연장 미적용 부서 등록")
  public ApiResult<GradePreservation> patchSaveDept(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uDeptCodeValue) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = gradePreservationService.patchGradeSaveDept(rObjectId.trim(), uDeptCodeValue.trim(), userSession);
    return OK(null);
  }

}
