package com.dongkuksystems.dbox.controllers;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.UploadResultCode;
import com.dongkuksystems.dbox.daos.table.log.LogDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.errors.UnauthorizedException;
import com.dongkuksystems.dbox.errors.upload.UploadAuthException;
import com.dongkuksystems.dbox.errors.upload.UploadExtensionException;
import com.dongkuksystems.dbox.errors.upload.UploadLockException;
import com.dongkuksystems.dbox.errors.upload.UploadNameLengthException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadResultDto;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.data.DataService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.utils.CommonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raonwiz.kupload.RAONKHandler;
import com.raonwiz.kupload.event.EventClass;
import com.raonwiz.kupload.util.EventVo;
import com.raonwiz.kupload.util.RAONKParameterVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@Api(tags = "KUpload APIs")
public class KUploadController {
	@Value("${kupload.base-path}")
	private String basePath;
	@Value("${kupload.temp-path}")
	private String tempPath;
	@Value("${kupload.garbage-clean-day}")
	private int garbageCleanDay;
  @Value("${kupload.config-path}")
  private String configPath;

	private final ServletContext servletContext;
	private final RedisRepository redisRepository;
  private final ObjectMapper objectMapper;

	private final DataService dataService;
  private final DocService docService;
  private final LogDao logDao;
  
	public KUploadController(ServletContext servletContext, RedisRepository redisRepository, ObjectMapper objectMapper,
	    DataService dataService, DocService docService, LogDao logDao) {
		this.servletContext = servletContext;
    this.redisRepository = redisRepository;
    this.objectMapper = objectMapper;
		this.dataService = dataService;
		this.docService = docService;
		this.logDao = logDao;
	}

  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-FORWARDED-FOR");
    if (ip == null) ip = request.getRemoteAddr();
    
