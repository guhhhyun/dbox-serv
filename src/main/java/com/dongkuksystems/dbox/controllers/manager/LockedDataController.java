package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.LockedDataDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.lockeddata.LockedDataService;

@RestController
@RequestMapping("/api")
public class LockedDataController extends AbstractCommonController {

	@Autowired
	private LockedDataService lockedDataService;

	@GetMapping("/locked-data")
	public ApiResult<List<Map<String, Object>>> selectLockedData(LockedDataDto lockedDataDto) {
		return OK(lockedDataService.selectDataLocked(lockedDataDto));
	}

	@PatchMapping("/locked-data/{rObjectId}/unlock")
	public ApiResult<String> unlockDataById(
			@PathVariable String rObjectId,
			@AuthenticationPrincipal JwtAuthentication authentication,
			HttpServletRequest request) throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
	      UserSession.class);
		
    String userType = "C"; // 사별관리자
    
    if(userSession.getUser().getMgr().getGroupComCode() != null) {
      userType = "G"; // 그룹관리자
    }
    
	  String ip = getClientIp(request);
		lockedDataService.unlockData(rObjectId, userSession, ip, userType);
		return OK(rObjectId);
	}



}
