package com.dongkuksystems.dbox.services.doc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.DfIdNotFoundException;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.AuthObjType;
import com.dongkuksystems.dbox.constants.AuthorType;
import com.dongkuksystems.dbox.constants.CodeType;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DocFlag;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.auth.CommonAuthDao;
import com.dongkuksystems.dbox.daos.type.auth.base.AuthBaseDao;
import com.dongkuksystems.dbox.daos.type.code.CodeDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.feedback.FeedbackDao;
import com.dongkuksystems.dbox.daos.type.folder.FolderDao;
import com.dongkuksystems.dbox.daos.type.manager.attachpolicy.AttachPolicyDao;
import com.dongkuksystems.dbox.daos.type.user.preset.UserPresetDao;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataViewCheckoutDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocApprovalDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackFilterDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.DocRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocImp;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.cache.CacheService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.CommonUtils;
import com.dongkuksystems.dbox.utils.dctm.AES256Util;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class DocServiceImpl extends AbstractCommonService implements DocService {

  @Value("${jwt.token.header}")
  private String tokenHeader;

  @Value("${kupload.base-path}")
  private String kuploadBasePath;

  @Value("${kupload.temp-path}")
  private String tempPath;
  
  private final AuthService 	authService;
  private final FolderService 	folderService;
  private final GwDeptService	gwDeptService;
  private final UserService 	userService;
  private final DocDao 			docDao;
  private final FeedbackDao 	feedbackDao;
  private final AuthBaseDao 	authBaseDao;
  private final CodeDao 		codeDao;
  private final UserPresetDao 	userPresetDao;
  private final AttachPolicyDao	attachPolicyDao;
  private final GwDeptDao 		gwDeptDao;
  private final CacheService 	cacheService;
  private final CodeService 	codeService;
  private final RedisRepository redisRepository;
  private final FolderDao   folderDao;
  private final JWT jwt;
  private final CommonAuthDao commonAuthDao;

  public DocServiceImpl(DocDao docDao, AuthService authService, FeedbackDao feedbackDao, AuthBaseDao authBaseDao, CacheService cacheService, CodeService codeService,
  		RedisRepository redisRepository, FolderService folderService, JWT jwt, CodeDao codeDao, UserPresetDao userPresetDao, AttachPolicyDao attachPolicyDao, FolderDao folderDao, GwDeptService gwDeptService, UserService userService, GwDeptDao gwDeptDao, CommonAuthDao commonAuthDao ) {
    
		this.authService 		= authService;
		this.gwDeptService 		= gwDeptService;
		this.userService 		= userService;
		this.docDao 			= docDao;
		this.folderService 		= folderService;
		this.feedbackDao 		= feedbackDao;
		this.authBaseDao 		= authBaseDao;
		this.codeDao 			= codeDao;
		this.userPresetDao 		= userPresetDao;
		this.attachPolicyDao 	= attachPolicyDao;
		this.gwDeptDao 			= gwDeptDao;
		this.cacheService 		= cacheService;
		this.codeService		= codeService;
		this.redisRepository 	= redisRepository;
		this.folderDao 			= folderDao;
		this.jwt 				= jwt;
		this.commonAuthDao      = commonAuthDao;
  }

  @Override
  public String createDoc(UserSession userSession, UploadDocDto dto, AttachedFile aFile, String docType,
      String secLevel, HamInfoResult hamInfo, IDfSession idfSession) throws Exception {
    String registObjId = null;
    String normalAclSuffix = null;
    FolderAuthDto folderAuthDto = null;
    IDfDocument idfNewDoc = null;
    IDfPersistentObject obj = null;
    long contentSize = 0;

    normalAclSuffix = MessageFormat
        .format(AclTemplate.DEFAULT.getValue(), hamInfo.getUCabinetCode(),
            CommonUtils.getAuthScope(hamInfo.getHamType()), DocFlag.GENERAL.getValue(), secLevel, docType)
        .toLowerCase();

    aFile.setDcmtContentType(cacheService.selectDmFormats());
    idfNewDoc = UploadDocDto.toIDfDocument(idfSession, dto, aFile);
    idfNewDoc.setACLDomain(idfSession.getDocbaseOwnerName());
    idfNewDoc.setACLName(normalAclSuffix);
    registObjId = idfNewDoc.getObjectId().toString();
    if (dto.checkRoot()) {
//      folderAuthDto = FolderAuthDto.builder()
//          .authBaseList(authService.selectDefaultFolderAuth(hamInfo, userSession.getUser())).build();
    } else {
      folderAuthDto = authService.selectFolderAuth(dto.getUpObjectId(), AuthorType.ALL.getValue());
    }

    // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
    if (folderAuthDto != null) {
      if (folderAuthDto.getAuthBaseList() != null) {
        for (AuthBase authBase : folderAuthDto.getAuthBaseList()) {
          if (!AuthorType.DEFAULT.getValue().equals(authBase.getUAuthorType())) {
            obj = authService.makeAuthBaseObj(idfSession, registObjId, authBase, userSession, true);
            obj.save();
            idfNewDoc.grant(authBase.getUAuthorId(), GrantedLevels.findByLevel(authBase.getUPermitType()), "");
          }
        }
      }
      if (folderAuthDto.getAuthShareList() != null) {
        for (AuthShare authShare : folderAuthDto.getAuthShareList()) {
          obj = authService.makeAuthShareObj(idfSession, registObjId, hamInfo.getUCabinetCode(), authShare, userSession,
              true);
          obj.save();
          idfNewDoc.grant(authShare.getUAuthorId(), GrantedLevels.findByLevel(authShare.getUPermitType()), "");
        }
      }
    }
    contentSize = idfNewDoc.getContentSize();
    idfNewDoc.save();
    return null;
  }

  @Override
  public String createDoc(UserSession userSession, UploadDocDto dto, AttachedFile aFile) throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    try {
      idfSession = this.getIdfSession(userSession);
      aFile.setDcmtContentType(cacheService.selectDmFormats());
      idfDoc = UploadDocDto.toIDfDocument(idfSession, dto, aFile);
      idfDoc.save();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
      aFile.deleteFile();
    }
    return idfDoc.getObjectId().getId();
  }

  @Override
  public String createDoc(String socialPerId, UploadDocDto dto, AttachedKUploadFile aFile) throws Exception {

    UserSession userSession = (UserSession) redisRepository.getObject(socialPerId, UserSession.class);
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    try {
      idfSession = this.getIdfSession(userSession);
      aFile.setDcmtContentType(cacheService.selectDmFormats());
      idfDoc = UploadDocDto.toIDfDocument(idfSession, dto, aFile);
      idfDoc.save();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
      aFile.deleteFile();
    }
    return idfDoc.getObjectId().getId();
  }

  @Override
  public IDfDocument createDoc(UserSession userSession, UploadDocDto dto, AttachedKUploadFile aFile) throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    try {
      idfSession = this.getIdfSession(userSession);
      aFile.setDcmtContentType(cacheService.selectDmFormats());
      idfDoc = UploadDocDto.toIDfDocument(idfSession, dto, aFile);
      idfDoc.save();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
      aFile.deleteFile();
    }
    return idfDoc;
  }
  
  @Override
  public IDfDocument createDoc(IDfSession idfSession, UploadDocDto dto, AttachedKUploadFile aFile) throws Exception {
    IDfDocument idfDoc = null;
    try {
      aFile.setDcmtContentType(cacheService.selectDmFormats());
      idfDoc = UploadDocDto.toIDfDocument(idfSession, dto, aFile);
    } catch (Exception e) {
      throw e;
    } 
    return idfDoc;
  }

  @Override
  public IDfDocument overWrite(IDfSession idfSession, String objectId, AttachedKUploadFile aFile) throws Exception {
    IDfDocument idfDoc = null;
    try {
      aFile.setDcmtContentType(cacheService.selectDmFormats());
      idfDoc = UploadDocDto.overWirteDocument(idfSession, objectId, aFile);
    } catch (Exception e) {
      throw e;
    } 
    return idfDoc;
  }
  
  @Override
  public CustomInputStreamResource downloadDoc(UserSession userSession, String objId) throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    InputStream inputStream = null;
    CustomInputStreamResource customInputStream = null;
    try {
      idfSession = this.getIdfSession(userSession);
      if (DfId.isObjectId(objId)) {
        idfDoc = (IDfDocument) idfSession.getObject(new DfId(objId));
        String fileName = idfDoc.getString("title");
        String secLevel = idfDoc.getString("u_sec_level");
        String docStatus = idfDoc.getString("u_doc_status");
        customInputStream = new CustomInputStreamResource(idfDoc.getContent(), idfDoc.getContentSize(), fileName, secLevel, docStatus);
      } else {
        throw new Exception("Object Id is not valid");
      }
    } catch (DfIdNotFoundException e) {
      throw new ForbiddenException("문서 권한 없음[Documentum]");
    } catch (Exception e) {
      throw e;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
      if (idfSession != null) {
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
    }
    return customInputStream;
  }
  
  @Override
  public CustomInputStreamResource downloadDoc(IDfSession idfSession, String objId) throws Exception {
  
	// 관리자 세션으로 받을시 시용.
    IDfDocument idfDoc = null;
    InputStream inputStream = null;
    CustomInputStreamResource customInputStream = null;
    try {
      if (DfId.isObjectId(objId)) {
        idfDoc = (IDfDocument) idfSession.getObject(new DfId(objId));
        String fileName = idfDoc.getString("title");
        String secLevel = idfDoc.getString("u_sec_level");
        String docStatus = idfDoc.getString("u_doc_status");
        inputStream = idfDoc.getContent();
        customInputStream = new CustomInputStreamResource(inputStream, idfDoc.getContentSize(), fileName, secLevel, docStatus);
      } else {
        throw new Exception("Object Id is not valid");
      }
    } catch (DfIdNotFoundException e) {
      throw new ForbiddenException("문서 권한 없음[Documentum]");
    } catch (Exception e) {
      throw e;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
    return customInputStream;
  }
  
  @Override
  public CustomInputStreamResource downloadDocByFile(IDfSession idfSession, String objId) throws Exception {
    
    // 관리자 세션으로 받을시 시용.
    IDfDocument idfDoc = null;
    CustomInputStreamResource customInputStream = null;
    File tmpFile = null;
    try {
      if (DfId.isObjectId(objId)) {
        idfDoc = (IDfDocument) idfSession.getObject(new DfId(objId));
        String fileName = idfDoc.getString("title");
        tmpFile = new File(idfDoc.getFile(tempPath + File.separator + UUID.randomUUID().toString() + ".tmp"));
        customInputStream = new CustomInputStreamResource(new FileInputStream(tmpFile), idfDoc.getContentSize(), fileName);
      } else {
        throw new Exception("Object Id is not valid");
      }
    } catch (DfIdNotFoundException e) {
      throw new ForbiddenException("문서 권한 없음[Documentum]");
    } catch (Exception e) {
      throw e;
    } finally {
      if (tmpFile != null) {
        tmpFile.delete();
      }
    }
    return customInputStream;
  }

  @Override
  public List<Doc> selectList(DocFilterDto dto) throws Exception {
    return docDao.selectList(dto);
  }

  @Override
  public List<DocDetailDto> selectAuthorizedDetailList(DocFilterDto dto, String userId, int level) throws Exception {
    // 보안등급 코드 조회
    Map<String, String> secLevelMap = codeService.getSecLevelMap();
    // 문서상태 코드 조회
    Map<String, String> docStatusMap = codeService.getDocStatusMap();

    final ModelMapper modelMapper = getModelMapper();

    // 문서 리스트
    List<Doc> docList = docDao.selectAuthorizedDetailList(dto, userId, level);

    // 문서 리스트 별 REPEATING 맵
    List<DocRepeating> docRepeatingList = docDao.selectAuthorizedRepeatingList(dto, userId, level);
    Map<String, List<DocRepeating>> docRepeatingListMap = docRepeatingList.stream()
        .collect(Collectors.groupingBy(item -> item.getRObjectId()));

    List<DocDetailDto> docDetailDtoList = docList.stream().map(item -> {
      boolean readable = item.getMaxLevel() > GrantedLevels.BROWSE.getLevel();

      // 작성자 리스트
      List<String> uEditorList = docRepeatingListMap.get(item.getRObjectId()).stream()
          .filter(item2 -> null != item2.getUEditor()).map(item2 -> item2.getUEditor()).collect(Collectors.toList());

      // 버전 리스트
      List<String> rVersionLabelList = docRepeatingListMap.get(item.getRObjectId()).stream()
          .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
          .sorted(Comparator.reverseOrder())
          .collect(Collectors.toList());

      // 연관 결재정보 리스트
      List<DocApprovalDto> docApprovalDtoList = docRepeatingListMap.get(item.getRObjectId()).stream()
          .filter(item2 -> null != item2.getUWfKey()).map(item2 -> {
            DocApprovalDto docApprovalDto = modelMapper.map(item2, DocApprovalDto.class);
            return docApprovalDto;
          }).collect(Collectors.toList());

      DocDetailDto docDto = modelMapper.map(item, DocDetailDto.class);
      docDto.setSecLevelName(secLevelMap.get(docDto.getUSecLevel()));
      docDto.setDocStatusName(docStatusMap.get(docDto.getUDocStatus()));
      docDto.setLastVersion(rVersionLabelList.get(rVersionLabelList.size() - 1)); // 최신 버전값
      docDto.setUEditor(uEditorList);
      docDto.setRVersionLabel(rVersionLabelList);
      docDto.setApprovals(docApprovalDtoList);
      docDto.setReadable(readable);

      return docDto;
    }).collect(Collectors.toList());

    return docDetailDtoList;
  }
  
  
  @Override
  public List<DocDetailDto> selectAuthorizedsearchList(DocFilterDto dto, String userId, int level,
  		String searchName, List<FolderDetailDto> folderDetailDtoList, String dataId, String dataCabinetCode) throws Exception {
	  
	  // 보안등급 코드 조회
	    Map<String, String> secLevelMap = codeService.getSecLevelMap();
	    // 문서상태 코드 조회
	    Map<String, String> docStatusMap = codeService.getDocStatusMap();

	    final ModelMapper modelMapper = getModelMapper();
	    // 문서 리스트
	    
	    List<Doc> docList = new ArrayList<>();
	    List<Doc> docList1 = new ArrayList<>();
	    List<DocRepeating> docRepeatingList = new ArrayList<>();
	    List<DocRepeating> docRepeatingList1 = new ArrayList<>();
	    List<DocDetailDto> docDetailDtoList = new ArrayList<>();
	    // 임원 체크
	    String imwoncheck = commonAuthDao.selectsearchImwonCheck(dataCabinetCode);
	    
	    if(dataId != null) {
	      
	      List<String> folderSearchList = folderDao.selectSearchList(dataId);
	      
	      for(int idx = 0; idx < folderSearchList.size(); idx++) {  
	        dataId = folderSearchList.get(idx);
	        docList1 = docDao.selectAuthorizedSearchList(dto, userId, level, dataId, searchName, dataCabinetCode, imwoncheck);
	        docRepeatingList1 =  docDao.selectAuthorizedRepeatingSearchList(dto, userId, level, searchName, dataId, dataCabinetCode, imwoncheck);
	        docList.addAll(docList1);
	        docRepeatingList.addAll(docRepeatingList1);
	      }
	    Map<String, List<DocRepeating>> docRepeatingListMap = docRepeatingList.stream()
		        .collect(Collectors.groupingBy(item -> item.getRObjectId()));

	    docDetailDtoList  = docList.stream().map(item -> {
	        boolean readable = item.getMaxLevel() > GrantedLevels.BROWSE.getLevel();

	        // 버전 리스트
	        List<String> rVersionLabelList = docRepeatingListMap.get(item.getRObjectId()).stream()
	            .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
	            .collect(Collectors.toList());

	        // 연관 결재정보 리스트
	        List<DocApprovalDto> docApprovalDtoList = docRepeatingListMap.get(item.getRObjectId()).stream()
	            .filter(item2 -> null != item2.getUWfKey()).map(item2 -> {
	              DocApprovalDto docApprovalDto = modelMapper.map(item2, DocApprovalDto.class);
	              return docApprovalDto;
	            }).collect(Collectors.toList());

	        // 피드백 개수
	        // TODO 상수시간 처리
	        FeedbackFilterDto feedbackFilterDto = FeedbackFilterDto.builder().uDocKey(item.getUDocKey()).uLevel(1).build();
	        int feedbackCount = feedbackDao.selectCount(feedbackFilterDto);

	        DocDetailDto docDto = modelMapper.map(item, DocDetailDto.class);
	        docDto.setSecLevelName(secLevelMap.get(docDto.getUSecLevel()));
	        docDto.setDocStatusName(docStatusMap.get(docDto.getUDocStatus()));
	        docDto.setLastVersion(rVersionLabelList.get(rVersionLabelList.size() - 1)); // 최신 버전값
	        docDto.setRVersionLabel(rVersionLabelList);
	        docDto.setApprovals(docApprovalDtoList);
	        docDto.setFeedbackCount(feedbackCount);
	        docDto.setReadable(readable);

	        return docDto;
	      }).collect(Collectors.toList());
	    
	    }else {
	       docList = docDao.selectAuthorizedSearchList(dto, userId, level, dataId, searchName, dataCabinetCode,imwoncheck);
	        
	        docRepeatingList =  docDao.selectAuthorizedRepeatingSearchList(dto, userId, level, searchName, dataId, dataCabinetCode, imwoncheck);

	        Map<String, List<DocRepeating>> docRepeatingListMap = docRepeatingList.stream()
	                .collect(Collectors.groupingBy(item -> item.getRObjectId()));

	        docDetailDtoList = docList.stream().map(item -> {
	            boolean readable = item.getMaxLevel() > GrantedLevels.BROWSE.getLevel();

	            // 버전 리스트
	            List<String> rVersionLabelList = docRepeatingListMap.get(item.getRObjectId()).stream()
	                .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
	                .collect(Collectors.toList());

	            // 연관 결재정보 리스트
	            List<DocApprovalDto> docApprovalDtoList = docRepeatingListMap.get(item.getRObjectId()).stream()
	                .filter(item2 -> null != item2.getUWfKey()).map(item2 -> {
	                  DocApprovalDto docApprovalDto = modelMapper.map(item2, DocApprovalDto.class);
	                  return docApprovalDto;
	                }).collect(Collectors.toList());

	            // 피드백 개수
	            // TODO 상수시간 처리
	            FeedbackFilterDto feedbackFilterDto = FeedbackFilterDto.builder().uDocKey(item.getUDocKey()).uLevel(1).build();
	            int feedbackCount = feedbackDao.selectCount(feedbackFilterDto);

	            DocDetailDto docDto = modelMapper.map(item, DocDetailDto.class);
	            docDto.setSecLevelName(secLevelMap.get(docDto.getUSecLevel()));
	            docDto.setDocStatusName(docStatusMap.get(docDto.getUDocStatus()));
	            docDto.setLastVersion(rVersionLabelList.get(rVersionLabelList.size() - 1)); // 최신 버전값
	            docDto.setRVersionLabel(rVersionLabelList);
	            docDto.setApprovals(docApprovalDtoList);
	            docDto.setFeedbackCount(feedbackCount);
	            docDto.setReadable(readable);

	            return docDto;
	          }).collect(Collectors.toList());
	    }
	    return docDetailDtoList;

  }
  

  @Override
  public Optional<Doc> selectOne(String objectId) throws Exception {
    return selectOne(objectId, false);
  }
  
  @Override
  public Optional<Doc> selectOne(String objectId, boolean isUDocKey) throws Exception {
    return docDao.selectOne(objectId, isUDocKey);
  }
  
  @Override
  public Optional<Doc> selectDetailOne(String objectId, String userId) throws Exception {
      return selectDetailOne(objectId, false, userId);
  }
  
  @Override
  public Optional<Doc> selectDetailOne(String objectId, boolean isUDocKey, String userId) throws Exception {
      Optional<Doc> optDoc = docDao.selectDetailOne(objectId, userId, isUDocKey);
      if (optDoc.isPresent()) {
        Doc doc = optDoc.get();
          
//        // 검색(2) 이상의 권한을 가졌는지 여부
//        boolean authorized = doc.getMaxLevel() >= GrantedLevels.BROWSE.getLevel();
//        
//        if (authorized) {
            // Repeating 타입 조회 후 설정
            List<DocRepeating> docRepeatingList = docDao.selectRepeatingOne(objectId, isUDocKey);
            if (isUDocKey) {
              List<DocRepeating> lastDocRepeatingList = docRepeatingList.stream()
                  .filter(item -> StringUtils.equals(doc.getRObjectId(), item.getRObjectId()))
                  .collect(Collectors.toList());
              doc.setDocRepeatings(lastDocRepeatingList);
            } else {
              doc.setDocRepeatings(docRepeatingList);
            }
 
	        // 문서에 해당하는 전체 권한 리스트 조회
	        List<AuthBase> authBaseList = authBaseDao.selectDetailList(doc.getUDocKey());
	        
	        // 백업된 권한 제외
//	        List<AuthBase> authBaseNotBackupList = authBaseList.stream()
//	                .filter(item -> !AuthObjType.BACKUP.getValue().equals(item.getUObjType()))
//	                .collect(Collectors.toList());
	        doc.setAuthBases(authBaseList);
            
          return optDoc;
//        }
//        else throw new ForbiddenException("문서에 대한 조회 권한이 없습니다.");
      } else {
          return optDoc;
      }
  }

  @Override
  public List<Doc> selectListForFolderId(String folderId) throws Exception {
    return docDao.selectListForFolderId(folderId);
  }

  @Override
  public List<Doc> selectAuthorizedListByFolIds(List<String> folIds, String userId, int level) {
    return docDao.selectAuthorizedListByFolIds(folIds, userId, level);
  }

  
  @Override
  public List<Doc> selectDeleteAuthorizedListByFolIds(List<String> folIds, String userId, int level) {
    return docDao.selectDeleteAuthorizedListByFolIds(folIds, userId, level);
  }
  
  
  @Override
  public List<Doc> selectListByFolIds(List<String> folIds) {
	return docDao.selectListByFolIds(folIds);
  }

@Override
  public boolean checkDocsDuple(DocFilterDto docFilterDto) {
    return docDao.selectDocDuple(docFilterDto, docFilterDto.getDocPaths());
  }

  @Override
  public boolean isLocked(String objectId) {
    return docDao.selectLockStatus(objectId);
  }

  @Override
  public String versionUp(String objectId, ByteArrayOutputStream fileStream, UserSession userSession, String objectName)
      throws Exception {
    IDfSession idfSession = getIdfSession(userSession);
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    IDfId newSysObjId = null;
    IDfVersionPolicy idfVerPolcy  = null;
    try {
      idfVerPolcy = idfDoc.getVersionPolicy();
      if (idfDoc.isCheckedOut()) {
        idfDoc.setObjectName(objectName);
        idfDoc.setContent(fileStream);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      } else {
        idfDoc.checkout();
        idfDoc.setObjectName(objectName);
        idfDoc.setContent(fileStream);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
    }
    return newSysObjId.getId();
  }

  @Override
  public String versionUp(String objectId, String filePath, UserSession userSession, String objectName)
      throws Exception {
    IDfSession idfSession = getIdfSession(userSession);
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    IDfId newSysObjId = null;
    IDfVersionPolicy idfVerPolcy  = null;
    try {
      idfVerPolcy = idfDoc.getVersionPolicy();
      if (idfDoc.isCheckedOut()) {
        idfDoc.setObjectName(objectName);
        idfDoc.setFile(filePath);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      } else {
        idfDoc.checkout();
        idfDoc.setObjectName(objectName);
        idfDoc.setFile(filePath);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      }
    } catch (Exception e) {
      throw e;
    }
    return newSysObjId.getId();
  }
  
  @Override
  public String versionUp(String objectId, ByteArrayOutputStream fileStream, IDfSession idfSession, String objectName)
      throws Exception {
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    IDfId newSysObjId = null;
    IDfVersionPolicy idfVerPolcy  = null;
    try {
      idfVerPolcy = idfDoc.getVersionPolicy();
      if (idfDoc.isCheckedOut()) {
        idfDoc.setObjectName(objectName);
        idfDoc.setContent(fileStream);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      } else {
        idfDoc.checkout();
        idfDoc.setObjectName(objectName);
        idfDoc.setContent(fileStream);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      }
    } catch (Exception e) {
      throw e;
    } finally {
    }
    return newSysObjId.getId();
  }

  @Override
  public String versionUp(String objectId, String filePath, IDfSession idfSession, String objectName) throws Exception {
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    IDfId newSysObjId = null;
    IDfVersionPolicy idfVerPolcy  = null;
    try {
      idfVerPolcy = idfDoc.getVersionPolicy();
      if (idfDoc.isCheckedOut()) {
        idfDoc.setObjectName(objectName);
        idfDoc.setFile(filePath);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      } else {
        idfDoc.checkout();
        idfDoc.setObjectName(objectName);
        idfDoc.setFile(filePath);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      }
    } catch (Exception e) {
      throw e;
    }
    return newSysObjId.getId();
  }

  @Override
  public String versionUp(IDfDocument idfDoc, ByteArrayOutputStream fileStream, String objectName) throws Exception {
    IDfId newSysObjId = null;
    IDfVersionPolicy idfVerPolcy  = null;
    try {
      idfVerPolcy = idfDoc.getVersionPolicy();
      if (idfDoc.isCheckedOut()) {
        idfDoc.setObjectName(objectName);
        idfDoc.setContent(fileStream);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      } else {
        idfDoc.checkout();
        idfDoc.setObjectName(objectName);
        idfDoc.setContent(fileStream);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      }
    } catch (Exception e) {
      throw e;
    }
    return newSysObjId.getId();
  }

  @Override
  public String versionUp(IDfDocument idfDoc, String filePath, String objectName) throws Exception {
    IDfId newSysObjId = null;
    IDfVersionPolicy idfVerPolcy  = null;
    // Checkin
    try {
      idfVerPolcy = idfDoc.getVersionPolicy();
      if (idfDoc.isCheckedOut()) {
        idfDoc.setObjectName(objectName);
        idfDoc.setFile(filePath);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      } else {
        idfDoc.checkout();
        idfDoc.setObjectName(objectName);
        idfDoc.setFile(filePath);
        newSysObjId = idfDoc.checkin(false, MessageFormat.format(DCTMConstants.VERSIONUP_TEMPLATE, idfVerPolcy.getNextMajorLabel()));
      }
    } catch (Exception e) {
      throw e;
    }
    return newSysObjId.getId();
  }

  @Override
  public void checkOut(IDfDocument idfDoc) throws Exception {
    if (!idfDoc.isCheckedOut()) {
      idfDoc.checkout();
    }
  }

  @Override
  public void checkOut(String objectId, IDfSession idfSession) throws Exception {
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    if (!idfDoc.isCheckedOut()) {
      idfDoc.checkout();
    }
  }

  @Override
  public String checkIn(String objectId, IDfSession idfSession) throws Exception {
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    IDfId newSysObjId = null;
    if (!idfDoc.isCheckedOut()) {
      newSysObjId = idfDoc.checkin(false, String.valueOf(idfDoc.getImplicitVersionLabel()));
      return newSysObjId.getId();
    } else {
      return objectId;
    }
  }

  @Override
  public String checkIn(IDfDocument idfDoc) throws Exception {
    IDfId newSysObjId = null;
    if (!idfDoc.isCheckedOut()) {
      newSysObjId = idfDoc.checkin(false, String.valueOf(idfDoc.getImplicitVersionLabel()));
      return newSysObjId.getId();
    } else {
      return idfDoc.getObjectId().getId();
    }
  }

  @Override
  public void checkOut(String objectId, UserSession userSession) throws Exception {
    IDfSession idfSession = getIdfSession(userSession);
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    try {
      if (!idfDoc.isCheckedOut()) {
        idfDoc.checkout();
      }
    } finally {
      if (idfSession != null) {
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getDUserId(), idfSession);
        }
      }
    }
  }

  @Override
  public String checkIn(String objectId, UserSession userSession) throws Exception {
    IDfSession idfSession = getIdfSession(userSession);
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    IDfId newSysObjId = null;
    try {
      if (!idfDoc.isCheckedOut()) {
        newSysObjId = idfDoc.checkin(false, String.valueOf(idfDoc.getImplicitVersionLabel()));
        return newSysObjId.getId();
      } else {
        return objectId;
      }
    } finally {
      if (idfSession != null) {
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getDUserId(), idfSession);
        }
      }
    }
  }
  
  /**
   * 보기전 문서 정보 확인.
   * 	- WEB 일반문서 보기
   * 	- HTML 문서 보기
   * 	- HTML 반출함 문서 보기
   * 
   * @param userSession		: dctm Session
   * @param objId			: R_OBJECT_ID
   * @param docKey			: U_DOC_KEY
   * @param sAction			: VIEW, EDIT
   * @param sOpenContent	: CURRENT, SELECTED  (최신ID or 선택된 ID )
   * @param approveId		: 반출함 문서 일경우   그외 ""
   * @param sMenu			: URL???
   * @return
   * @throws Exception
   */
  	@Override
  	public DataViewCheckoutDto getViewCheck(UserSession userSession, String objId, String docKey, String sOpenContent, String approveId, String sSysId) throws Exception {
    	
  		// ========================
  		// 	HTMl 문서 보기시
  		//		1. robjectid_docid_approveid_sysid  : 4개를 1개로 합쳐서 보내야함.
  		//				r_object_id, u_doc_key, approveid, syspath
		// ========================
  		
		// ==============================================
		// 편집 정책 조회 : Agent Websocket 호출전 정보확인.
		// ==============================================
		// 호출.
		// 		userSession 	:
		//		objId			: R_OBJECT_ID
  		//		docKey			: U_DOC_KEY
		// 		sOpenContent 	: CURRENT, SELECTED
		// 		sMenu 			: URL or ""
  		// 
		// Return
		// 		- flag 			: 1	: 덮어쓰기 2 : 버전갱신 9 : 중간저장 + 덮어쓰기 10: 중간저장 + 버전갱신
		// 		- return_code 	: 0 : 성공 -1 : 실패
		// 		- return_msg
		// ========================
	  
	  
	  	IDfSession 			idfSession 				= null;
	  	IDfSysObject 		idf_sObj 				= null;
	  	IDfSysObject 		idf_CurrentObj			= null;
	  	DataViewCheckoutDto	rtnDataViewCheckoutDto	= null;
	  	
	  	String 		sRtnFlag			= "4";		// 보기시 4 고정
	  	String 		sRtnVersion			= "";
		String		sRtnFileName		= "";
		String 		sRtnDocKey			= "";
		
		String		sRtnSelectedId		= objId;	// 선택된 ID	
		String 		sRtnCurrentId		= "";		// 최신 ID
		String 		sRtnCurrentFileName	= "";		// 최신 ID 파일명
		String 		sRtnCurrentVersion	= "";		// 최신 ID 버전
 
		boolean		bAdminSession		= false;
		String		sAutoPermitSystemYn		= "N";		// 현재 권한 없지만 문서 보기시 자동권한 추가 할 문서임.
		
		// HTML 보기시 SYSPATH 복호화 내용
		String 		sSysIdDocKeyId			= "";	// 복호화 후 안에 있는 U_DOC_KEY 다시 암호화 할때 이부분으로 해야함 안그러면 보기시 Agent Service 에서 자동으로 보기될
		String		sSysIdSystemKey			= "";
		String 		sSysIdAttachRObjectId	= "";
		boolean		bIsCurrentObjId			= true;
		
		try {
			
			//===========================================
			// Agent 에 문서 정보 전달
			// 	HTML 문서 보기 ( 반출함 여부 체크 )
			//===========================================
			if(StringUtils.isBlank(docKey))		{	docKey		= "";}
			if(StringUtils.isBlank(approveId))	{ 	approveId	= "";}
			if(StringUtils.isBlank(sSysId))		{	sSysId 		= "";}
			
			//=======================
			// 일반보기, HTML 보기 구분함
			//=======================
			if(StringUtils.isBlank(sSysId))
			{

				//--------------
				// 일반 보기
				//--------------
				
				idfSession 			= this.getIdfSession(userSession);
				idf_sObj 			= (IDfSysObject) idfSession.getObject(new DfId(objId));
				sRtnDocKey			= idf_sObj.getChronicleId().getId();
				sRtnFileName		= idf_sObj.getTitle().replaceAll("/", "-");										// 파일명
				sRtnVersion			= idf_sObj.getVersionLabels().getImplicitVersionLabel();	// 버전
				sRtnFlag			= "4";														// 보기 ( 4 )
				
				
				String sObjectName 	= idf_sObj.getObjectName().replaceAll("/", "-");
				String sOrgName		= userSession.getUser().getOrgNm().replaceAll("/", "-");
				String sVersion     = idf_sObj.getVersionLabel(0).substring(0, idf_sObj.getVersionLabel(0).indexOf("."));
				String sModifyDate	= ""; // idf_sObj.getString("u_update_date").substring(0, 10);
				
				// 수정일 없을경우 r_modify_date 로 변경 ( 등록후 값 없을경우.... )
				if(StringUtils.isNotBlank(idf_sObj.getString("u_update_date")) && !idf_sObj.getString("u_update_date").equals("nulldate"))
				{
					sModifyDate	= idf_sObj.getString("u_update_date").substring(0, 10);
				}
				else
				{
					sModifyDate	= idf_sObj.getString("r_modify_date").substring(0, 10);
				}
				
				// 최종 수정자
				String sUserName	= userSession.getUser().getDisplayName();
				if(StringUtils.isNotBlank(idf_sObj.getString("u_last_editor")))
				{
					VUser userData = userService.selectOneByUserId(idf_sObj.getString("u_last_editor")).orElse(new VUser());
					sUserName	= userData.getDisplayName();
				}
				
				// 문서 소유 부서의 팀
				if(StringUtils.isNotBlank(idf_sObj.getString("u_cabinet_code")))
				{
					
					VDept vDept = gwDeptDao.selectOneByCabinetCode(idf_sObj.getString("u_cabinet_code")).orElse(new VDept());
					sOrgName 	= StringUtils.stripToEmpty(vDept.getOrgNm()).replaceAll("/", "-");
				}
				
				
				String sFileFormat	= idf_sObj.getString("u_file_ext");
				
				sRtnFileName		= sObjectName + "_" + sOrgName + "_" + sVersion + "_" + sModifyDate + "_" + sUserName + "." + sFileFormat;
				sRtnVersion			= sVersion;
				
				// 최신 ID 조회.
				if(sOpenContent.equals("CURRENT"))
				{
					String sChronicleId	= idf_sObj.getChronicleId().toString();						// 최신 OBJECT_ID 로 확인.  ( 중간 버전 편집 처리 할수도 있기때문에 최신 ID 로 비교함 )			
					sRtnCurrentId		= DCTMUtils.getCurrentObjectID(idfSession, sChronicleId);
					
					idf_CurrentObj 		= (IDfSysObject) idfSession.getObject(new DfId(sRtnCurrentId));
					sRtnCurrentFileName = idf_CurrentObj.getTitle().replaceAll("/", "-");			
					sRtnCurrentVersion	= idf_CurrentObj.getVersionLabels().getImplicitVersionLabel();
					
					sObjectName 	= idf_CurrentObj.getObjectName().replaceAll("/", "-");
					sOrgName		= userSession.getUser().getOrgNm().replaceAll("/", "-");
					sVersion     	= idf_CurrentObj.getVersionLabel(0).substring(0, idf_CurrentObj.getVersionLabel(0).indexOf("."));
					
					// sModifyDate		= idf_CurrentObj.getString("u_update_date").substring(0, 10);
					if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_update_date")) && !idf_CurrentObj.getString("u_update_date").equals("nulldate"))
					{
						sModifyDate	= idf_CurrentObj.getString("u_update_date").substring(0, 10);
					}
					else
					{
						sModifyDate	= idf_CurrentObj.getString("r_modify_date").substring(0, 10);
					}
					
					sUserName		= userSession.getUser().getDisplayName();
					
					if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_last_editor")))
					{
						VUser userData = userService.selectOneByUserId(idf_CurrentObj.getString("u_last_editor")).orElse(new VUser());
						sUserName	= userData.getDisplayName();
					}
					
					// 문서 소유 부서의 팀
					if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_cabinet_code")))
					{
						
						VDept vDept = gwDeptDao.selectOneByCabinetCode(idf_CurrentObj.getString("u_cabinet_code")).orElse(new VDept());
						sOrgName 	= StringUtils.stripToEmpty(vDept.getOrgNm()).replaceAll("/", "-");
					}
					
					sFileFormat		= idf_CurrentObj.getString("u_file_ext");
					
					sRtnCurrentFileName		= sObjectName + "_" + sOrgName + "_" + sVersion + "_" + sModifyDate + "_" + sUserName + "." + sFileFormat;
					
				}
				
			}
			else
			{
				
				//------------------------------------------
				// HTML 보기
				//	1. r_object_id 문서 정보 조회
				//	2. 문서가 없을경우 dockey 로 문서 정보 조회
				//	3. 문서 권한 확인
				//	4. 문서 권한 없을경우 admin 세션 연결
				//	5. 문서의 최신 r_object_id 확인
				//------------------------------------------
				
				// 1. HTML 문서 보기 권한 있는지 여부 확인.
				// 2. 권한확인.
				
				// 사용자 정보
		    	String userId = userSession.getUser().getUserId();
				
				// 문서 확인 ( 선택된 문서가 없으면 dockey 정보로 확인 )
		    	Optional<Doc> optDoc = docDao.selectDetailOne(objId, userId, false);
				if (!optDoc.isPresent()) {
					
					// 권한 없을경우 내부적으로 Exception 던지고 있음...
					optDoc = docDao.selectDetailOne(docKey, userId, true);
				}
				
				// 문서 권한 확인
				if (optDoc.isPresent()) {
					
					Doc doc = optDoc.get();
					if (doc.getMaxLevel() < GrantedLevels.BROWSE.getLevel())
			        {
						bAdminSession = true;
			        }
				}
				
				// 세션 연결. ( 권한 없을경우 관리자 세션 )
				if(bAdminSession)
				{
					idfSession 			= DCTMUtils.getAdminSession();
				}
				else
				{
					idfSession 			= this.getIdfSession(userSession);
				}
				
				// 문서가 존재하면 최신 버전인지 확인
				// 문서가 없을경우 docid 를 이용하여 최신 문서 정보 조회
				if(optDoc.isPresent())
				{
					Doc doc = optDoc.get();
					
					idf_sObj 			= (IDfSysObject) idfSession.getObject(new DfId(doc.getRObjectId()));
					String sChronicleId	= idf_sObj.getChronicleId().toString();						// 최신 OBJECT_ID 로 확인.  ( 중간 버전 편집 처리 할수도 있기때문에 최신 ID 로 비교함 )			
					sRtnCurrentId		= DCTMUtils.getCurrentObjectID(idfSession, sChronicleId);

					// 최신이 아니면 새롭게 조회.
					if(!objId.equals(sRtnCurrentId))
					{
						bIsCurrentObjId = false;
						optDoc 			= docDao.selectDetailOne(sRtnCurrentId, userId, false);
					}
 
				}
				
				
		    	//==========================================================
				// 문서정보 확인 DB 로 확인
		    	//	HTML 문서 보기시 권한 없을때 자동권한 부여 해야 되는지 확인 필요함
		    	//==========================================================
				if (optDoc.isPresent()) {
			        
					Doc doc = optDoc.get();
			        String sDocKey = doc.getUDocKey();
			        // Agent 자리수 r_object_id 길이 암호화 해서 120 자리 이내로 하기 위해서 만약을 위해 sysId 대신 첨부 edms_attach_policy 의 r_object_id 를 사용함
			        
			        //=================================================
			        // 복호화 처리후 첨부 시스템 정보 확인
			        //=================================================
			        // BASE64 DECODE
					byte[] byteObjSyspath		= sSysId.getBytes("UTF-8");
					byte[] byteDescObjSyspath	= Base64.decodeBase64(byteObjSyspath);
					String sDecBase64			= new String(byteDescObjSyspath, "UTF-8");

					// AES256 복호화
					AES256Util aesUtil			= new AES256Util();
					String sDecSysPath			= aesUtil.decrypt(sDecBase64);
					
					// SYSID 복호화 정보.
					sSysIdDocKeyId				= sDecSysPath.substring(0, 16);
					sSysIdSystemKey 			= sDecSysPath.substring(16);

					// URL 복사시 첨부 시스템 dbox 로 사용
					// 첨부 시스템 없어도 dbox 로 고정함
					if(sSysIdSystemKey.equals("dbox")){
						sSysIdAttachRObjectId = "dbox";
					}
					else
					{
						// 첨부시스템 r_object_id
						Optional<AttachPolicy> opAttachPolicy = attachPolicyDao.selectOneBySystemKey(sSysIdSystemKey);
						if(opAttachPolicy.isPresent())
						{
							sSysIdAttachRObjectId = opAttachPolicy.get().getRObjectId();
						}
						else
						{
							sSysIdAttachRObjectId = "dbox";
						}
					}
					
			        // 문서 권한이 없으면서, 문서 속성에 자동권한 보기 일경우, HTML 보기 sysid 있을경우
			        if (doc.getMaxLevel() < GrantedLevels.READ.getLevel() && doc.isUAutoAuthMailFlag())
			        {
			        		
						// 1. 복호화
						// 2. EDMS_CODE 에 메일 자동권한 여부 확인
						// 3. 문서 속성에 자동권한 확인.
						// 4. HTML 문서는 최신 ID 보기.
						// 5. OBJ 없으면 DOCID 이용해서 최신 ID 보기
						if(sDocKey.equals(sSysIdDocKeyId))
						{
							
							sAutoPermitSystemYn = "Y";
							
							// 2021-12-30 :: 모든 HTML 은 자동권한 추가임
//							CodeFilterDto codeFilterDto = CodeFilterDto.builder()
//																.uCodeType(CodeType.ATTACH_MAIL_SYSTEM.getValue())
//																.uCodeVal1(sSysIdSystemKey)
//																.build();
//						    List<Code> codeList = codeDao.selectList(codeFilterDto);
//						    if (codeList != null && codeList.size() > 0)
//						    {
//						    	sAutoPermitSystemYn = "Y";
//						    }
						}						
			        }
				}
				
				// 현재 문서 or 최신 버전 ID 로 조회.
				if(bIsCurrentObjId)
				{
					idf_sObj 			= (IDfSysObject) idfSession.getObject(new DfId(objId));					
				}
				else
				{
					idf_sObj 			= (IDfSysObject) idfSession.getObject(new DfId(sRtnCurrentId));		
				}
				
				sRtnDocKey				= idf_sObj.getChronicleId().getId();
				sRtnFileName			= idf_sObj.getTitle().replaceAll("/", "-");										// 파일명
				sRtnVersion				= idf_sObj.getVersionLabels().getImplicitVersionLabel();	// 버전
				sRtnFlag				= "4";														// 보기 ( 4 )
				
				// 기본정책 ( 최신 보기 )
				sOpenContent 			= "CURRENT";
				
				// 최신 ID 조회.
				if(sOpenContent.equals("CURRENT"))
				{
					String sChronicleId	= idf_sObj.getChronicleId().toString();						// 최신 OBJECT_ID 로 확인.  ( 중간 버전 편집 처리 할수도 있기때문에 최신 ID 로 비교함 )			
					sRtnCurrentId		= DCTMUtils.getCurrentObjectID(idfSession, sChronicleId);
					
					idf_CurrentObj 		= (IDfSysObject) idfSession.getObject(new DfId(sRtnCurrentId));
					sRtnCurrentFileName = idf_CurrentObj.getTitle().replaceAll("/", "-");			
					sRtnCurrentVersion	= idf_CurrentObj.getVersionLabels().getImplicitVersionLabel();
					
					// 파일명 처리.
					String sObjectName 	= idf_CurrentObj.getObjectName().replaceAll("/", "-");
					String sOrgName		= userSession.getUser().getOrgNm().replaceAll("/", "-");
					String sVersion		= idf_CurrentObj.getVersionLabel(0).substring(0, idf_CurrentObj.getVersionLabel(0).indexOf("."));
					
					String sModifyDate	= "";
					if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_update_date")) && !idf_CurrentObj.getString("u_update_date").equals("nulldate"))
					{
						sModifyDate	= idf_CurrentObj.getString("u_update_date").substring(0, 10);
					}
					else
					{
						sModifyDate	= idf_CurrentObj.getString("r_modify_date").substring(0, 10);
					}
					
					String sUserName		= userSession.getUser().getDisplayName();
					String sFileFormat		= idf_CurrentObj.getString("u_file_ext");
					
					if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_last_editor")))
					{
						VUser userData = userService.selectOneByUserId(idf_CurrentObj.getString("u_last_editor")).orElse(new VUser());
						sUserName	= userData.getDisplayName();
					}
					
					// 문서 소유 부서의 팀
					if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_cabinet_code")))
					{
						
						VDept vDept = gwDeptDao.selectOneByCabinetCode(idf_CurrentObj.getString("u_cabinet_code")).orElse(new VDept());
						sOrgName 	= StringUtils.stripToEmpty(vDept.getOrgNm()).replaceAll("/", "-");
					}
					
					
					
					
					sRtnCurrentFileName		= sObjectName + "_" + sOrgName + "_" + sVersion + "_" + sModifyDate + "_" + sUserName + "." + sFileFormat;
					
					
				}
				
				// 수정중. systemKey 에서 첨부 r_objectId 로.
				if(StringUtils.isNotBlank(sSysId))
				{
					// sRtnSelectedId  =	sRtnSelectedId 	+ "_" + docKey + "_" + approveId + "_" + sSysId;	// 사용안함 CURRENT 만 사용.
					
					// 최신 OBJECTID 로 변경함.
					AES256Util aesUtil			= new AES256Util();
					String sEncSysPath			= aesUtil.encrypt(sSysIdDocKeyId + sSysIdAttachRObjectId);
					String sUrlEncodeSysPath 	= Base64.encodeBase64URLSafeString(sEncSysPath.getBytes());
					
					// 초긴 OBJECT_ID 는 approveId 에 입력함.
					sRtnCurrentId				=	sRtnCurrentId 	+ "_" + sUrlEncodeSysPath;
				}
				
				// 이전 SYSTEM_KEY 로 사용시
//				if(StringUtils.isNotBlank(docKey) || StringUtils.isNotBlank(approveId) || StringUtils.isNotBlank(sSysId))
//				{
//					sRtnSelectedId  =	sRtnSelectedId 	+ "_" + docKey + "_" + approveId + "_" + sSysId;	// 사용안함 CURRENT 만 사용.
//					
//					
//					// 최신 OBJECTID 로 변경함.
//					AES256Util aesUtil			= new AES256Util();
//					String sEncSysPath			= aesUtil.encrypt(sRtnCurrentId + sSysIdSystemKey);
//					String sUrlEncodeSysPath 	= Base64.encodeBase64URLSafeString(sEncSysPath.getBytes());
//					
//					
//					// 초긴 OBJECT_ID 는 approveId 에 입력함.
//					sRtnCurrentId	=	sRtnCurrentId 	+ "_" + docKey + "_" + approveId + "_" + sUrlEncodeSysPath;
//				}
			}
			
			// 리턴정보.
			rtnDataViewCheckoutDto	= DataViewCheckoutDto.builder()
									.rObjectId(objId)
									.objectName(sRtnFileName)
									.docKey(sRtnDocKey)
									.versionLabel(sRtnVersion)
									.currentObjectId(sRtnCurrentId)
									.currentObjectName(sRtnCurrentFileName)
									.currentVersionLabel(sRtnCurrentVersion)
									.selectedObjectId(sRtnSelectedId)
									.selectedObjectName(sRtnFileName)
									.selectedVersionLabel(sRtnVersion)
									.flag(sRtnFlag)
									.sAutoPermitSystemYn(sAutoPermitSystemYn)
									.build();
			
		} catch (Exception e) {
			throw e;
		} finally {
			
			if(bAdminSession)
			{
				if (idfSession != null && idfSession.isConnected()) {
					idfSession.disconnect();
				}

			}
			else
			{
				this.sessionRelease(userSession.getDUserId(), idfSession);
			}
			
		}

		return rtnDataViewCheckoutDto;
	}
  	
	/**
	 * 문서 편집전 문서정보확인
	 * 
	 * @param userSession  : dctm Session
	 * @param objId        : R_OBJECT_ID
	 * @param docKey       : U_DOC_KEY    :: 필요 없음.
	 * @param sOpenContent : CURRENT, SELECTED (최신ID or 선택된 ID )
	 * @param sMenu        : 편집시 필요 없음.
	 * @return
	 * @throws Exception
	 */
  	@Override
  	public DataViewCheckoutDto getCheckoutCheck(UserSession userSession, String objId, String docKey, String sOpenContent) throws Exception {
    
  		//==============================================
  		// 편집 정책 조회	: Agent Websocket 호출전 정보확인.
  		//==============================================
  		// 호출.
  		//		sAction			: VIEW, EDIT
  		//		sOpenContent	: CURRENT, SELECTED
  		//		sMenu			: URL or ""
  		// Return
  		//		- flag			: 1 : 덮어쓰기 2 : 버전갱신 9 : 중간저장 + 덮어쓰기 10: 중간저장 + 버전갱신
  		// 		- return_code	: 0 : 성공 -1 : 실패
  		//		- return_msg
  		//
  		//
  		// 1. 버전업 저장은  사용자 설정에서 확인 ( 테이블 조회 필요 )
  		//		편집 기본은 버전업
  		//		사용자 세팅에 의해서 같은 버전 할지 유무 판단 필요
  		// 2. 중간 저장은 부서 설정에 확인 ( 테이블 조회 필요 )
  		//		==> 2021-12-23 수정 : 사용자 기준에서 문서 기준으로 조회함
  		//		2. 중간저장 사용자 여부 확인 하는 부분 정리 필요함
  		//			2.1 회사 설정 : CONFIG_MID_SAVE_DEPT
  		//			2.2 부서 설정 : 사용되는 테이블 확인 필요함.
  		//			2.3 flag 값에 담아서 리턴 필요.
  		//==============================================
  		
		// 회사및, cabinetCode 활용 
		// GWDEPT : selectOrgIdByCabinetcode  로 orgid 조회 필요.
  		// CURRENT 기준으로 할지 SELECTED 기준으로 할지 정하자 :: SELECTED 는 별로 없어서 CURRENT 로 하자.....
		
	  	IDfSession 	idfSession 				= null;
	  	IDfSysObject idf_sObj 				= null;
	  	IDfSysObject idf_CurrentObj 		= null;
	  	DataViewCheckoutDto	rtnDataViewCheckoutDto	= null;
	  	
	  	String 		sRtnFlag			= "2";		// 1 : 덮어쓰기 2 : 버전갱신 9 : 중간저장 + 덮어쓰기 10: 중간저장 + 버전갱신
	  	String 		sRtnVersion			= "";		// 버전   ( Agent 파일명에서 사용 )
		String		sRtnFileName		= "";		// 파일명
		String 		sRtnDocKey			= "";

		String 		sRtnCurrentId		= "";		// 최신 ID
		String 		sRtnCurrentFileName	= "";		// 최신 ID
		String 		sRtnCurrentVersion	= "";		// 최신 ID
	
		String 		sDocCabinetCode		= "";		// 문서함 code
		String 		sDocStatus			= "";		// L : 진행, C : 완료   ( C 일경우 보기로만 OPEN 으로 열어줌 )
		boolean		bIsCheckout			= false;	// 편집중상태확인
		int			iPermit				= 0;
		
		
		String 		sDocFolderId		= "";
		boolean 	bIsReadOnly			= false;	// 잠금 폴더일경우 읽기 전용으로 Open ( 20211125 추가됨 )
		
		try {
			
			idfSession 			= this.getIdfSession(userSession);
			
			// 최신정보조회 하면서 objectId 가 없을경우 docKey 에서 조회함. [URL 보기에서 사용됨.]
			if(sOpenContent.equals("CURRENT") && StringUtils.isBlank(objId) && StringUtils.isNotBlank(docKey))
			{
				objId	= DCTMUtils.getCurrentObjectID(idfSession, docKey);
			}
			
			//==================
			// 선택된 문서 정보
			//==================
			idf_sObj 			= (IDfSysObject) idfSession.getObject(new DfId(objId));
			sRtnDocKey			= idf_sObj.getChronicleId().getId();
			sRtnFileName		= idf_sObj.getTitle().replaceAll("/", "-");					// .getObjectName(); // 파일명
			sRtnVersion			= idf_sObj.getVersionLabels().getImplicitVersionLabel();	// 버전
			
			//==================
			// 파일명칭 변경
			//==================
			String sObjectName 	= idf_sObj.getObjectName().replaceAll("/", "-");
			String sOrgName		= userSession.getUser().getOrgNm().replaceAll("/", "-");
			String sVersion     = idf_sObj.getVersionLabel(0).substring(0, idf_sObj.getVersionLabel(0).indexOf("."));
			
			// 수정일 없을경우 r_modify_date 로 변경 ( 등록후 값 없을경우.... )
			String sModifyDate	= ""; // idf_sObj.getString("u_update_date").substring(0, 10);
			if(StringUtils.isNotBlank(idf_sObj.getString("u_update_date")) && !idf_sObj.getString("u_update_date").equals("nulldate"))
			{
				sModifyDate	= idf_sObj.getString("u_update_date").substring(0, 10);
			}
			else
			{
				sModifyDate	= idf_sObj.getString("r_modify_date").substring(0, 10);
			}
			
			String sUserName	= userSession.getUser().getDisplayName();
			String sFileFormat	= idf_sObj.getString("u_file_ext");
			
			if(StringUtils.isNotBlank(idf_sObj.getString("u_last_editor")))
			{
				VUser userData = userService.selectOneByUserId(idf_sObj.getString("u_last_editor")).orElse(new VUser());
				sUserName	= userData.getDisplayName();
			}
			
			// 문서 소유 부서의 팀
			if(StringUtils.isNotBlank(idf_sObj.getString("u_cabinet_code")))
			{
				
				VDept vDept = gwDeptDao.selectOneByCabinetCode(idf_sObj.getString("u_cabinet_code")).orElse(new VDept());
				sOrgName 	= StringUtils.stripToEmpty(vDept.getOrgNm()).replaceAll("/", "-");
			}
			
			sRtnFileName		= sObjectName + "_" + sOrgName + "_" + sVersion + "_" + sModifyDate + "_" + sUserName + "." + sFileFormat;
			sRtnVersion			= sVersion;
			
			
			
			//==================
			// 최신 문서 정보 
			// 		중간버전 보기시 (편집중 상태는 최신 문서 를 확인함 )
			// 		OBJID 로 편집 호출시 최신 버전 정보 확인함.
			//==================
			sRtnCurrentId		= DCTMUtils.getCurrentObjectID(idfSession, objId);
			idf_CurrentObj 		= (IDfSysObject) idfSession.getObject(new DfId(sRtnCurrentId));
			
			sRtnCurrentFileName = idf_CurrentObj.getTitle().replaceAll("/", "-");		
			sRtnCurrentVersion	= idf_CurrentObj.getVersionLabels().getImplicitVersionLabel();
			sDocFolderId		= idf_CurrentObj.getString("u_fol_id");
			sDocStatus			= idf_CurrentObj.getString("u_doc_status");
			iPermit				= idf_CurrentObj.getPermit();
			bIsCheckout			= idf_CurrentObj.isCheckedOut();
			
			//==================
			// 최신문서 파일명칭 변경
			//==================
			sObjectName	= idf_CurrentObj.getObjectName().replaceAll("/", "-");
			sOrgName	= userSession.getUser().getOrgNm().replaceAll("/", "-");
			sVersion	= idf_CurrentObj.getVersionLabel(0).substring(0, idf_CurrentObj.getVersionLabel(0).indexOf("."));
			
			// 수정일 없을경우 r_modify_date 로 변경 ( 등록후 값 없을경우.... )
			if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_update_date")) && !idf_CurrentObj.getString("u_update_date").equals("nulldate"))
			{
				sModifyDate	= idf_CurrentObj.getString("u_update_date").substring(0, 10);
			}
			else
			{
				sModifyDate	= idf_CurrentObj.getString("r_modify_date").substring(0, 10);
			}
			sUserName	= userSession.getUser().getDisplayName();
			sFileFormat	= idf_CurrentObj.getString("u_file_ext");
			
			if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_last_editor")))
			{
				VUser userData = userService.selectOneByUserId(idf_CurrentObj.getString("u_last_editor")).orElse(new VUser());
				sUserName	= userData.getDisplayName();
			}
			
			// 문서 소유 부서의 팀
			if(StringUtils.isNotBlank(idf_CurrentObj.getString("u_cabinet_code")))
			{
				
				VDept vDept = gwDeptDao.selectOneByCabinetCode(idf_CurrentObj.getString("u_cabinet_code")).orElse(new VDept());
				sOrgName 	= StringUtils.stripToEmpty(vDept.getOrgNm()).replaceAll("/", "-");
			}
			
			sRtnCurrentFileName	= sObjectName + "_" + sOrgName + "_" + sVersion + "_" + sModifyDate + "_" + sUserName + "." + sFileFormat;
			sRtnCurrentVersion	= sVersion;
			
			//===========================
			// 권한체크
			//===========================
			if(iPermit < 3)
			{
				throw new ForbiddenException("문서 권한 없음[Documentum]");				
			}
			
			//===========================
			// 문서상태 체크 ( 보기, 편집 ) 구별
			//===========================
			// 쓰기 잠금 폴더 여부 확인
			Optional<Folder> optFolder = folderService.selectOne(sDocFolderId);
			if(optFolder.isPresent()) {
				if( optFolder.get().getUFolStatus().equals("C") ) {//잠금처리된 폴더
					bIsReadOnly = true;
				}
			}
			
			// 완료문서일경우도 읽기전용으로 OPEN
			// URL 문서 보기시 호출 대비 해서 ( 편집중도 보기, 6권한보다 작으면 보기 )
			if(sDocStatus.equals("C") || bIsCheckout || iPermit < 6) {
				bIsReadOnly = true;
			}
				
			if(bIsReadOnly)
			{
				sRtnFlag	= "4";
			}
			else
			{
				
				// 편집 정책 조회 ( 1 : 덮어쓰기 2 : 버전갱신 9 : 중간저장 + 덮어쓰기 10: 중간저장 + 버전갱신 )
				int iOverWriteOrNew		= 2;	// DEFAULT
				int iMiddleSave 		= 0;	// DEFAULT
				
				// 버전업 or 덮어쓰기 구별
				Optional<UserPreset> userPresetPolicy = userPresetDao.selectOneByUserId(userSession.getDUserId());
				if(userPresetPolicy.isPresent())
				{
					String sUserPresetFlag = userPresetPolicy.get().getUEditSaveFlag();				
					if(sUserPresetFlag.equals("O"))
					{
						iOverWriteOrNew = 1;
					}
				}
				
				
				// 문서함 정보
				sDocCabinetCode			= idf_CurrentObj.getString("u_cabinet_code");
//				String sDocComOrgId		= gwDeptService.selectComCodeByCabinetCode(sDocCabinetCode);
//				String sDocDeptOrgId 	= gwDeptService.selectOrgIdByCabinetcode( sDocCabinetCode );
				
				// 편집중 중간 저장 여부 구별  .getDeptCabinetcode() 에서 수정함.
				CodeFilterDto codeFilterDto = CodeFilterDto.builder()
						.uCodeType(CodeType.CONFIG_MID_SAVE_DEPT.getValue())
            .uCodeVal1(userSession.getUser().getComOrgId())    // userSession.getUser().getComOrgId()
            .uCodeVal2(userSession.getUser().getOrgId())   // userSession.getUser().getOrgId()
//            .uCodeVal1(sDocComOrgId)    // userSession.getUser().getComOrgId()
//            .uCodeVal2(sDocDeptOrgId)   // userSession.getUser().getOrgId()
						.build();
				List<Code> codeList = codeDao.selectList(codeFilterDto);
				if (codeList != null && codeList.size() > 0)
				{
					iMiddleSave = 8;
				}
				
				int iSaveOption		= iOverWriteOrNew + iMiddleSave;
				sRtnFlag 			= Integer.toString(iSaveOption);
			}
			
			// 최신 ID 조회.
			// 문서 정보 확인위해 위에서 조회먼저됨.
//			if(sOpenContent.equals("CURRENT"))
//			{
//				// String sChronicleId	= idf_sObj.getChronicleId().toString();						// 최신 OBJECT_ID 로 확인.  ( 중간 버전 편집 처리 할수도 있기때문에 최신 ID 로 비교함 )			
//				// sRtnCurrentId		= DCTMUtils.getCurrentObjectID(idfSession, sChronicleId);
//				
//				idf_CurrentObj 		= (IDfSysObject) idfSession.getObject(new DfId(sRtnCurrentId));
//				sRtnCurrentFileName = idf_CurrentObj.getTitle();			
//				sRtnCurrentVersion	= idf_CurrentObj.getVersionLabels().getImplicitVersionLabel();
//			}
			
			// 리턴정보.
			rtnDataViewCheckoutDto	= DataViewCheckoutDto.builder()
									.rObjectId(objId)
									.objectName(sRtnFileName)
									.docKey(sRtnDocKey)
									.versionLabel(sRtnVersion)
									.currentObjectId(sRtnCurrentId)
									.currentObjectName(sRtnCurrentFileName)
									.currentVersionLabel(sRtnCurrentVersion)
									.selectedObjectId(objId)
									.selectedObjectName(sRtnFileName)
									.selectedVersionLabel(sRtnVersion)
									.flag(sRtnFlag)
									.build();
			
		} catch (Exception e) {
			throw e;
		} finally {
			this.sessionRelease(userSession.getDUserId(), idfSession);
		}

		return rtnDataViewCheckoutDto;
	}

  @Override
  public List<String> selectDocChk(String value) throws Exception  {
    
    return docDao.selectDocChk(value);

  }

  @Override
  public List<DataCreatorDto> selectDocCreators(String dataId) {
    return docDao.selectDocCreators(dataId);
  }

  @Override
  public List<DocVersionListDto> selectDocVersionList(String docKey) throws Exception {
    return docDao.selectDocVersionList(docKey);
  }

  @Override
  public List<DocVersionListDto> selectDocImpVersionList(String docImpKey) throws Exception {
    // TODO Auto-generated method stub
    return docDao.selectDocImpVersionList(docImpKey);
  }

