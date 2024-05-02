package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.dto.table.logodd.LogOddFilterDto;
import com.dongkuksystems.dbox.models.dto.table.statodd.StatOddFilterDto;
import com.dongkuksystems.dbox.models.table.log.LogOdd;
import com.dongkuksystems.dbox.models.table.stat.StatOdd;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.statodd.StatOddService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
@Api(tags = "특이 사용 이력 APIs")
public class StatOddController extends AbstractCommonController {
	private final StatOddService statOddService;

	public StatOddController(StatOddService statOddService) {
		this.statOddService = statOddService;
	}

	@GetMapping("/stat-odd")
	@ApiOperation(value = "특이 사용 이력 조회")
	public ApiResult<List<StatOdd>> selectStatOdd(@ModelAttribute StatOddFilterDto filter, @AuthenticationPrincipal JwtAuthentication authentication) {
		List<StatOdd> list = statOddService.selectStatOdd(filter);
		return OK(list);
	}
	
	@GetMapping("/log-odd")
	@ApiOperation(value = "특이 사용 일별 이력 조회")
	public ApiResult<List<LogOdd>> selectLogOdd(@ModelAttribute LogOddFilterDto filter, @AuthenticationPrincipal JwtAuthentication authentication) {
		List<LogOdd> list = statOddService.selectLogOdd(filter);
		return OK(list);
	}


}
