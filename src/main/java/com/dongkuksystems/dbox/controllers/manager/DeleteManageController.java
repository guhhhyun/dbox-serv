package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManage;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManageLog;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.deletemanage.DeleteManageService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "휴지통 관리(삭제문서관리)-관리자 APIs")
public class DeleteManageController extends AbstractCommonController {

  private final DeleteManageService deleteManageService;

  public DeleteManageController(DeleteManageService deleteManageService) {
    this.deleteManageService = deleteManageService;
  }

  @GetMapping(value = "/delete-manage")
  @ApiOperation(value = "삭제 문서 조회")
  public ApiResult<List<DeleteManage>> selectDeleteDocument(@AuthenticationPrincipal JwtAuthentication authentication,
      DeleteManageDto dto, Model model) {
    List<DeleteManage> list = deleteManageService.selectDeleteDocument(dto);
    return OK(list);
  }

  @GetMapping(value = "/delete-manage/log")
  @ApiOperation(value = "삭제 문서 이력 조회")
  public ApiResult<List<DeleteManageLog>> selectDeleteDocumentLog(
      @AuthenticationPrincipal JwtAuthentication authentication, DeleteManageDto dto, Model model) {
    List<DeleteManageLog> list = deleteManageService.selectDeleteDocumentLog(dto);
    return OK(list);
  }

}