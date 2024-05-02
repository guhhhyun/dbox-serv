package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.deptinformconfig.DeptInformConfigDto;
import com.dongkuksystems.dbox.models.type.manager.Mgr;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.managerconfig.ManagerConfigService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "그룹 / 전사 관리자 구분 APIs")
public class ManagerConfigController extends AbstractCommonController {
	private final ManagerConfigService managerConfigService;
	private final RedisRepository redisRepository;

	public ManagerConfigController(RedisRepository redisRepository, ManagerConfigService managerConfigService) {
    this.redisRepository = redisRepository;
		this.managerConfigService = managerConfigService;
	}

	@GetMapping("/manager")
	@ApiOperation(value = "관리자 여부 체크")
	public ApiResult<Map<String, Object>> selectManagerChk(@AuthenticationPrincipal JwtAuthentication authentication) {
    UserSession userSession = (UserSession) redisRepository.getObject(authentication.loginId, UserSession.class);
		Boolean type = managerConfigService.selectManagerChk(authentication.loginId);
		Map<String, Object> rst = new HashMap<>();
		rst.put("user", userSession);
		rst.put("isMgr", type);
    return OK(rst);
	}
	
	@GetMapping("/dept/dept-manager")
  @ApiOperation(value = "관리자 부서 정보")
  public ApiResult<List<Mgr>> selectDeptManagerConfig(@AuthenticationPrincipal JwtAuthentication authentication) {
    List<Mgr> rst = managerConfigService.selectDeptManagerConfig(authentication.loginId);
    return OK(rst);
  }
	

}
