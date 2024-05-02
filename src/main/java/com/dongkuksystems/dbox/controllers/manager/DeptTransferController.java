package com.dongkuksystems.dbox.controllers.manager;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.depttransfer.DeptTransferDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.depttransfer.DeptTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

@RestController
@RequestMapping("/api")
public class DeptTransferController extends AbstractCommonController {

	@Autowired
	private DeptTransferService deptTransferService;

	@GetMapping("/dept-transfers")
	public ApiResult<List<Map<String, Object>>> selectDeptTransfers() {
		return OK(deptTransferService.selectDeptTransfers());
	}

	@GetMapping("/dept-transfers/folders/tree")
	public ApiResult<List<Map<String, Object>>> selectFoldersAsTree(DeptTransferDto deptTransferDto) {
		return OK(deptTransferService.selectFoldersAsTree(deptTransferDto));
	}

	@GetMapping("/dept-transfers/requests/{uDeptCode}")
	public ApiResult<List<Map<String, Object>>> selectDeptTransfersRequested(@PathVariable String uDeptCode) {
		return OK(deptTransferService.selectDeptTransfersRequested(uDeptCode));
	}

	@PostMapping("/dept-transfers/{rObjectId}")
	public ApiResult<Boolean> insertDeptTransfers(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId,
			@RequestBody DeptTransferDto deptTransferDto) {
		UserSession userSession = getUserSession(authentication);
		deptTransferService.insertDeptTransfers(deptTransferDto.rObjectId(rObjectId).userSession(userSession));
		return OK(null);
	}

	@PostMapping("/dept-transfers/data/aggregate")
	public ApiResult<Map<String, Object>> selectAggregateDataToTransfer(@RequestBody List<String> rObjectIds) {
		if(CollectionUtils.isEmpty(rObjectIds)) {
			// TODO Handle error.
		}
		return OK(deptTransferService.selectAggregateDataToTransfer(rObjectIds));
	}

	@DeleteMapping("/dept-transfers/requests")
	public ApiResult<Boolean> deleteDeptTransfersRequested(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@RequestBody List<String> rObjectIds) {
		if(CollectionUtils.isEmpty(rObjectIds)) {
			// TODO Handle error.
		}
		UserSession userSession = getUserSession(authentication);
		deptTransferService.deleteDeptTransfersRequested(userSession, rObjectIds);
		return OK(null);
	}

	private UserSession getUserSession(JwtAuthentication authentication) {
		return (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
	}

}
