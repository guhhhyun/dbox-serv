package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.PatchDeleteScheduleDto;
import com.dongkuksystems.dbox.models.dto.type.manager.storageperiod.StoragePeriodLogListDto;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriod;
import com.dongkuksystems.dbox.models.type.manager.storageperiod.StoragePeriodLogList;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.storageperiod.StoragePeriodService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "삭제 완료 및 폐기 승인 문서 시스템 보관 기간 설정")
public class StoragePeriodController extends AbstractCommonController {
  private final StoragePeriodService storagePeriodService;

  public StoragePeriodController(StoragePeriodService storagePeriodService) {
    this.storagePeriodService = storagePeriodService;
  }

  @GetMapping("/storagePeriod/{uCodeVal1}")
  @ApiOperation(value = "사별 기간 조회")
  public ApiResult<List<StoragePeriod>> selectStoragePeriod(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uCodeVal1) {
    List<StoragePeriod> list = storagePeriodService.selectStoragePeriod(uCodeVal1);
    return OK(list);
  }

  @GetMapping("/storagePeriod/{uCodeVal1}/schedule")
  @ApiOperation(value = "사별 삭제 스케쥴 조회")
  public ApiResult<List<StoragePeriod>> selectDeleteSchedule(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uCodeVal1) {
    List<StoragePeriod> list = storagePeriodService.selectDeleteSchedule(uCodeVal1);
    return OK(list);
  }

  @PatchMapping("/storagePeriod/{rObjectId}/patch/{uCodeVal3}")
  @ApiOperation(value = "사별 보관 기간 수정")
  public ApiResult<Code> patchStoragePeriod(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uCodeVal3) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = storagePeriodService.patchStoragePeriod(rObjectId, uCodeVal3, userSession);
    return OK(null);
  }

  @PatchMapping("/storagePeriod/schedule/patch")
  @ApiOperation(value = "사별 삭제 스케쥴 수정")
  public void patchDeleteSchedule(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody PatchDeleteScheduleDto patchDeleteScheduleDto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    storagePeriodService.patchDeleteSchedule(userSession, patchDeleteScheduleDto);
  }

  @GetMapping("/storagePeriod/recycleLog")
  @ApiOperation(value = "사별 휴지통 이력 조회")
  public ApiResult<List<StoragePeriodLogList>> selectRecycleLog(
      @AuthenticationPrincipal JwtAuthentication authentication, StoragePeriodLogListDto storagePeriodLogListDto,
      Model model) {
    List<StoragePeriodLogList> list = storagePeriodService.selectRecycleLog(storagePeriodLogListDto);
    return OK(list);
  }

  @GetMapping("/storagePeriod/deleteLog")
  @ApiOperation(value = "사별 폐기 이력 조회")
  public ApiResult<List<StoragePeriodLogList>> selectDeleteLog(
      @AuthenticationPrincipal JwtAuthentication authentication, StoragePeriodLogListDto storagePeriodLogListDto,
      Model model) {
    List<StoragePeriodLogList> list = storagePeriodService.selectDeleteLog(storagePeriodLogListDto);
    return OK(list);
  }

}
