package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.folder.FolderService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api")
@Api(tags = "공유/협업 APIs")
public class ShareFolderController extends AbstractCommonController {
	private final FolderService folderService;
	private final AuthService authService;

	public ShareFolderController(FolderService folderService, AuthService authService) {
		this.folderService = folderService;
		this.authService = authService;
	}

  @GetMapping("/share-folder")
  @ApiOperation(value = "공유 폴더 리스트 조회")
  public ApiResult<List<DataDetailDto>> selectShareFolderList( @AuthenticationPrincipal JwtAuthentication authentication,
  		@ApiParam(value = "폴더이름") @RequestParam(required = false) String objectName
      ) throws Exception {
    List<DataDetailDto> list = folderService.selectShareFolderList(FolderFilterDto.builder().uFolName(objectName).build(), authentication.loginId);
    return OK(list);
  }
  
//	@GetMapping("/share-folder/{folderObjId}")
//	@ApiOperation(value = "사전 설정 조회")
//	public ApiResult<List<AuthShare>> selectShareAuths( @AuthenticationPrincipal JwtAuthentication authentication,
//      @ApiParam(value = "폴더 키값") @PathVariable String folderObjId) throws Exception {
//		List<AuthShare> list = authService.selectAuthShareList(folderObjId);
//		return OK(list);
//	}
	
	
}
