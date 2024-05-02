package com.dongkuksystems.dbox.controllers;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dongkuksystems.dbox.constants.DrmSecLevelType;
import com.dongkuksystems.dbox.models.dto.etc.DrmDeptDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;
import com.dongkuksystems.dbox.services.drm.DrmService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "DRM APIs")
public class DrmController extends AbstractCommonController {
	private final DrmService drmService;

	public DrmController(DrmService drmService) {
		this.drmService = drmService;
	}

	@PostMapping("/drm/decrypt")
	@ApiOperation(value = "복호화 테스트")
	public String decrypt(@RequestPart("filedata") MultipartFile multipartFile) throws Exception {
		File file = drmService.decrypt(multipartFile.getInputStream(), multipartFile.getOriginalFilename(),
				multipartFile.getSize());

		return file.getAbsolutePath();
	}

	@PostMapping("/drm/encrypt")
	@ApiOperation(value = "암호화 테스트")
	public String encrypt(@RequestPart("filedata") MultipartFile multipartFile) throws Exception {
		// 개인
//		List<DrmUserDto> authUserList = Arrays.asList(DrmUserDto.builder().userId("soik.cha").displayName("차소익").build());	// 샘플 데이터
//		File file = drmService.encrypt(multipartFile.getInputStream(), DrmSecLevelType.INDIVIDUAL, null, null, authUserList,
//				multipartFile.getOriginalFilename(), multipartFile.getSize(), "loginId!!", "userName!!", "deptCode!!",
//				"deptName!!", "entCode!!", "entName!!", "ip!!");

		// 부서와 개인
		List<DrmDeptDto> authDeptList = Arrays.asList(DrmDeptDto.builder().orgId("UNC50014030").orgNm("강병프로젝트TFT").build());	// 샘플 데이터
		List<DrmUserDto> authUserList = Arrays.asList(DrmUserDto.builder().userId("soik.cha").displayName("차소익").build());			// 샘플 데이터
//		File file = drmService.encrypt(multipartFile.getInputStream(), DrmSecLevelType.INDIVIDUAL, null, authDeptList, authUserList,
//				multipartFile.getOriginalFilename(), multipartFile.getSize(), "loginId!!", "userName!!", "deptCode!!",
//				"deptName!!", "entCode!!", "entName!!", "ip!!");

//		// 사내
//		DrmCompanyDto company = DrmCompanyDto.builder().companyId(DrmCompanyId.DKSYSTEMS.getValue()).companyName("동국시스템즈").build();	// 샘플 데이터
//		File file = drmService.encrypt(multipartFile.getInputStream(), DrmSecLevelType.COMPANY, company, null, null,
//				multipartFile.getOriginalFilename(), multipartFile.getSize(), "loginId!!", "userName!!", "deptCode!!",
//				"deptName!!", "entCode!!", "entName!!", "ip!!");

		// 그룹사내
//		File file = drmService.encrypt(multipartFile.getInputStream(), DrmSecLevelType.GROUP, null, null, null,
//				multipartFile.getOriginalFilename(), multipartFile.getSize(), "loginId!!", "userName!!", "deptCode!!",
//				"deptName!!", "entCode!!", "entName!!", "ip!!");

		return null;
	}

	@PostMapping("/drm/check")
	@ApiOperation(value = "암호화 여부 확인 테스트")
	public boolean check(@RequestPart("filedata") MultipartFile multipartFile) throws Exception {
		boolean result = drmService.check(multipartFile.getInputStream(), multipartFile.getOriginalFilename(),
				multipartFile.getSize());
		
		return result;
	}
}