@Override
public List<DocLinkListDto> selectDocLinkList(String dataId) throws Exception {
	// TODO Auto-generated method stub
	return docDao.selectDocLinkList(dataId);
}

@Override
public List<DocDetailDto> selectImpAuthorizedsearchList(DocFilterDto dto, String userId, int level,
		String searchName, List<FolderDetailDto> folderDetailDtoList, String dataId, String dataCabinetCode) throws Exception {
	
	// 보안등급 코드 조회
    Map<String, String> secLevelMap = codeService.getSecLevelMap();
    // 문서상태 코드 조회
    Map<String, String> docStatusMap = codeService.getDocStatusMap();

    final ModelMapper modelMapper = getModelMapper();
    
    List<Doc> docList = new ArrayList<>();
    List<Doc> docList1 = new ArrayList<>();
    
    List<DocRepeating> docRepeatingList = new ArrayList<>();
    List<DocRepeating> docRepeatingList1 = new ArrayList<>();
    
    List<DocDetailDto> docDetailDtoList = new ArrayList<>();
    
    if(dataId != null) {
      List<String>   folderSearchList = folderDao.selectSearchList(dataId);
      
      for(int idx = 0; idx < folderSearchList.size(); idx++) {
        
        dataId = folderSearchList.get(idx);
    // 문서 리스트
    docList1 = docDao.selectImpAuthorizedSearchList(dto, userId, level, dataId, searchName, dataCabinetCode); 
    docRepeatingList1 = docDao.selectDocImpAuthorizedRepeatingSearchList(dto, userId, level, searchName, dataId,dataCabinetCode);
    docList.addAll(docList1);
    docRepeatingList.addAll(docRepeatingList1);
      }
    Map<String, List<DocRepeating>> docRepeatingListMap = docRepeatingList.stream()
	        .collect(Collectors.groupingBy(item -> item.getRObjectId()));
    
    
    docDetailDtoList = docList.stream().map(item -> {
        boolean readable = item.getMaxLevel() > GrantedLevels.BROWSE.getLevel();

        // 작성자 리스트
        List<String> uEditorList = docRepeatingListMap.get(item.getRObjectId()).stream()
            .filter(item2 -> null != item2.getUEditor()).map(item2 -> item2.getUEditor()).collect(Collectors.toList());

        // 버전 리스트
        List<String> rVersionLabelList = docRepeatingListMap.get(item.getRObjectId()).stream()
            .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
            .collect(Collectors.toList());

        // 연관 결재정보 리스트
        List<DocApprovalDto> docApprovalDtoList = docRepeatingListMap.get(item.getRObjectId()).stream()
            .filter(item2 -> null != item2.getUWfKey()).map(item2 -> {
              DocApprovalDto docApprovalDto = modelMapper.map(item2, DocApprovalDto.class);
              return docApprovalDto;
            }).collect(Collectors.toList());

        // 피드백 개수
        // TODO 상수시간 처리
        FeedbackFilterDto feedbackFilterDto = FeedbackFilterDto.builder().uDocKey(item.getUDocKey()).uLevel(1).build();
        int feedbackCount = feedbackDao.selectCount(feedbackFilterDto);

        DocDetailDto docDto = modelMapper.map(item, DocDetailDto.class);
        docDto.setSecLevelName(secLevelMap.get(docDto.getUSecLevel()));
        docDto.setDocStatusName(docStatusMap.get(docDto.getUDocStatus()));
        docDto.setLastVersion(rVersionLabelList.get(rVersionLabelList.size() - 1)); // 최신 버전값
        docDto.setUEditor(uEditorList);
        docDto.setRVersionLabel(rVersionLabelList);
        docDto.setApprovals(docApprovalDtoList);
        docDto.setFeedbackCount(feedbackCount);
        docDto.setReadable(readable);

        return docDto;
      }).collect(Collectors.toList());
    }else {
      
      // 문서 리스트
      docList = docDao.selectImpAuthorizedSearchList(dto, userId, level, dataId, searchName, dataCabinetCode); 
      docRepeatingList = docDao.selectDocImpAuthorizedRepeatingSearchList(dto, userId, level, searchName, dataId,dataCabinetCode);

      Map<String, List<DocRepeating>> docRepeatingListMap = docRepeatingList.stream()
              .collect(Collectors.groupingBy(item -> item.getRObjectId()));
      
      
      docDetailDtoList = docList.stream().map(item -> {
          boolean readable = item.getMaxLevel() > GrantedLevels.BROWSE.getLevel();

          // 작성자 리스트
          List<String> uEditorList = docRepeatingListMap.get(item.getRObjectId()).stream()
              .filter(item2 -> null != item2.getUEditor()).map(item2 -> item2.getUEditor()).collect(Collectors.toList());

          // 버전 리스트
          List<String> rVersionLabelList = docRepeatingListMap.get(item.getRObjectId()).stream()
              .filter(item2 -> null != item2.getRVersionLabel()).map(item2 -> item2.getRVersionLabel())
              .collect(Collectors.toList());

          // 연관 결재정보 리스트
          List<DocApprovalDto> docApprovalDtoList = docRepeatingListMap.get(item.getRObjectId()).stream()
              .filter(item2 -> null != item2.getUWfKey()).map(item2 -> {
                DocApprovalDto docApprovalDto = modelMapper.map(item2, DocApprovalDto.class);
                return docApprovalDto;
              }).collect(Collectors.toList());

          // 피드백 개수
          // TODO 상수시간 처리
          FeedbackFilterDto feedbackFilterDto = FeedbackFilterDto.builder().uDocKey(item.getUDocKey()).uLevel(1).build();
          int feedbackCount = feedbackDao.selectCount(feedbackFilterDto);

          DocDetailDto docDto = modelMapper.map(item, DocDetailDto.class);
          docDto.setSecLevelName(secLevelMap.get(docDto.getUSecLevel()));
          docDto.setDocStatusName(docStatusMap.get(docDto.getUDocStatus()));
          docDto.setLastVersion(rVersionLabelList.get(rVersionLabelList.size() - 1)); // 최신 버전값
          docDto.setUEditor(uEditorList);
          docDto.setRVersionLabel(rVersionLabelList);
          docDto.setApprovals(docApprovalDtoList);
          docDto.setFeedbackCount(feedbackCount);
          docDto.setReadable(readable);

          return docDto;
        }).collect(Collectors.toList());
    }
    return docDetailDtoList;
}




}
