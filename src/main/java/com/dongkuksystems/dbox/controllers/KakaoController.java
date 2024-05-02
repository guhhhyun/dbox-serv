package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;

import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.kakao.KakaoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "kakao APIs")
public class KakaoController extends AbstractCommonController {
  private final KakaoService kakaoService;
  
  public KakaoController(KakaoService kakaoService) {

    this.kakaoService = kakaoService;
  }


  @PostMapping("/kakao/test")
  @ApiOperation(value = "카카오 insert")
  public ApiResult<String> createReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
        String userId, String callphone, String templatecode, String msg) throws Exception {
  
    kakaoService.insertKakao(userId, callphone, templatecode, msg);
  
    return OK(null);
  }
}
