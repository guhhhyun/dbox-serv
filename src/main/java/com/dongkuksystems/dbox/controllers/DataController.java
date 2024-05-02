package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;
import static com.dongkuksystems.dbox.models.api.response.ApiResult.FAIL;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.constants.UploadResultCode;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateBatchReqDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateReqDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataViewCheckoutDto;
import com.dongkuksystems.dbox.models.dto.type.data.RegistDataDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocUnLockDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackCreateDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackDetailDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackFatchDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadResultDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.agent.AgentService;
import com.dongkuksystems.dbox.services.data.DataService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.feedback.FeedbackService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.utils.CommonUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "자료 APIs")
public class DataController extends AbstractCommonController {
	@Value("${synap.viewer.ip}")
	private String[] synapViewerIps;
	
	private static final String LockFolderDto = null;
	private final DataService dataService;
	private final FolderService folderService;
	private final DocService docService;
	private final FeedbackService feedbackService;
	private final AgentService agentService;

	public DataController(DataService dataService, FolderService folderService, DocService docService, 
			FeedbackService feedbackService, AgentService agentService) {
		this.dataService = dataService;
		this.folderService = folderService;
		this.docService = docService;
    this.feedbackService = feedbackService;
    this.agentService = agentService;
	}

	@GetMapping(value = "/data/{dataId}")
	@ApiOperation(value = "자료 조회")
	public ApiResult<DataDetailDto> getDataOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "폴더/문서의 키값") @PathVariable String dataId,
			@ApiParam(value = "uDocKey를 통한 조회인지 여부") @RequestParam(required = false) boolean isUDocKey,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		
    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
    
    DataDetailDto result = dataService.getDataOne(userSession, dataId, isUDocKey, isMobile);

		return OK(result);
	}

	@GetMapping(value = "/data")
	@ApiOperation(value = "자료 리스트 조회(일반,통합 검색)")
	public ApiResult<Object> getDataList(@AuthenticationPrincipal JwtAuthentication authentication,
			
	      // 일반검색 파라미터
	        @RequestParam String searchName, // 검색 내용
			@ApiParam(value = "폴더Id") @RequestParam(required = false) String dataId,  // 폴더 현재 위치(전자결제, 연구과제, 팀, 공유 폴더가 아닌 현재 폴더 위치)
			@ApiParam(value = "함 종류") @RequestParam(required = false) String hamSearchType,  // 
			@ApiParam(value = "캐비넷코드", example = "CabnetCode") @RequestParam(required = false)  String dataCabinetCode,
			// 검색 모드 분할
			@ApiParam(value = "검색모드", example = "Search") @RequestParam(required = false)  String mode,  // 검색모드 (일반검색 : Search, 통합검색 : Dsearch)
			// 공통파라미터
			  //부서함 케비넷코드, 연구과제 함 코드, 공유코드, 등등
			@ApiParam(value = "폴더 종류", example = "DFO")@RequestParam(required = false)  String folderType, 
			//통합검색 파라미터
			@ApiParam(value = "Code") @RequestParam(required = false)  String folderCode,
			@ApiParam(value = "부서코드") @RequestParam(required = false)  String deptCode,
	        HttpServletRequest request,
	        HttpServletResponse response) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,UserSession.class);
		String ip = request.getRemoteAddr();
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;
		List<DataDetailDto> result = new ArrayList<>();
		try {
  		if(mode.equals("Dsearch")) {
  			result= dataService.getDataDsearchList(searchName , dataId, userSession, deptCode, folderCode, folderType);
  		} else {
  		    result = dataService.getDataList(searchName, dataId, userSession,  ip,
  		        hamSearchType, folderType, isMobile, dataCabinetCode);
  		}
		} catch (Exception e) {
      Map<String, Object> error = new HashMap<>();
      if (e instanceof BadRequestException) {
        error.put("message", e.getMessage()); 
        error.put("status", HttpStatus.BAD_REQUEST.value());
        return FAIL(error);
      } else if (e instanceof ForbiddenException) {
        error.put("message", e.getMessage()); 
        error.put("status", HttpStatus.FORBIDDEN.value());
        return FAIL(error);
      } else {
        error.put("message", "에러가 발생했습니다."); 
        error.put("status", 500);
        return FAIL(error);
      }
    }
		
		 return OK(result);
	}  
	@GetMapping(value = "/data/{dataId}/children")
	@ApiOperation(value = "자료(폴더) 하위 리스트 조회 ")
	public ApiResult<List<DataDetailDto>> getDataChildren(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "부모 폴더 Id") @PathVariable String dataId,
			@ApiParam(value = "함 종류") @RequestParam(required = false) String hamType,
      @ApiParam(value = "폴더 종류", example = "DFO") @RequestParam(required = false) String folderType,
      @ApiParam(value = "권한", example = "R") @RequestParam(required = false) String level,
			@ApiParam(value = "문서 포함 여부", example = "true") @RequestParam(required = false) boolean withDoc,
			@ApiParam(value = "하위 존재 여부", example = "true") @RequestParam(required = false) boolean checkHasChildren,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
	    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
		List<DataDetailDto> result = dataService.getDataChildren(userSession, dataId, hamType, folderType, level, withDoc, checkHasChildren, isMobile);

		return OK(result);
	}

	@GetMapping(value = "/data/{dataId}/descendants")
	@ApiOperation(value = "폴더 자손 리스트 조회 ")
	public ApiResult<List<DocDescendantDto>> getDataDescendants(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "부모 폴더 Id") @PathVariable String dataId,
      @ApiParam(value = "본인리스트만 조회") @RequestParam(required = false, defaultValue = "false") boolean download,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		
  	boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
    
		List<DocDescendantDto> result = dataService.getDataDescendants(userSession, dataId, isMobile, download);

		return OK(result);
	}

	@PostMapping(value = "/data/{dataId}/children")
	@ApiOperation(value = "자료(폴더/프로젝트 분류폴더/템플릿 문서) 생성")
	public ApiResult<String> postData(@AuthenticationPrincipal JwtAuthentication authentication, HttpServletRequest request,
			@ApiParam(value = "자료생성 정보") @RequestBody RegistDataDto registDataDto,
			@ApiParam(value = "상위폴더  obj Id", example = "213211") @PathVariable String dataId)
			throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = getClientIp(request);
		registDataDto.addAdditionalInfo(dataId);
		String objId = dataService.registData(userSession, registDataDto, null, null, ip, null, null);
		return OK(objId);
	}

	@GetMapping(value = "/data/{dataId}/content", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ApiOperation(value = "자료(문서) 다운로드")
	public Object getDataContent(
			@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "문서 아이디") @PathVariable String dataId,
      @ApiParam(value = "uDocKey 여부", example = "false") @RequestParam(required = false) boolean isUDocKey,
      @ApiParam(value = "암호화 해제 여부", example = "false") @RequestParam(required = false) boolean noEncrypt,
      @ApiParam(value = "반출함 아이디") @RequestParam(required = false) String approveId,
      @ApiParam(value = "시스템 정보 (암호화됨)") @RequestParam(required = false) String syspath,
  		HttpServletRequest request
  	) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		
    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
    String ip = request.getRemoteAddr();
    String browser = CommonUtils.getBrowser(request);

    boolean isViewer = Arrays.asList(synapViewerIps).stream().anyMatch(item -> StringUtils.equals(item, ip));

    Object result = dataService.downloadData(userSession, dataId, isUDocKey, "1", browser, isMobile, noEncrypt, isViewer, approveId, syspath, ip);

		return result;
	}

	@PostMapping(value = "/data/{dataId}/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiOperation(value = "자료(문서) 업로드", notes = "파일 단건 업로드", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResult<String> postDataContent(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "업로드 정보") @ModelAttribute UploadDocDto doc,
			@ApiParam(value = "파일") @RequestPart(required = true) MultipartFile file) throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = docService.createDoc(userSession, doc, AttachedFile.toAttachedFile(file,
				MessageFormat.format(Commons.TMP_STORAGE_PATH, authentication.loginId), Commons.DEFAULT_EXTENSION));
		System.out.println("upload id: " + rst);
