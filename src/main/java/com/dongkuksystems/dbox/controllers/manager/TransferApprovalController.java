package com.dongkuksystems.dbox.controllers.manager;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.transferapproval.TransferApprovalDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.transferapproval.TransferApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

@RestController
@RequestMapping("/api")
public class TransferApprovalController extends AbstractCommonController {

	@Autowired
	private TransferApprovalService transferApprovalService;

	@GetMapping("/transfer-approvals")
	public ApiResult<List<Map<String, Object>>> selectTransferApprovals(TransferApprovalDto transferApprovalDto) {
		List<Map<String, Object>> transferApprovals = transferApprovalService.selectTransferApprovals(transferApprovalDto);
		return OK(transferApprovals);
	}

	@GetMapping("/transfer-approvals/req-data/{uReqId}")
	public ApiResult<List<Map<String, Object>>> selectReqData(@PathVariable String uReqId) {
		List<Map<String, Object>> reqData = transferApprovalService.selectReqData(uReqId);
		return OK(reqData);
	}

	@GetMapping("/transfer-approvals/req-users")
	public ApiResult<List<Map<String, Object>>> selectReqUsers() {
		List<Map<String, Object>> reqUsers = transferApprovalService.selectReqUsers();
		return OK(reqUsers);
	}

	@PatchMapping("/transfer-approvals/{action}")
	public ApiResult<Boolean> patchTransferApproval(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String action,
			@RequestBody TransferApprovalDto transferApprovalDto) {
		UserSession userSession = getUserSession(authentication);
		transferApprovalService.patchTransferApproval(transferApprovalDto.userSession(userSession).action(action));
		return OK(null);
	}

	private UserSession getUserSession(JwtAuthentication authentication) {
		return (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
	}

}
