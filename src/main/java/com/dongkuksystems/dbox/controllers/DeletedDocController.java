package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.DocRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.RecycleDetailDto;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.recycle.Recycle;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.recycle.RecycleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "휴지통 APIs")
public class DeletedDocController extends AbstractCommonController {

	private final DocService docService;
	private final RecycleService recycleService;

	public DeletedDocController(DocService docService, RecycleService recycleService) {
		this.docService = docService;
		this.recycleService = recycleService;
	}
	@GetMapping(value = "/deleted-data/{dataId}")
	@ApiOperation(value = "휴지통 자료 조회")
	public ApiResult<List<RecycleDetailDto>> getDeletedDataOne(@AuthenticationPrincipal JwtAuthentication authentication,
      HttpServletRequest request, @PathVariable String dataId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    List<RecycleDetailDto> tree = recycleService.getDeletedDataByOrgId(userSession, dataId);
    return OK(tree);
  }
	@GetMapping(value = "/deleted-data")
	@ApiOperation(value = "휴지통 자료 리스트 조회")
	public ApiResult<List<RecycleDetailDto>> getDeletedDataList(@AuthenticationPrincipal JwtAuthentication authentication,
			//@RequestParam("deptId") Optional<String> deptId,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
	// TODO FIXED soik.cha 에러표시가 나서 빌드를 위해 우선 수정해두었습니다. 변경사항 확인 후 작업해주세요.
    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;
		List<RecycleDetailDto> tree = recycleService.getDeletedDataList(userSession, isMobile);
		return OK(tree);
	}

	@PostMapping(value = "/deleted-data/{dataId}/restore")
	@ApiOperation(value = "휴지통 자료 복원")
	public ApiResult<String> postDeletedDataRestoreOne(@AuthenticationPrincipal JwtAuthentication authentication,
			 @PathVariable String dataId, HttpServletRequest request, @RequestBody(required = false) DeleteManageDto dto)
			throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = getClientIp(request);
		String rst = recycleService.restoreDataByOrgId(userSession, dataId, ip, dto);
		return OK(null);
	}

	@PostMapping(value = "/deleted-data/restore")
	@ApiOperation(value = "휴지통 자료 리스트 복원")
	public ApiResult<Map<String, Integer>> postDeletedDataRestoreList(@AuthenticationPrincipal JwtAuthentication authentication,
			@RequestParam String restoreIds, HttpServletRequest request) throws Exception {
		
		List<String> retoreAllList = new ObjectMapper().readValue(restoreIds,
				new TypeReference<List<String>>() {
				});
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = getClientIp(request);
		Map<String, Integer> result = recycleService.restoreAllDataByIds(userSession, retoreAllList, ip);
		return OK(result);
	}

	@DeleteMapping(value = "/deleted-data/{dataId}")
	@ApiOperation(value = "휴지통 자료 삭제")
	public ApiResult<Boolean> deleteDeletedDataRestoreOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String dataId, HttpServletRequest request, @RequestBody(reuiredq = false) DeleteManageDto dto)
			throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = getClientIp(request);
		String rst = recycleService.deleteData(userSession, dataId, ip, dto);
		return OK(true);
	}

	@DeleteMapping(value = "/deleted-data")
	@ApiOperation(value = "휴지통 자료 리스트 삭제")
	public ApiResult<Map<String, Integer>> deleteDeletedDataList(@AuthenticationPrincipal JwtAuthentication authentication,
			 @RequestParam String dataIds, HttpServletRequest request) throws Exception {
		
		List<String> deleteAllList = new ObjectMapper().readValue(dataIds, new TypeReference<List<String>>() {
		});
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = getClientIp(request);
		Map<String, Integer> result = recycleService.deleteAllData(userSession, deleteAllList, ip);
		return OK(result);
	}

}
