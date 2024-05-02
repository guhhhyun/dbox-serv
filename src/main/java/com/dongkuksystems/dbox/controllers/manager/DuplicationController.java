package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.dongkuksystems.dbox.config.support.SimpleOffsetPageRequest;
import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.duplication.DuplicationDto;
import com.dongkuksystems.dbox.models.dto.type.manager.duplication.PatchDuplicationDto;
import com.dongkuksystems.dbox.models.type.manager.duplication.Duplication;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.duplication.DuplicationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "중복 자료 관리 APIs")
public class DuplicationController extends AbstractCommonController {
	private final DuplicationService duplicationService;
	
	public DuplicationController(DuplicationService duplicationService) {
		this.duplicationService = duplicationService;
	}

	@GetMapping("/duplication")
	@ApiOperation(value = "중복 자료 관리 조회")
	public ApiResult<Map<String, Object>> selectAll(@AuthenticationPrincipal JwtAuthentication authentication, DuplicationDto dto,  Model model) {
		Map<String, Object> result = new HashMap<String, Object>();
    int totalCnt = duplicationService.selectAllCount(dto); 
    SimpleOffsetPageRequest pageable = new SimpleOffsetPageRequest(Long.parseLong(dto.getOffset()),  Integer.parseInt(dto.getLimit()));
    List<Duplication> list = duplicationService.selectAll(dto, pageable.offset(), pageable.limit()); 
    result.put("totalCnt", totalCnt);
    result.put("list", list);
    
    return OK(result);
	}

	@GetMapping("/duplicationlist")
	@ApiOperation(value = "중복 자료 삭제 리스트 조회")
	public ApiResult<List<Duplication>> selectList(@AuthenticationPrincipal JwtAuthentication authentication, DuplicationDto dto,  Model model) {
		List<Duplication> list = duplicationService.selectList(dto);
		return OK(list);
	}
	
	@PatchMapping("/duplicationlist/update/{rObjectId}")
	@ApiOperation(value = "문서 첨부 정책 수정")
	public ApiResult<Duplication> patchDuplication(@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId, @RequestBody PatchDuplicationDto patchDuplicationDto) throws Exception {
		
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		String rst = duplicationService.patchDuplication(rObjectId, userSession, patchDuplicationDto);
		return OK(null);
	}

	@PostMapping("/duplication/send")
  @ApiOperation(value = "중복 자료 메일 발송")
  public ApiResult<Map<String, Integer>> sendAllMail(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody DuplicationDto dto) throws Exception {
 
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    Map<String, Integer> result = duplicationService.sendAllMail(userSession, dto);
    return OK(result);
  }
	

}