    return ip;
  }
  
  @GetMapping(value = "/kupload/raonkupload-config", produces = MediaType.APPLICATION_XML_VALUE)
  @ApiOperation(value = "raonkupload.config.xml")
  public ResponseEntity<Resource> raonkuploadConfigXml() throws Exception {
    ClassPathResource resource = new ClassPathResource(configPath);
    return ResponseEntity.ok().body(resource);
  }
  
	@RequestMapping(value = "/kupload/raonkhandler", method = { RequestMethod.GET, RequestMethod.POST })
	@ApiOperation(value = "raonkhandler")
	public ResponseEntity<String> raonkhandler(@AuthenticationPrincipal JwtAuthentication authentication,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
	  final JwtAuthentication finalAuthentication = authentication;
		RAONKHandler upload = new RAONKHandler();

		EventClass event = new EventClass();
		RAONKParameterVo parameterVo = new RAONKParameterVo();
		parameterVo.setLogType("L");
		upload.settingVo.setDebugMode(parameterVo);

		event.addAfterUploadEventListener((EventVo eventVo) -> {
		  UploadResultDto uploadRstDto = null;
      try {
        uploadRstDto = (UploadResultDto) redisRepository.getObject(finalAuthentication.loginId.concat("_UPLOAD_"), UploadResultDto.class);
        if (uploadRstDto == null) uploadRstDto = new UploadResultDto();
        int successCnt = uploadRstDto.getSuccessCnt();
        int uploadCnt = uploadRstDto.getUploadCnt();
        uploadRstDto.setUploadCnt(++uploadCnt);
        
        UserSession userSession = (UserSession) redisRepository.getObject(finalAuthentication.loginId, UserSession.class);
        if (userSession == null) throw new RuntimeException("User session not found");
        int chkLock = checkLockedUser(finalAuthentication.loginId, userSession.getToken());
        if (chkLock == 1) throw new UnauthorizedException("Logout User Token");
        if (chkLock == 2) throw new UnauthorizedException("Locked User");

        String strOriginalFileName = eventVo.getOriginalFileName(); //Original File Name
        String strNewFileLocation = eventVo.getNewFileLocation(); //NewFileLocation Value
        String strResponseFileServerPath = eventVo.getResponseFileServerPath(); //ResponseFileServerPath Value
        String strFileIndex = eventVo.getFileIndex(); //FileIndex Value - 마지막 파일은 index 뒤에 z가 붙습니다.
        String strGuid = eventVo.getGuid(); //Guid
        String path = eventVo.getObjectKeyFolderPath();
        String prType = Objects.isNull(eventVo.getUpload().getParameterValue("prType"))?null:eventVo.getUpload().getParameterValue("prType")[0];
//        String prType = "R";
        String hamType = Objects.isNull(prType)? eventVo.getUpload().getParameterValue("hamType")[0] : "D";
        String upObjectId = Objects.isNull(prType)? eventVo.getUpload().getParameterValue("upObjectId")[0]: userSession.getUser().getOrgId();
        String uploadFlag = eventVo.getUpload().getParameterValue("uploadFlag")[0];
        String ip = getClientIp(request);

        AttachedKUploadFile aFile;
        aFile = new AttachedKUploadFile(
        		strNewFileLocation,
        		strResponseFileServerPath,
            strOriginalFileName,
            strFileIndex,
            strGuid
            );
        UploadDocDto dto = UploadDocDto.builder().hamType(hamType).upObjectId(upObjectId).uploadFlag(uploadFlag).prType(prType).build();
        dataService.uploadDoc(userSession, dto, aFile, ip, null);
        response.setStatus(HttpStatus.CREATED.value()); 
        uploadRstDto.setSuccessCnt(++successCnt);
        redisRepository.put(finalAuthentication.loginId.concat("_UPLOAD_"), uploadRstDto, (long) 60 * 60); // 1시간
      } catch (Exception e) {
        e.printStackTrace();
        int failedCnt = uploadRstDto.getFailedCnt();
        uploadRstDto.setFailedCnt(++failedCnt);
        int tmpCnt= 0;
        try {
          if (e instanceof UploadExtensionException) {
            tmpCnt = uploadRstDto.getExtCnt();
            uploadRstDto.setExtCnt(tmpCnt + 1);
          } else if (e instanceof UploadLockException) {
            tmpCnt = uploadRstDto.getLockCnt();
            uploadRstDto.setLockCnt(tmpCnt + 1);
          } else if (e instanceof UploadAuthException) {
            tmpCnt = uploadRstDto.getAuthCnt();
            uploadRstDto.setAuthCnt(tmpCnt + 1);
          } else if (e instanceof UploadNameLengthException) {
            tmpCnt = uploadRstDto.getNameCnt();
            uploadRstDto.setNameCnt(tmpCnt + 1);
          } else {
            tmpCnt = uploadRstDto.getErrorCnt();
            uploadRstDto.setErrorCnt(tmpCnt + 1);
//            eventVo.setCustomError("909", "[에러] 관리자에게 문의해주세요.");
          }
          redisRepository.put(finalAuthentication.loginId.concat("_UPLOAD_"), uploadRstDto, (long) 60 * 60); // 1시간
        } catch (Exception e2) {
          System.out.println("");
        }
      }
    });
		
		// 다운로드 직전 이벤트
		event.addBeforeDownloadEventListener((eventVo) -> {
      try {
        UserSession userSession = finalAuthentication != null ? (UserSession) redisRepository.getObject(finalAuthentication.loginId, UserSession.class) : null;
        
        String[] customValArr = eventVo.getDownloadCustomValue();
        String customVal = customValArr != null && customValArr.length > 0 ? customValArr[0] : null;
        Map<String, String> customValMap = new HashMap<String, String>();
        if (customVal != null && !customVal.isEmpty()) {
          customValMap = objectMapper.readValue(customVal, new TypeReference<HashMap<String, String>>() {});
        }
        
        // 암호화 안함 여부
        final Boolean noEncrypt = customValMap.get("noEncrypt") != null ? Boolean.parseBoolean(customValMap.get("noEncrypt")): false;
        final String fileObjId = customValMap.get("fileObjId") != null ? customValMap.get("fileObjId"): null;
        
        // 시스템 정보 (암호화됨, 권한 자동 부여 여부 확인)
        final String approveId = customValMap.get("approveId");
        final String syspath = customValMap.get("syspath");

        // 반출함 하위일 경우에만 잠금여부 확인
        if (approveId == null) {
          int chkLock = checkLockedUser(finalAuthentication.loginId, userSession.getToken());
          if (chkLock == 1) throw new UnauthorizedException("Logout User Token");
          if (chkLock == 2) throw new UnauthorizedException("Locked User");
        }
        
  			String[] dataIdArr = eventVo.getDownloadFilePath();
	      List<String> filePathList = Arrays.asList(dataIdArr).stream().map((item) -> {
	        try {
	          String ip = request.getRemoteAddr();
	          String browser = CommonUtils.getBrowser(request);
	          ResponseEntity<Resource> responseEntity = (ResponseEntity<Resource>) dataService.downloadData(userSession, item, false, "1", browser, false, noEncrypt, false, approveId, syspath, ip);
	          
	          InputStream inputStream = responseEntity.getBody().getInputStream();
	          File tempFile = File.createTempFile(
	              (finalAuthentication != null ? (File.separator + finalAuthentication.loginId) : "")
	              + File.separator + String.valueOf(inputStream.hashCode()), ".tmp", new File(tempPath));
	          FileUtils.copyInputStreamToFile(inputStream, tempFile);
            
	          return tempFile.getAbsolutePath();
	        } catch (Exception e) {
	          if (e instanceof BadRequestException)
	            eventVo.setCustomError("905", e.getMessage());
	          else if (e instanceof ForbiddenException)
	            eventVo.setCustomError("904", e.getMessage());
	          else if (e instanceof UnauthorizedException)
	            eventVo.setCustomError("901", e.getMessage());
            else if (e instanceof NotFoundException)
              eventVo.setCustomError("906", e.getMessage());
            else if (e instanceof RuntimeException)
              eventVo.setCustomError("908", e.getMessage());
	          else {
	            eventVo.setCustomError("909", "[에러] 관리자에게 문의해주세요.");
	          }
	          throw new RuntimeException(e.getMessage());
	        }
	      }).collect(Collectors.toList());

	      String[] filePathArr = filePathList.toArray(new String[filePathList.size()]);
	      eventVo.setDownloadFilePath(filePathArr);

	      //다운로드 시작 시간 등록
	      if (fileObjId != null) {
	        ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul"));
	        long milli = zdt.toInstant().toEpochMilli();

	        String ip = request.getHeader("X-FORWARDED-FOR");
	        if (ip == null) ip = request.getRemoteAddr();
	        String downCheckKey = (ip == null || ip.isEmpty()) ? userSession.getUser().getUserId() : ip;
	        redisRepository.put(downCheckKey.concat("_DOWN_").concat(fileObjId), milli, (long) 60 * 60); // 1시간
	      }
      } catch (Exception e2) {
        e2.printStackTrace();
//        throw new RuntimeException();
      }
		});
		
		//다운로드 후 파일 삭제처리 & 로그 입력
		event.addAfterDownloadEventListener((eventVo) -> {
		  try {
		    String[] aryDownloadFilePath = eventVo.getDownloadFilePath(); //DownloadFilePath Value
		    for (String path: aryDownloadFilePath) {
		      File file = new File(path);
		      if (file.exists()) {
		        file.delete();
		      }
		    }
		    String[] customValArr = eventVo.getDownloadCustomValue();
        String customVal = customValArr != null && customValArr.length > 0 ? customValArr[0] : null;
        Map<String, String> customValMap = new HashMap<String, String>();
        if (customVal != null && !customVal.isEmpty()) {
          customValMap = objectMapper.readValue(customVal, new TypeReference<HashMap<String, String>>() {});
        }
        
        // 암호화 안함 여부
        final String fileObjId = customValMap.get("fileObjId") != null ? customValMap.get("fileObjId"): null;
        final String docVersion = customValMap.get("version") != null ? customValMap.get("version"): null;
        String timeKey = null;
        if (fileObjId != null) {
          try {
            UserSession userSession = finalAuthentication != null ? (UserSession) redisRepository.getObject(finalAuthentication.loginId, UserSession.class) : null;

            String ip = request.getHeader("X-FORWARDED-FOR");
            if (ip == null) ip = request.getRemoteAddr();
            String downCheckKey = (ip == null || ip.isEmpty()) ? userSession.getUser().getUserId() : ip;
            
            timeKey = downCheckKey.concat("_DOWN_").concat(fileObjId);
            Long milli = (Long) redisRepository.get(timeKey, Long.class);
            if (milli != null) {
              
              Doc doc = docService.selectOne(fileObjId).orElseThrow(() -> new NotFoundException("Doc not found"));
              //작업자 구분 획득
              String comCode = userSession != null ? userSession.getUser().getMgr().getCompanyComCode() : null;
              String groupCode = userSession != null ? userSession.getUser().getMgr().getGroupComCode() : null;
              List<String> depts = userSession != null ? userSession.getUser().getMgr().getCompanyDeptCode() : null;
              String userType = "P";
              try {
                if (depts != null) {
                  for (String dept : depts) {
                    if (doc.getUDeptCode().equals(dept)) {
                      userType = "D";
                      break;
                    }
                  }
                }
                if ("P".equals(userType) && comCode != null) {
                  if (doc.getOwnDeptDetail().getComOrgId().equals(comCode)) {
                    userType = "C";
                  }
                }
                if ("P".equals(userType) && groupCode != null) {
                  userType = "G";
                }
              } catch (Exception e) {
                //userType 조회 실패 -> 기본유저로
              }
              
              LocalDateTime startTime = Instant.ofEpochMilli(milli).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
              Duration duration = Duration.between(startTime, LocalDateTime.now());
              LogDoc logDoc = LogDoc.builder()
                .uJobCode(DocLogItem.DL.name())
                .uDocId(fileObjId)
                .uDocKey(doc.getUDocKey())
                .uDocName(doc.getTitle())
                .uDocVersion(docVersion)
                .uFileSize(Long.valueOf(doc.getRContentSize()))
                .uOwnDeptcode(doc.getUDeptCode())
                .uActDeptCode(userSession.getUser().getOrgId())
                .uJobUser(finalAuthentication != null ? finalAuthentication.loginId : null)
                .uJobUserType(userType) // 작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
  //              .uJobDate()
                .uDocStatus(doc.getUDocStatus())
                .uSecLevel(doc.getUSecLevel())
                .uCabinetCode(doc.getUCabinetCode())
                .uJobGubun("D") // 등록=[D:Dbox, P:PC], 수정=[W:덮어쓰기, V:버전생성], 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 보안등급 변경=[U:상향, D:하향], 복호화 반출=[S:
                .uUserIp(getClientIp(request))
                .uViewDownTime(duration.getSeconds() + "." + duration.getNano())
                .build();
              
              logDao.insertLog(logDoc);
            }
          } catch (Exception ee) {
            ee.printStackTrace();
          } finally {
            if (timeKey != null) redisRepository.delete(timeKey);
          }
        }
      } catch (Exception e2) {
        e2.printStackTrace();
        throw new RuntimeException(e2);
      }
		});

		// 파일 저장되는 경로
    upload.settingVo.setPhysicalPath(basePath);
    
    // 업로드 임시 경로
    upload.settingVo.setTempPath(tempPath);
    
    // 위에 설정된 임시파일 물리적 경로에 불필요한 파일을 삭제 처리하는 설정 (단위: 일)
    upload.settingVo.setGarbageCleanDay(garbageCleanDay);
    
    String result = upload.Process(request, response, servletContext, event);
		if (StringUtils.isNotBlank(result)) {
			return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(result);
		} else {
			return null;
		}
	}

  private int checkLockedUser(String userId, String token) {
    if (token != null) {
      String isBanned = redisRepository.get(Commons.LOGOUT_PREFIX + token, String.class);
      if (isBanned != null) return 1;
    }
    if (userId != null) {
      String isLocked = redisRepository.get(Commons.LOCK_PREFIX + userId, String.class);
      if (isLocked != null) return 2;
    }
    return 0;
  }
}
