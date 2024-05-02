package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.dto.type.manager.history.HistoryDeleteFilterDto;
import com.dongkuksystems.dbox.models.type.history.HistoryAttach;
import com.dongkuksystems.dbox.models.type.history.HistoryDocDistribution;
import com.dongkuksystems.dbox.models.type.history.HistoryDocLifeCycle;
import com.dongkuksystems.dbox.models.type.history.HistoryUsb;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.history.HistoryService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "이력관리 APIs")
public class HistoryController extends AbstractCommonController {
	
	private final HistoryService historyService;

	public HistoryController(HistoryService historyService) {
		this.historyService = historyService;
	}
	
	@GetMapping("/history/historytotal")
	@ApiOperation(value = "현황 - 사용현황")
	public ApiResult<List<Map<String, Object>>> selectHistoryTotal(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<Map<String, Object>> tree = historyService.selectHistoryTotal(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/historytotalitem")
	@ApiOperation(value = "현황 - 항목별 사용 현황")
	public ApiResult<List<Map<String, Object>>> selectHistoryTotalItem(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<Map<String, Object>> tree = historyService.selectHistoryTotalItem(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/historytotalasset")
	@ApiOperation(value = "현황 - 자산화 현황")
	public ApiResult<List<Map<String, Object>>> selectHistoryTotalAsset(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<Map<String, Object>> tree = historyService.selectHistoryTotalAsset(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/deptlifecycle")
	@ApiOperation(value = "조직별 이력 - Life Cycle")
	public ApiResult<List<HistoryDocLifeCycle>> selectHistoryDeptLifeCycle(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<HistoryDocLifeCycle> tree = historyService.selectHistoryDeptLifeCycle(historyDeleteFilterDto);
		return OK(tree);
	}

	@GetMapping("/history/deptdistribution")
	@ApiOperation(value = "조직 이력 - 문서 유통")
	public ApiResult<List<HistoryDocDistribution>> selectHistoryDeptDistribution(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<HistoryDocDistribution> tree = historyService.selectHistoryDeptDistribution(historyDeleteFilterDto);
		return OK(tree);
	}

	@GetMapping("/history/doclifecycle")
	@ApiOperation(value = "문서별 이력 - Life Cycle")
	public ApiResult<List<ReqDelete>> selectHistoryDocumentLifeCycle(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<ReqDelete> tree = historyService.selectHistoryDocumentLifeCycle(historyDeleteFilterDto);
		return OK(tree);
	}

	@GetMapping("/history/docdistribution")
	@ApiOperation(value = "문서별 이력 - 문서 유통")
	public ApiResult<List<ReqDelete>> selectHistoryDocumentDistribution(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<ReqDelete> tree = historyService.selectHistoryDocumentDistribution(historyDeleteFilterDto);
		return OK(tree);
	}

	@GetMapping("/history/messengeruser")
	@ApiOperation(value = "외부 메신저 연동 이력(사용자 기준)")
	public ApiResult<List<ReqDelete>> selectHistoryMessengerUser(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<ReqDelete> tree = historyService.selectHistoryMessengerUser(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/externalattach")
	@ApiOperation(value = "외부 사이트 반출 이력 ( 메신저 이력 포함 )")
	public ApiResult<List<ReqDelete>> selectHistoryExternalAttach(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<ReqDelete> tree = historyService.selectHistoryExternalAttach(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/externalattachdetail")
	@ApiOperation(value = "외부 사이트 반출 이력 상세정보")
	public ApiResult<List<ReqDelete>> selectHistoryExternalAttachDetail(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<ReqDelete> tree = historyService.selectHistoryExternalAttachDetail(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/historylogdetail")
	@ApiOperation(value = "상세이력")
	public ApiResult<List<HistoryAttach>> selectHistoryLogDetail(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<HistoryAttach> tree = historyService.selectHistoryLogDetail(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/historyusb")
	@ApiOperation(value = "기타이력 - 외부저장매체 사용이력")
	public ApiResult<List<HistoryUsb>> selectHistoryUsb(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<HistoryUsb> tree = historyService.selectHistoryUsb(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/historyusbdetail")
	@ApiOperation(value = "기타이력 - 외부저장매체 사용이력")
	public ApiResult<List<HistoryUsb>> selectHistoryUsbDetail(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<HistoryUsb> tree = historyService.selectHistoryUsbDetail(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/historyodd")
	@ApiOperation(value = "기타이력 - 특이사용자 이력")
	public ApiResult<List<HistoryUsb>> selectHistoryOdd(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<HistoryUsb> tree = historyService.selectHistoryOdd(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@GetMapping("/history/historytrans")
	@ApiOperation(value = "기타이력 - 자료 이관")
	public ApiResult<List<HistoryUsb>> selectHistoryTrans(@AuthenticationPrincipal JwtAuthentication authentication,
			@ModelAttribute HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<HistoryUsb> tree = historyService.selectHistoryTrans(historyDeleteFilterDto);
		return OK(tree);
	}
	
	@PostMapping("/history/historydeletes")
	@ApiOperation(value = "자료 폐기 이력")
	public ApiResult<List<ReqDelete>> selectHistoryDelete(@AuthenticationPrincipal JwtAuthentication authentication,
			@RequestBody HistoryDeleteFilterDto historyDeleteFilterDto) {
		List<ReqDelete> tree = historyService.selectHistoryDelete(historyDeleteFilterDto);
		return OK(tree);
	}
	
}
