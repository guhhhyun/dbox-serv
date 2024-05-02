package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqClosedDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.LockUserDto;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;
import com.dongkuksystems.dbox.models.type.user.UserLock;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.isdelete.IsDeleteService;
import com.dongkuksystems.dbox.services.reqdisposal.ReqDisposalService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Closed 폐기요청 APIs")
public class ReqDisposalController extends AbstractCommonController{
  private final ReqDisposalService reqDisposalService;
  private final IsDeleteService isDeleteService;
  
  public ReqDisposalController(ReqDisposalService reqDisposalService, IsDeleteService isDeleteService) {
    this.reqDisposalService = reqDisposalService;
    this.isDeleteService = isDeleteService;
  }
  
  @GetMapping("/closed-requests")
  @ApiOperation(value = "폐기요청 조회")
  public ApiResult<List<ReqClosedDetailDto>> reqAuthList(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
    
    List<ReqClosedDetailDto> tree = reqDisposalService.reqClosedList();
    return OK(tree);
  }

  @GetMapping("/closed-requests/{rObjectId}")
  @ApiOperation(value = "폐기요청 조회")
  public ApiResult<ReqClosedDetailDto> getClosedRequest(@PathVariable String rObjectId) throws Exception {
    return OK(reqDisposalService.getClosedRequest(rObjectId));
  }
  
  @PostMapping("/closed-requests/{closedRequestId}/approve")
  @ApiOperation(value = "폐기요청 승인")
  public ApiResult<String> approveReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
       @PathVariable String closedRequestId, HttpServletRequest request, @RequestBody(required = false) DeleteManageDto dto)throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    String ip = getClientIp(request);
    String rst = reqDisposalService.approveReqClosed(closedRequestId, userSession, ip, dto);
    
    return OK(null);
  }
  
  @PostMapping("/closed-requests/{closedRequestId}/reject")
  @ApiOperation(value = "폐기요청 반려")
  public ApiResult<String> rejectReqClosed(@AuthenticationPrincipal JwtAuthentication authentication,
       @PathVariable String closedRequestId, HttpServletRequest request, @RequestBody(required = false) DeleteManageDto dto)throws Exception {
    
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    String ip = getClientIp(request);
    String rst = reqDisposalService.rejectReqClosed(closedRequestId, userSession, ip, dto);
    
    return OK(null);
  }
  
  
  
  @GetMapping("/request-disposal/{deptCode}")
  @ApiOperation(value = "문서 폐기 리스트 조회")
  public ApiResult<List<ReqDisposalDetailDto>> reqDisposalList(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String deptCode, @ModelAttribute ReqDisposalFilterDto dto) throws Exception {
    List<ReqDisposalDetailDto> result = reqDisposalService.reqDisposalList(deptCode, dto);
    return OK(result);
  }
  
  
  @PostMapping("/request-disposal")
  @ApiOperation(value = "문서 폐기 요청")
  public ApiResult registUserLock(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ReqDelete dto, HttpServletRequest request) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    String ip = getClientIp(request);
    reqDisposalService.registReqDisposal(userSession, dto, ip);
    return OK(null);
  }
  
  @PatchMapping("/request-disposal/{rObjectId}/delete")
  @ApiOperation(value = "문서 폐기 요청 취소 다른 폴더로 이동 포함")
  public ApiResult<Boolean> deleteReqDisposal(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @RequestBody ReqDisposalDetailDto dto, HttpServletRequest request) throws Exception {
    
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String ip = getClientIp(request);
    Boolean rst = reqDisposalService.deleteReqDisposal(userSession, rObjectId, dto, ip);
    return OK(rst);
  }
   
  
  @PatchMapping("/request-disposal/{rObjectId}")
  @ApiOperation(value = "문서 폐기 연장")
  public ApiResult<UserLock> patchReqDisposal(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, HttpServletRequest request) throws Exception {
    String ip = getClientIp(request);
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    reqDisposalService.patchReqDisposal(userSession, rObjectId, ip);
    return OK(null);
  }
  
  
  
  
  

  
}
