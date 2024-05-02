package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.AutoClosingDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.autoclosing.AutoClosingService;

@RestController
@RequestMapping("/api")
public class AutoClosingController extends AbstractCommonController {  
  
	@Autowired
	private AutoClosingService autoClosingService;	
	
	@GetMapping("/auto-closing/periods/{comCode}")
	public ApiResult<Map<String, Object>> selectPeriodToCloseByComCode(@PathVariable String comCode) {
		return OK(autoClosingService.selectPeriodToCloseByComCode(comCode));
	}

	@GetMapping("/auto-closing/data")
	public ApiResult<List<Map<String, Object>>> selectDataToClose(AutoClosingDto autoClosingDto) {
		List<Map<String, Object>> dataToClose = autoClosingService.selectDataToClose(autoClosingDto);
		return OK(dataToClose);
	}

	@GetMapping("/auto-closing/data/{docKey}")
	public ApiResult<List<Map<String, Object>>> selectDataByDocKeyToClose(@PathVariable String docKey) {
		List<Map<String, Object>> dataByIdToClose = autoClosingService.selectDataByDocKeyToClose(docKey);
		return OK(dataByIdToClose);
	}

	@PatchMapping("/auto-closing/periods/{rObjectId}")
	public ApiResult<Boolean> patchPeriodToCloseByComCode(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId, @RequestBody AutoClosingDto autoClosingDto) throws Exception {
		UserSession userSession = getUserSession(authentication);
		autoClosingService.patchPeriodToCloseByComCode(userSession, autoClosingDto.rObjectId(rObjectId));    
		return OK(null);
	}

	@DeleteMapping("/auto-closing/data/{objectId}")
	public void deleteObject(@PathVariable String objectId) {
		autoClosingService.deleteObject(objectId);
	}

	private UserSession getUserSession(JwtAuthentication authentication) {
		return (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
	}

}
