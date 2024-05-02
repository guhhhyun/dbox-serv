package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutApproveDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutConfigDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutCreateDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutRejectDto;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfig;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.takeout.TakeoutReqService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "외부반출 APIs")
public class TakeoutReqController extends AbstractCommonController {
	private final TakeoutReqService takeoutReqService;

	public TakeoutReqController(TakeoutReqService takeoutReqService) {
		this.takeoutReqService = takeoutReqService;
	}

	@GetMapping("/takeout-requests")
	@ApiOperation(value = "외부반출 요청 조회")
	public ApiResult<List<ReqTakeoutDetailDto>> takeoutDetailList(
			@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "본인리스트만 조회") @RequestParam(required = false, defaultValue = "true") boolean mine,
      @ApiParam(value = "본인리스트만 조회") @RequestParam(required = false) String reqStatus) throws Exception {
    List<ReqTakeoutDetailDto> result;
//    LocalDate today = LocalDate.now();
    ReqTakeout takeout = ReqTakeout.builder()
        .uReqUser(mine?authentication.loginId:null)
//        .uReqStatus(reqStatus==null?"A":reqStatus)//승인
//        .uLimitDateStr(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .build();
	  result = takeoutReqService.takeoutDetailList(takeout);
		return OK(result);
	}

  @GetMapping("/takeout-requests/{takeoutRequestId}/doc")
  @ApiOperation(value = "외부반출 요청 조회(reqDocId)")
  public ApiResult<List<ReqTakeoutDetailDto>> takeoutDetailListByObjId(
      @AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String takeoutRequestId)
      throws Exception {

    List<ReqTakeoutDetailDto> result = takeoutReqService.takeoutDetailListByObjId(takeoutRequestId);
    return OK(result);
  }

	@PostMapping("/takeout-requests")
	@ApiOperation(value = "외부반출 요청 생성")
	public ApiResult<String> createTakeout(@AuthenticationPrincipal JwtAuthentication authentication,
	    @RequestBody ReqTakeoutCreateDto takeoutCreateDto, @RequestParam(required = false) String docIds, HttpServletRequest request) throws Exception {
	  takeoutCreateDto.validation();
	  List<String> docIdList = new ObjectMapper().readValue(docIds,
        new TypeReference<List<String>>() {
        });
	  String ip = getClientIp(request);
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = takeoutReqService.createReqTakeout(userSession, takeoutCreateDto, docIdList, ip);
		return OK(rst);
		
	}

	@PostMapping("/takeout-requests/{takeoutRequestId}/approve")
	@ApiOperation(value = "외부반출 요청 승인")
	public ApiResult<ReqTakeout> approveTakeout(@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String takeoutRequestId, HttpServletRequest request) throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = getClientIp(request);
		String rst = takeoutReqService.approveReqTakeout(takeoutRequestId, userSession, ip);
		return OK(null);
	}

	@PostMapping("/takeout-requests/approve")
	@ApiOperation(value = "외부반출 요청 일괄승인")
	public ApiResult<Map<String, Integer>> approveAllTakeout(@AuthenticationPrincipal JwtAuthentication authentication,
	    @RequestParam(required = false) String takeoutRequestIds, HttpServletRequest request) throws Exception {
		List<String> takeoutRequestIdList = new ObjectMapper().readValue(takeoutRequestIds,
				new TypeReference<List<String>>() {
				});
		String ip = getClientIp(request);
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		Map<String, Integer> result = takeoutReqService.approveAllReqTakeout(userSession, takeoutRequestIdList, ip);
		return OK(result);
	}

	@PostMapping("/takeout-requests/{takeoutRequestId}/reject")
	@ApiOperation(value = "외부반출 요청 반려")
	public ApiResult<ReqTakeout> rejectTakeout(@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String takeoutRequestId, @RequestBody ReqTakeoutDto dto)
			throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = takeoutReqService.rejectReqTakeout(takeoutRequestId, userSession, dto.getURejectReason());
		return OK(null);
	}

	@PostMapping("/takeout-requests/reject")
	@ApiOperation(value = "외부반출 요청 일괄반려")
	public ApiResult<Map<String, Integer>> rejectAllTakeout(@AuthenticationPrincipal JwtAuthentication authentication,
	    @RequestParam(required = false) String takeoutRequestIds, @RequestParam String rejectReason)
			throws Exception {

		List<String> takeoutRequestIdList = new ObjectMapper().readValue(takeoutRequestIds,
				new TypeReference<List<String>>() {
				});
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);

		Map<String, Integer> result = takeoutReqService.rejectAllReqTakeout(userSession, rejectReason,
				takeoutRequestIdList);
		return OK(result);
	}

	
  
  @GetMapping("/takeout-requests/{orgId}/configs/{mode}")
  @ApiOperation(value = "부서 반출 설정 (프리패스/자동승인) 항목 조회")
  public ApiResult<TakeoutConfig> selectTakeoutConfigNamesList(
      @AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String orgId, @PathVariable String mode )
      throws Exception {

//    List<ReqTakeoutConfigDto> result = takeoutReqService.nameListByDeptCode(orgId);
    TakeoutConfig test = takeoutReqService.seletOneByDeptCode(orgId, mode);
    return OK(test);
  }
  
  @PatchMapping("/takeout-configs/{rObjectId}")
  @ApiOperation(value = "복호화 반출 승인 설정 수정")
  public ApiResult<TakeoutConfig> patchTakeoutConfig(@AuthenticationPrincipal JwtAuthentication authentication,
       @PathVariable String rObjectId, @RequestBody ReqTakeoutConfigDto dto) throws Exception {
    
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    takeoutReqService.patchTakeoutConfig(userSession, dto);
    return OK(null);
  }
  
  @PatchMapping("/takeout-configs/{rObjectId}/repeating")
  @ApiOperation(value = "복호화 반출 승인 목록 삭제")
  public ApiResult<TakeoutConfig> deleteTakeoutConfig(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @RequestBody ReqTakeoutConfigDto dto) throws Exception {
   
   UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
   takeoutReqService.deleteTakeoutConfig(userSession, dto);
   return OK(null);
  }
  
  @GetMapping("/takeout-requests/depts/{deptCode}")
  @ApiOperation(value = "외부반출 요청 조회(deptCode)")
  public ApiResult<List<ReqTakeout>> takeoutListByDeptCode(
      @AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String deptCode, @ModelAttribute ReqTakeoutDto dto)
      throws Exception {

    List<ReqTakeout> result = takeoutReqService.takeoutListByDeptCode(deptCode, dto);
    return OK(result);
  }
  
  @GetMapping("/takeout-requests/{reqId}/detail")
  @ApiOperation(value = "외부반출 요청 조회 detail")
  public ApiResult<List<ReqTakeoutDto>> takeoutListByReqId(@PathVariable String reqId) {
    List<ReqTakeoutDto> result = takeoutReqService.takeoutListByReqId(reqId);
    return OK(result);
  }
  
   
}
