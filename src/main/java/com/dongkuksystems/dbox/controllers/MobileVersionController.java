package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.dto.mobile.MobileVersionDetail;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.mobile.MobileVersionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "모바일 버전 APIs")
public class MobileVersionController extends AbstractCommonController{
  private final MobileVersionService mobileVersionService;

  public MobileVersionController(MobileVersionService mobileVersionService) {
    this.mobileVersionService = mobileVersionService;
  }
  
  @GetMapping("/mobile-version")
  @ApiOperation(value = "모바일 버전조회")
  public ApiResult<MobileVersionDetail> mobileVersion(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
    
    MobileVersionDetail mobile = mobileVersionService.mobileVersion();
    return OK(mobile);
  }
}
