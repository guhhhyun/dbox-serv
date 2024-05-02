package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.isdelete.IsDeleteService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "상위폴더 삭제여부")
public class IsDeleteController extends AbstractCommonController {
  private final IsDeleteService isDeleteService;
  
  public IsDeleteController(IsDeleteService isDeleteService) {
    this.isDeleteService = isDeleteService;
  }



  @GetMapping("/is-delete/{docId}")
  @ApiOperation(value = "문서의 상위폴더 삭제여부")
  public ApiResult<Boolean> isDelete(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String docId) throws Exception {

    Boolean rst = isDeleteService.isDelete(docId);
    return OK(rst);
  }
  
  @GetMapping("/is-delete/{folId}/folder")
  @ApiOperation(value = "폴더의 상위폴더 삭제여부")
  public ApiResult<Boolean> isDeleteFolder(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String folId) throws Exception {

    Boolean rst = isDeleteService.isDeleteFol(folId);
    return OK(rst);
  }
}
