package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.stopword.StopWordDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;
import com.dongkuksystems.dbox.models.type.manager.stopword.StopWord;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.stopword.StopWordService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "불용어 관리 APIs")
public class StopWordController extends AbstractCommonController {
  private final StopWordService StopWordService;

  public StopWordController(StopWordService StopWordService) {
    this.StopWordService = StopWordService;
  }

  @GetMapping("/stopWordGroup/{companyCode}")
  @ApiOperation(value = "그룹 공통 불용어리스트  조회")
  public ApiResult<List<StopWord>> selectStopWordGroup(@AuthenticationPrincipal JwtAuthentication authentication,
	      @PathVariable String companyCode ) {    
	    List<StopWord> list = StopWordService.selectStopWordGroup(companyCode);
	    return OK(list);
  }
  
  @GetMapping("/stopWord/{companyCode}")
  @ApiOperation(value = "그룹사별 불용어 리스트  조회")
  public ApiResult<List<StopWord>> selectStopWord(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String companyCode ) {    
    List<StopWord> list = StopWordService.selectStopWord(companyCode);
    return OK(list);
  }
  
  
  @GetMapping("/blindDept/{companyCode}")
  @ApiOperation(value = "검색제외 부서 리스트  조회")
  public ApiResult<List<StopWord>> selectBlindDept(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String companyCode ) {    
    List<StopWord> list = StopWordService.selectBlindDept(companyCode);
    return OK(list);
  }
  
/*  @GetMapping("/stopWordGroup")
  @ApiOperation(value = "그룹 공통 불용어리스트  조회")
  public ApiResult<List<StopWord>> selectStopWordGroup(@AuthenticationPrincipal JwtAuthentication authentication) {    
    String type = "A";
    List<StopWord> list = StopWordGroupService.selectStopWordGroup(type);
    return OK(list);
  }
*/
   

}
