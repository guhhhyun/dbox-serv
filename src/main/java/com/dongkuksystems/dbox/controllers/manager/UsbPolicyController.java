package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.UsbPolicy;
import com.dongkuksystems.dbox.models.type.manager.usbpolicy.UsbPolicyType;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.manager.usbpolicy.UsbPolicyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = " 외부저장매체 기본정책 설정 APIs")
public class UsbPolicyController extends AbstractCommonController {
  private final UsbPolicyService usbPolicyService;
  
  public UsbPolicyController(UsbPolicyService usbPolicyService) {
    this.usbPolicyService = usbPolicyService;    
  }

  @GetMapping("managepolicy/usbpolicy/getCompData/{uComCode}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 회사 조회")
  public ApiResult<List<UsbPolicyType>> selectUsbPolicyComp(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uComCode) {
    List<UsbPolicyType> list = usbPolicyService.selectUsbPolicyComp(uComCode);
    return OK(list);
  }

  @GetMapping("managepolicy/usbpolicy/getDeptData/{uComCode}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 부서 조회")
  public ApiResult<List<UsbPolicyType>> selectUsbPolicyDept(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uComCode) {
    List<UsbPolicyType> list = usbPolicyService.selectUsbPolicyDept(uComCode);
    return OK(list);
  }

  @GetMapping("managepolicy/usbpolicy/getUserData/{uComCode}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 유저 조회")
  public ApiResult<List<UsbPolicyType>> selectUsbPolicyUser(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uComCode) {
    List<UsbPolicyType> list = usbPolicyService.selectUsbPolicyUser(uComCode);
    return OK(list);
  }

  @PatchMapping("managepolicy/usbpolicy/compValue/{rObjectId}/{uCodeVal2}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 회사 정책 수정")
  public ApiResult<UsbPolicy> patchCompValue(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uCodeVal2) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = usbPolicyService.patchCompValue(rObjectId, uCodeVal2, userSession);
    return OK(null);
  }

  @PostMapping("managepolicy/usbpolicy/deptSave/{uDeptCode}/{uComCode}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 부서 추가")
  public ApiResult postDeptSava(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uDeptCode, @PathVariable String uComCode) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = usbPolicyService.postDeptSave(uDeptCode, uComCode, userSession);
    return OK(null);
  }

  @DeleteMapping("managepolicy/usbpolicy/deleteDept/{rObjectId}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 부서 삭제")
  public ApiResult deleteDept(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId)
      throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = usbPolicyService.deleteDept(rObjectId, userSession);
    return OK(null);
  }

  @PostMapping("/managepolicy/usbpolicy/userSave/{userId}/{uComCode}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 사용자 추가")
  public ApiResult postUserSava(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String userId,
      @PathVariable String uComCode) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = usbPolicyService.postUserSave(userId, uComCode, userSession);
    return OK(null);
  }

  @DeleteMapping("managepolicy/usbpolicy/deleteUser/{rObjectId}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 사용자 삭제")
  public ApiResult deleteUser(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId)
      throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = usbPolicyService.deleteUser(rObjectId, userSession);
    return OK(null);
  }

  @PatchMapping("managepolicy/usbpolicy/userValue/{rObjectId}/{uPolicy}/{uStartDate}/{uEndDate}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 사용자 정책 수정")
  public ApiResult<UsbPolicy> patchUserValue(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uPolicy, @PathVariable String uStartDate,
      @PathVariable String uEndDate) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = usbPolicyService.patchUserValue(rObjectId, uPolicy, uStartDate, uEndDate, userSession);
    return OK(null);
  }

  @PatchMapping("managepolicy/usbpolicy/deptValue/{rObjectId}/{uPolicy}/{uStartDate}/{uEndDate}")
  @ApiOperation(value = "외부저장매체 기본정책 설정 부서 정책 수정")
  public ApiResult<UsbPolicy> patchDeptValue(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uPolicy, @PathVariable String uStartDate,
      @PathVariable String uEndDate) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = usbPolicyService.patchDeptValue(rObjectId, uPolicy, uStartDate, uEndDate, userSession);
    return OK(null);
  }

}