//    return OK(new Doc());
		return OK(rst);
	}

	@PatchMapping(value = "/data/{dataId}")
	@ApiOperation(value = "자료 수정")
	public ApiResult<Doc> patchDataOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(new Doc());
	}

	@PatchMapping(value = "/data")
	@ApiOperation(value = "자료 일괄 수정")
	public ApiResult<Doc> patchDataList(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(new Doc());
	}

	@PatchMapping(value = "/data/{dataId}/lock")
	@ApiOperation(value = "자료(폴더) 잠금")
	public ApiResult<Boolean> postDataLock(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "folder") @PathVariable String dataId,
			HttpServletRequest request,
      HttpServletResponse response
		) throws Exception {
		
			UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,UserSession.class);	
			String result = dataService.getLockFolderData(userSession, dataId);
			boolean rst ;
			if(result.equals("success")) {
				rst = true;
			}else {
				rst = false;
			}
			
			return OK(rst);
	}
	
	@PatchMapping(value = "/data/{dataId}/unlock")
	@ApiOperation(value = "자료(폴더) 잠금해제")
	public ApiResult<Boolean> postDataUnlock(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "folder") @PathVariable String dataId,
      HttpServletRequest request,
      HttpServletResponse response
		) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		String result = dataService.patchUnLockFolder(dataId, userSession);
		boolean rst ;
		if(result.equals("success")) {
			rst = true;
		}else {
			rst = false;
		}
		return OK(rst);
	}
	
	@PostMapping(value = "/data/{dataId}/close")
	@ApiOperation(value = "자료(문서) Closed 전환")  // 자료, 유저 ID 
	public ApiResult<Boolean> postDataClose(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "dataId") @PathVariable String dataId,
		      HttpServletRequest request,
		      HttpServletResponse response
			) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = request.getRemoteAddr();
		String result = dataService.patchDocClosed(dataId, userSession,ip);
		boolean rst ;
		if(result.equals("success")) {
			rst = true;
		}else {
			rst = false;
		}
		return OK(rst);
	}
	

	@PostMapping(value = "/data/{dataId}/unclose")
	@ApiOperation(value = "자료(폴더) Live 전환")
	public ApiResult<Boolean> postDataUnclose(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "dataId") @PathVariable String dataId,
			@ApiParam(value = "변경사유") @RequestBody DocUnLockDto docReason,
		      HttpServletRequest request,
		      HttpServletResponse response)
			throws Exception {
		String ip = request.getRemoteAddr();
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
		String result = dataService.patchDocUnClosed(dataId, docReason.getDocReason() , userSession,ip);
		boolean rst ;
		if(result.equals("success")) {
			rst = true;
		}else {
			rst = false;
		}
		return OK(rst);
		
	}

	@DeleteMapping(value = "/data/{dataId}")
	@ApiOperation(value = "자료 삭제")
	public ApiResult<Boolean> deleteDataOne(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(true);
	}

	@DeleteMapping(value = "/data")
	@ApiOperation(value = "자료 일괄 삭제")
	public ApiResult<List<Map<String, String>>> deleteDataList(@AuthenticationPrincipal JwtAuthentication authentication)
			throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(null);
	}

	@PostMapping(value = "/data/{dataId}/move")
	@ApiOperation(value = "자료 이동", notes = "")
	public ApiResult<String> postDataMoveOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "obj Id", example = "213211") @PathVariable String dataId,
			@ApiParam(value = "정보") @ModelAttribute RegistFolderDto folderDto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(null);
	}

	@PostMapping(value = "/data/move")
	@ApiOperation(value = "자료 일괄 이동", notes = "")
	public ApiResult<String> postDataMoveList(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "정보") @ModelAttribute RegistFolderDto folderDto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(null);
	}

	@PostMapping(value = "/data/{dataId}/transfer")
	@ApiOperation(value = "자료 이관", notes = "")
	public ApiResult<String> postDataTransferOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "obj Id", example = "213211") @PathVariable String dataId,
			@ApiParam(value = "정보") @ModelAttribute RegistFolderDto folderDto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(null);
	}

	@PostMapping(value = "/data/transfer")
	@ApiOperation(value = "자료 일괄 이관", notes = "")
	public ApiResult<String> postDataTransferList(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "정보") @ModelAttribute RegistFolderDto folderDto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(null);
	}

	@PostMapping(value = "/data/{dataId}/copy")
	@ApiOperation(value = "자료 복사", notes = "")
	public ApiResult<String> postDataCopyOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "obj Id", example = "213211") @PathVariable String dataId,
			@ApiParam(value = "정보") @ModelAttribute RegistFolderDto folderDto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(null);
	}

	@PostMapping(value = "/data/copy")
	@ApiOperation(value = "자료 일괄 복사", notes = "")
	public ApiResult<String> postDataCopyList(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "정보") @ModelAttribute RegistFolderDto folderDto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		return OK(null);
	}

	@GetMapping(value = "/data/{dataId}/paths")
	@ApiOperation(value = "자료 경로 조회", notes = "")
	public ApiResult<List<DataDetailDto>> getDataPathList(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "자료 아이디") @PathVariable String dataId,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
    
    List<DataDetailDto> result = dataService.getDataPaths(userSession, dataId, isMobile);

		return OK(result);
	}

  @GetMapping(value = "/data/{dataId}/fullpaths")
  @ApiOperation(value = "자료 경로 조회", notes = "")
  public ApiResult<String> getDataFullPathList(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "폴더 아이디") @PathVariable String dataId,
      @ApiParam(value = "폴더 타입") @RequestParam( required = false ) String folderType,
      @ApiParam(value = "부서 코드") @RequestParam( required = false ) String deptCode,
      HttpServletRequest request) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    
    String deptCodeIfNotEmpty = userSession.getUser().getOrgId();
    if (deptCode != null) deptCodeIfNotEmpty = deptCode;
        
    String result = dataService.getDataFullPaths(dataId, folderType, deptCodeIfNotEmpty);

    return OK(result);
  }

	@GetMapping(value = "/data/{dataId}/versions/{versionId}")
	@ApiOperation(value = "자료 버전 조회", notes = "")
	public ApiResult<String> getDataVersionOne(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId,
			@ApiParam(value = "doc version id", example = "213211") @PathVariable String versionId) throws Exception {
		return OK(null);
	}

	@GetMapping(value = "/data/{dataId}/versions")
	@ApiOperation(value = "자료 버전리스트 조회", notes = "")
	public ApiResult<List<DocVersionListDto>> getDataVersionList(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;
		List<DocVersionListDto> result = dataService.getDataVersionListPaths(userSession, dataId, isMobile);
		return OK(result);
	}

	@GetMapping(value = "/data/{dataType}/{dataId}/creators")
	@ApiOperation(value = "자료 작성자 조회")
	public ApiResult<List<DataCreatorDto>> getDataCreators(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String dataType,
			@PathVariable String dataId) {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		List<DataCreatorDto> result = dataService.getDataCreators(userSession, dataType, dataId);
		return OK(result);
	}

	@PatchMapping(value = "/data/{dataId}/versions/{docVersionChck}")
	@ApiOperation(value = "자료 버전 수정", notes = "")
	public ApiResult<String> patchDataVersion(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id") @PathVariable String dataId ,
			HttpServletRequest request,
			@ApiParam(value = "버전유지여부") @RequestParam boolean docVersionChck) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String ip = request.getRemoteAddr();
		String rst = dataService.postDocVersion(userSession, dataId ,docVersionChck , ip);
		return OK(rst);
	}

	@GetMapping(value = "/data/{dataId}/versions/{versionId}/content")
	@ApiOperation(value = "자료 버전 다운로드")
	public ResponseEntity<InputStreamResource> getDataVersionContent(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId,
			@ApiParam(value = "doc version id", example = "213211") @PathVariable String versionId) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		CustomInputStreamResource rst = docService.downloadDoc(userSession, dataId);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Content-Disposition", "attachment; filename=" + rst.getFilename());
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		return ResponseEntity.ok().headers(headers).contentLength(rst.contentLength())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(new InputStreamResource(rst.getInputStream()));
	}
	
	@GetMapping(value = "/data/{dataId}/feedbacks")
	@ApiOperation(value = "자료(문서) 피드백 리스트 조회", notes = "")
	public ApiResult<List<FeedbackDetailDto>> getDataFeedbackList(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		List<FeedbackDetailDto> result = feedbackService.getFeedbackList(userSession, dataId, request);
		return OK(result);
	}

	@PostMapping(value = "/data/{dataId}/feedbacks")
	@ApiOperation(value = "자료(문서) 피드백 생성", notes = "")
	public ApiResult<String> postDataFeedback(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId, FeedbackCreateDto dto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
		String rst = feedbackService.createFeedback(userSession, dto, dataId);
		
		return OK(null);
	}

	@PatchMapping(value = "/data/{dataId}/feedbacks/{feedbackId}")
	@ApiOperation(value = "자료(문서) 피드백 수정")
	public ApiResult<Doc> patchDataFeedback(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc feedback id", example = "213211") @PathVariable String feedbackId
			,  @PathVariable String dataId, FeedbackFatchDto dto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = feedbackService.patchFeedback(userSession, dto, dataId, feedbackId);
		return OK(new Doc());
	}

	@DeleteMapping(value = "/data/{dataId}/feedbacks/{feedbackId}")
	@ApiOperation(value = "자료(문서) 피드백 삭제")
	public ApiResult<Boolean> deleteDataFeeback(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc feedback id", example = "213211") @PathVariable String feedbackId, @PathVariable String dataId) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = feedbackService.deleteFeedback(userSession, feedbackId);
		return OK(true);
	}

	// comment의 comment고려
	@PostMapping(value = "/data/{dataId}/feedbacks/{feedbackId}/comments")
	@ApiOperation(value = "자료(문서) 피드백 댓글 생성", notes = "")
	public ApiResult<String> postDataFeedbackComment(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId,
			@ApiParam(value = "doc feedback id", example = "213211") @PathVariable String feedbackId,
			FeedbackCreateDto dto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = feedbackService.createComment(userSession, dto, dataId, feedbackId);
		return OK(null);
	}

	@PatchMapping(value = "/data/{dataId}/feedbacks/{feedbackId}/comments/{commentId}")
	@ApiOperation(value = "자료(문서) 피드백 댓글 수정")
	public ApiResult<Doc> patchDataFeedbackComment(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc feedback id", example = "213211") @PathVariable String feedbackId,
			@ApiParam(value = "doc feedback comment id", example = "213211") @PathVariable String commentId,
			FeedbackFatchDto dto)
			throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = feedbackService.patchComment(userSession, feedbackId, commentId, dto);
		return OK(new Doc());
	}

	@DeleteMapping(value = "/data/{dataId}/feedbacks/{feedbackId}/comments/{commentId}")
	@ApiOperation(value = "자료(문서) 피드백 댓글 삭제")
	public ApiResult<Boolean> deleteDataFeebackComment(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc feedback id", example = "213211") @PathVariable String feedbackId,
			@ApiParam(value = "doc feedback comment id", example = "213211") @PathVariable String commentId)
			throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		String rst = feedbackService.deleteComment(userSession, commentId);
		return OK(true);
	}


  @PostMapping(value = "/data/{dataId}/children/duple-check")
  @ApiOperation(value = "대상 하위 파일명 중복 체크")
  public ApiResult<Boolean> checkDocsDuple(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "doc obj Id", example = "213211") @PathVariable String dataId,
      @ApiParam(value = "조회 정보") @RequestBody DocFilterDto filterDto)
      throws Exception {
    filterDto.addInfo(dataId);
    return OK(dataService.checkDocsDuple(filterDto));
  }
	
	@GetMapping(value = "/data/{dataId}/related-approvals")
	@ApiOperation(value = "관련 전자결재 문서 목록 조회", notes = "")
	public ApiResult<List<DocLinkListDto>> getDocsEApproval(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "doc obj Id") @PathVariable String dataId,
			HttpServletRequest request
			) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;
		List<DocLinkListDto> result  = dataService.getDataLinkListPaths(userSession, dataId, isMobile);
		return OK(result);
	}
	
	
	// TODO softm	
		@GetMapping(value = "/data/{dataId}/versions/{versionId}/content2")
		@ApiOperation(value = "자료 다운로드")
		public ResponseEntity<InputStreamResource> downloadDocumentTestOrigin(		
				@AuthenticationPrincipal JwtAuthentication authentication,
				@ApiParam(value = "Doc", example = "090004d280005103") @PathVariable String dataId,
				@ApiParam(value = "잠금 여부", example = "true") @RequestParam boolean isLock
		) throws Exception {
			UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
					UserSession.class);
			CustomInputStreamResource rst = docService.downloadDoc(userSession, dataId);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Content-Disposition", "attachment; filename=" + rst.getFilename());
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
//			VUser user = userSevice.selectOneByUserId(loginId).orElseThrow(() -> new NotFoundException(VUser.class, loginId));
			userSession.getUser().getOrgId().equals(userSession.getSocialPerId());
			return ResponseEntity.ok().headers(headers).contentLength(rst.contentLength())
					.contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(new InputStreamResource(rst.getInputStream()));
		}
		
	// TODO softm	
		@GetMapping(value = "/data/{dataId}/versions/{versionId}/download")
		@ApiOperation(value = "자료 다운로드")
		public Object getDocumentDownloadOrFolder(
			@AuthenticationPrincipal JwtAuthentication authentication,
			// docid : 090004d2800056d8 , folderid : 000004d280002a2c
			@ApiParam(value = "Doc", example = "doc : 090004d28000643a, folder : 000004d2800037c4") @PathVariable String dataId,
			@ApiParam(value = "잠금 여부", example = "true") @RequestParam boolean isLock,
			@ApiParam(value = "암호화 해제 여부", example = "false") @RequestParam boolean noEncrypt,
			@ApiParam(value = "doc version id", example = "213211") @PathVariable String versionId,
			HttpServletRequest request
		) throws Exception {
			UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
			
		    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
		    String ip = request.getRemoteAddr();
		    String browser = CommonUtils.getBrowser(request);

		    Object result = dataService.downloadData(userSession, dataId, false, versionId, browser, isMobile, noEncrypt, false, null, null, ip);

			return result;
		}
		
		// TODO softm	
		@PatchMapping(value = "/data/{dataId}/versions/{versionId}/update")
		@ApiOperation(value = "자료 수정")
		public Object updateDocumentNFolder(
			@AuthenticationPrincipal JwtAuthentication authentication,
			// docid : 090004d2800056d8 , folderid : 000004d280002a2c
			@ApiParam(value = "Doc", example = "doc : 090004d280006484, folder : 000004d2800037c4") @PathVariable String dataId,
			@ApiParam(value = "잠금 여부", example = "true") @RequestParam boolean isLock,
			@ApiParam(value = "isUDocKey 사용 여부", example = "true") @RequestParam(required = false, defaultValue = "true") boolean isUDocKey,
			@ApiParam(value = "doc version id", example = "213211") @PathVariable String versionId,
			@RequestBody DataUpdateReqDto dataUpdateReqDto,
		      HttpServletRequest request,
		      HttpServletResponse response
		) throws Exception {

			UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
			
		    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
		    String ip = request.getRemoteAddr();
		    String browser = CommonUtils.getBrowser(request);
	
		    Object result = dataService.updateData(userSession, dataId, versionId, dataUpdateReqDto, browser, isMobile, isLock, isUDocKey, false, ip);
	
			return result;
		}

		@PostMapping(value = "/versions/{versionId}/update/batch")
		@ApiOperation(value = "일괄 자료 수정")
		public Object updateDocumentNFolderForBatch(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "잠금 여부", example = "true") @RequestParam boolean isLock,
			@ApiParam(value = "doc version id", example = "213211") @PathVariable String versionId,
			@RequestBody DataUpdateBatchReqDto dataUpdateBatchReqDto,
			HttpServletRequest request) throws Exception {

			UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
			
		    boolean isMobile = request.getHeader("User-Agent").toUpperCase().contains("MOBILE");	// TODO flutter에서도 모바일 여부 확인 가능한지
		    String ip = request.getRemoteAddr();
		    String browser = CommonUtils.getBrowser(request);

			return dataService.updateDataForBatch(userSession, versionId, dataUpdateBatchReqDto, browser, isMobile, isLock, ip);
		}
		
    @GetMapping(value = "/data/{dataId}/lock/{dataType}")
    @ApiOperation(value = "자료 잠금여부확인")
    public ApiResult<Map<String, Object>> getDocLockStatus(
      @AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "Doc", example = "doc : 090004d28000643a, folder : 000004d2800037c4") @PathVariable String dataId,
      @ApiParam(value = "dataType", example = "doc : D, folder : F") @PathVariable String dataType,
      @ApiParam(value = "편집권한 보기", example = "true") @RequestParam boolean hasWAuth,
      @ApiParam(value = "문서 최신정보 보기", example = "true") @RequestParam String sOpenContent
    ) throws Exception {
      UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
      return OK(dataService.isDataLock(userSession, dataId, dataType, hasWAuth, sOpenContent));
    }
	
	/**
	 * Agent 문서 보기 정보 확인
	 * @param authentication
	 * @param dataViewCheckoutDto
	 * @param dataId
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/data/{dataId}/viewcheck")
	@ApiOperation(value = "Agent 문서 보기 정보 확인")
	public ApiResult<DataViewCheckoutDto> getViewCheck(@AuthenticationPrincipal JwtAuthentication authentication,
			// @ApiParam(value = "문서보기 정보") @RequestBody DataViewCheckoutDto dataViewCheckoutDto,
			@ApiParam(value = "R_OBJECT_ID"		, example = "0900") @RequestParam String objId,
			@ApiParam(value = "U_DOC_KEY"		, example = "0900") @RequestParam String docKey,
			@ApiParam(value = "최신 선택된 ID 여부"	, example = "CURRENT, SELECTED") @RequestParam String sOpenContent,
			@ApiParam(value = "반출함 ID"			, example = "0000d00") @RequestParam String approveId,
			@ApiParam(value = "첨부시스템"			, example = "mail.com") @RequestParam String sMenu ) throws Exception {
		
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		DataViewCheckoutDto rst = docService.getViewCheck(userSession, objId, docKey, sOpenContent, approveId, sMenu);
		return OK(rst);
	}
	
	/**
	 * Agent 문서 편집 정보 확인
	 * @param authentication
	 * @param dataViewCheckoutDto
	 * @param dataId
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/data/{dataId}/checkoutcheck")
	@ApiOperation(value = "Agent 문서 편집 정보 확인")
	public ApiResult<DataViewCheckoutDto> getCheckoutCheck(@AuthenticationPrincipal JwtAuthentication authentication,
			// @ApiParam(value = "문서보기 정보") @RequestBody DataViewCheckoutDto dataViewCheckoutDto,
			@ApiParam(value = "R_OBJECT_ID"		, example = "0900") @RequestParam String objId,
			@ApiParam(value = "U_DOC_KEY"		, example = "0900") @RequestParam String docKey,
			@ApiParam(value = "최신 선택된 ID 여부"	, example = "CURRENT, SELECTED") @RequestParam String sOpenContent) throws Exception {
		
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		DataViewCheckoutDto rst = docService.getCheckoutCheck(userSession, objId, docKey, sOpenContent);
		return OK(rst);
	}
	
  /**
   * 상위폴더 잠금상태 확인
   * @param authentication
   * @param dataViewCheckoutDto
   * @param dataId
   * @return
   * @throws Exception
   */
  @GetMapping(value = "/data/{dataId}/check-ancestor-lock")
  @ApiOperation(value = "상위폴더 잠금 상태 확인")
  public ApiResult<Boolean> getCheckAncestorLock(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "부모 폴더 Id") @PathVariable String dataId) throws Exception {
    Integer rst = folderService.selectAncestorHasLock(dataId);
    return OK(rst == null || rst == 0 ?false:true);
  }

	@GetMapping(value = "/data/{dataId}/related-approval")
	@ApiOperation(value = "관련 전자결재 문서 목록 조회")
	public ApiResult<List<Map<String, String>>> getDocsEApproval(@PathVariable String dataId) {
		return OK(dataService.getDocsEApproval(dataId));
	}
  
  @GetMapping(value = "/data/{dataId}/url-download")
  @ApiOperation(value = "링크 다운로드용 자료 조회")
  public ApiResult<DataDetailDto> getUrlDownloadDataOne(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "폴더/문서의 키값") @PathVariable String dataId,
      @ApiParam(value = "uDocKey를 통한 조회인지 여부") @RequestParam(required = false) boolean isUDocKey,
      HttpServletRequest request) throws Exception {
    UserSession userSession = authentication!= null ? (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class) : null;

    DataDetailDto result = dataService.getDataOne(userSession, dataId, isUDocKey, false);

    return OK(result);
  }
  
  @GetMapping(value = "/data/{uDocKey}/syspath")
  @ApiOperation(value = "syspath 조회")
  public ApiResult<String> getUrlDownloadDataOne(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "폴더/문서의 키값") @PathVariable String uDocKey,
      @ApiParam(value = "syspath") @RequestParam(required = false, defaultValue = "dbox") String syspath,
      HttpServletRequest request) throws Exception {
    String result = agentService.makeSyspath(uDocKey, syspath);

    return OK(result);
  }

  @GetMapping(value = "/data/upload-result")
  @ApiOperation(value = "업로드 결과 조회")
  public ApiResult<UploadResultDto> getUploadDataResult(@AuthenticationPrincipal JwtAuthentication authentication, 
      HttpServletRequest request) throws Exception {
    String key = authentication.loginId.concat("_UPLOAD_");
    UploadResultDto uploadRstDto = (UploadResultDto) getRedisRepository().getObject(key, UploadResultDto.class);
    if (uploadRstDto != null ) getRedisRepository().delete(key);
    if (uploadRstDto.getFailedCnt() != 0) {
      List<String> msg = new ArrayList<>();
      if (uploadRstDto.getAuthCnt() != 0) msg.add(MessageFormat.format(UploadResultCode.AUTH.getValue(), uploadRstDto.getAuthCnt())); 
      if (uploadRstDto.getLockCnt() != 0) msg.add(MessageFormat.format(UploadResultCode.LOCK.getValue(), uploadRstDto.getLockCnt())); 
      if (uploadRstDto.getExtCnt() != 0) msg.add(MessageFormat.format(UploadResultCode.EXT.getValue(), uploadRstDto.getExtCnt())); 
      if (uploadRstDto.getNameCnt() != 0) msg.add(MessageFormat.format(UploadResultCode.NAMELENGTH.getValue(), uploadRstDto.getNameCnt())); 
      if (uploadRstDto.getErrorCnt() != 0) msg.add(MessageFormat.format(UploadResultCode.ERROR.getValue(), uploadRstDto.getErrorCnt())); 
      uploadRstDto.setErrorMsg(msg);
    }
    return OK(uploadRstDto);
  }

  @DeleteMapping(value = "/data/upload-result")
  @ApiOperation(value = "업로드 결과 삭제")
  public ApiResult<Boolean> deleteUploadDataResult(@AuthenticationPrincipal JwtAuthentication authentication)
      throws Exception {
    String key = authentication.loginId.concat("_UPLOAD_");
    if (getRedisRepository().isExists(key)) getRedisRepository().delete(key);
    return OK(true);
  }
  
  @PostMapping(value = "/data/{dataId}/data/unlock")
  @ApiOperation(value = "자료 편집중 잠금해제")  // 자료, 유저 ID 
  public ApiResult<String> unlockDataOne(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "dataId") @PathVariable String dataId, HttpServletRequest request      
      ) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    String ip = getClientIp(request);
    String rst = dataService.unlockDataOne(dataId, userSession, ip);
    return OK(rst);
  }
}
