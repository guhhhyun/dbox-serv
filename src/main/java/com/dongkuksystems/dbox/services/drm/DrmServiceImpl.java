package com.dongkuksystems.dbox.services.drm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.constants.DrmCompanyId;
import com.dongkuksystems.dbox.constants.DrmResultType;
import com.dongkuksystems.dbox.constants.DrmSecLevelType;
import com.dongkuksystems.dbox.models.dto.etc.DrmCompanyDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmDeptDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;
import com.dongkuksystems.dbox.utils.DboxStringUtils;

import MarkAny.MaSaferJava.MaFileChk;
import MarkAny.MaSaferJava.Madec;
import MarkAny.MaSaferJava.Madn;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Primary
@Service
public class DrmServiceImpl implements DrmService {
	@Value("${drm.info}")
	private String drmInfo;
	@Value("${drm.dir}")
	private String drmDir;

	@Override
	public File encrypt(InputStream inputStream, DrmSecLevelType secLevelType, DrmCompanyDto company,
			List<DrmDeptDto> authDepts, List<DrmUserDto> authUsers, String docId, String docKey, String fileName, long fileSize, String loginId,
			String userName, String deptCode, String deptName, String entCode, String entName, boolean disableSaveEdit, String ip) throws Exception {
		ClassPathResource resource = new ClassPathResource(drmInfo);
		String path = Paths.get(resource.getURI()).toString();

		Madn madn = new Madn(path);

		File encrypted = new File(drmDir + File.separator + UUID.randomUUID());
		FileUtils.forceMkdirParent(encrypted);

		try (BufferedInputStream decryptedInputStream = new BufferedInputStream(inputStream);
				BufferedOutputStream encryptedOutputStream = new BufferedOutputStream(new FileOutputStream(encrypted));) {
			int piAclFlag = 0; // ACL 참조 방식(0 고정)
			String pstrDocLevel = "0"; // 암호화 문서 등급(0 고정)
			String pstrUserId = loginId; // 사용자 ID
			String pstrFileName = DboxStringUtils.replaceSymbols(fileName); // 파일 이름
			long plFileSize = fileSize; // 암호화하려는 원본 파일 크기
			String pstrOwnerId = loginId; // 암호화 대상 파일 소유자
			String pstrCompanyId = "DONGKUK"; // 회사코드 ID
			String pstrGroupId = ""; // 그룹코드 ID
			String pstrPositionId = ""; // 직위코드 ID
			String pstrGrade = ""; // 등급
			String pstrFileId = "DBOX;" + docId + ";" + docKey; // 파일 고유 ID
			int piCanSave = 1; // 저장 권한 (가능 1, 불가 0)
			int piCanEdit = 1; // 수정 권한 (가능 1, 불가 0)
			int piBlockCopy = 1; // 블록복사 권한 (가능 1, 불가 0)
			int piOpenCount = -99; // 열람 가능 회수 (회수 또는 제한 없음 -99)
			int piPrintCount = -99; // 출력 가능 회수 (회수 또는 제한 없음 -99)
			int piValidPeriod = -99; // 문서 사용 가능 기간(기간 또는 제한 없음 -99)
			int piSaveLog = 1; // 저장 로그 (가능 1, 불가 0)
			int piPrintLog = 1; // 출력 로그 (가능 1, 불가 0)
			int piOpenLog = 1; // 열람 로그 (가능 1, 불가 0)
			int piVisualPrint = 1; // 인쇄 시 워터마크 적용(적용1, 미적용 0)
			int piImageSafer = 0; // 캡쳐 방지 적용(적용1, 미적용 0)
			int piRealTimeAcl = 0; // 사용하지 않음(0 고정)
			String pstrDocumentTitle = ""; // 문서 제목
			String pstrCompanyName = "동국제강"; // 회사명
			String pstrGroupName = ""; // 그룹명
			String pstrPositionName = ""; // 직위명
			String pstrUserName = userName; // 사용자명
			String pstrUserIp = ip; // 사용자PC IP
			String pstrServerOrigin = "DBOX"; // 시스템명
			int piExchangePolicy = secLevelType.getValue(); // 암호화 문서 정책
			int piDrmFlag = 0; // 암호화 여부(0 고정)
			int iBlockSize = 0; // 블럭 크기 (0 고정)
			String strMachineKey = ""; // 머신키
			String strFileVersion = ""; // 암호화 파일 버전
			String strMultiUserID = ""; // 다중 사용자 ID
			String strMultiUserName = ""; // 다중 사용명
			String strEnterpriseID = "DONGKUK"; // 회사 대표 ID
			String strEnterpriseName = "동국제강그룹"; // 회사 대표명
			String strDeptID = deptCode; // 부서코드 ID
			String strDeptName = deptName; // 부서명
			String strPositionLevel = ""; // 직위레벨
			String strSecurityLevel = ""; // 보안레벨
			String strSecurityLevelName = ""; // 보안레벨명
			String strPgCode = ""; // 사용하지 않음
			String strCipherBlockSize = "16"; // 사이퍼 블럭크기 (16 고정)
			String strCreatorID = loginId; // 생성자 ID
			String strCreatorName = userName; // 생성자 이름
			String strOnlineContorl = "0"; // 0 고정
			String strOfflinePolicy = ""; // 고정
			String strValidPeriodType = ""; // 고정
			String strUsableAlways = "0"; // 0 고정
			String strPriPubKey = ""; // 고정
			String strCreatorCompanyId = ""; // 생성자 회사코드 ID
			String strCreatorDeptId = ""; // 생성자 부서코드 ID
			String strCreatorGroupId = ""; // 생성자 그룹코드 ID
			String strCreatorPositionId = ""; // 생성자 직위코드 ID
			String strFileSize = Long.toString(fileSize); // 원본파일 크기
			String strHeaderUpdateTime = ""; // 헤더 업데이트 시간
			String strReserved01 = ""; // 지정필드1
			String strReserved02 = ""; // 지정필드2
			String strReserved03 = ""; // 지정필드3
			String strReserved04 = ""; // 지정필드4
			String strReserved05 = ""; // 지정필드5

      pstrCompanyId = company.getCompanyId(); // 회사코드 ID
      pstrCompanyName = company.getCompanyName(); // 회사명
			if (DrmSecLevelType.LIVE == secLevelType) {
        pstrCompanyId = "DONGKUK";
        pstrCompanyName = "동국제강그룹"; // 회사 대표명
        strMultiUserID = authUsers.stream().map((item) -> item.getUserId()).collect(Collectors.joining(";"));
        strMultiUserName = authUsers.stream().map((item) -> item.getDisplayName()).collect(Collectors.joining(";"));
        pstrGroupId = authDepts.stream().map((item) -> item.getOrgId()).collect(Collectors.joining("|"));
        pstrGroupName = authDepts.stream().map((item) -> item.getOrgNm()).collect(Collectors.joining("|"));
      }  else if (DrmSecLevelType.LIVE_CLOSED == secLevelType) {
        pstrCompanyId = "DONGKUK";
        pstrCompanyName = "동국제강그룹"; // 회사 대표명
        strMultiUserID = authUsers.stream().map((item) -> item.getUserId()).collect(Collectors.joining(";"));
        strMultiUserName = authUsers.stream().map((item) -> item.getDisplayName()).collect(Collectors.joining(";"));
        pstrGroupId = authDepts.stream().map((item) -> item.getOrgId()).collect(Collectors.joining("|"));
        pstrGroupName = authDepts.stream().map((item) -> item.getOrgNm()).collect(Collectors.joining("|"));
        
        piImageSafer = 1; //캡처 방지 기능 On
        piPrintCount = 0; //인쇄가능 횟수 = 0번
      } else if (DrmSecLevelType.GROUP == secLevelType) {
         
        
      } else if (DrmSecLevelType.COMPANY == secLevelType) {
        //멀티개인한
        piExchangePolicy = DrmSecLevelType.INDIVIDUAL.getValue(); 
        pstrCompanyId = "DONGKUK";
        pstrCompanyName = "동국제강그룹"; // 회사 대표명
				
        strMultiUserID = authUsers.stream().map((item) -> item.getUserId()).collect(Collectors.joining(";"));
        strMultiUserName = authUsers.stream().map((item) -> item.getDisplayName()).collect(Collectors.joining(";"));
        pstrGroupId = authDepts.stream().map((item) -> item.getOrgId()).collect(Collectors.joining("|"));
        pstrGroupName = authDepts.stream().map((item) -> item.getOrgNm()).collect(Collectors.joining("|"));
			} else if (DrmSecLevelType.TEAM == secLevelType) {
			  //멀티개인한
			  piExchangePolicy = DrmSecLevelType.INDIVIDUAL.getValue(); 
        pstrCompanyId = "DONGKUK";
        pstrCompanyName = "동국제강그룹"; // 회사 대표명
        strMultiUserID = authUsers.stream().map((item) -> item.getUserId()).collect(Collectors.joining(";"));
        strMultiUserName = authUsers.stream().map((item) -> item.getDisplayName()).collect(Collectors.joining(";"));
        pstrGroupId = authDepts.stream().map((item) -> item.getOrgId()).collect(Collectors.joining("|"));
        pstrGroupName = authDepts.stream().map((item) -> item.getOrgNm()).collect(Collectors.joining("|"));
      }  else if (DrmSecLevelType.INDIVIDUAL == secLevelType) {
        pstrCompanyId = "DONGKUK";
        pstrCompanyName = "동국제강그룹"; // 회사 대표명
        strMultiUserID = authUsers.stream().map((item) -> item.getUserId()).collect(Collectors.joining(";"));
        strMultiUserName = authUsers.stream().map((item) -> item.getDisplayName()).collect(Collectors.joining(";"));
        pstrGroupId = authDepts.stream().map((item) -> item.getOrgId()).collect(Collectors.joining("|"));
        pstrGroupName = authDepts.stream().map((item) -> item.getOrgNm()).collect(Collectors.joining("|"));
        
        piImageSafer = 1; //캡처 방지 기능 On
        piPrintCount = 0; //인쇄가능 횟수 = 0번
      }
			
			// 수정불가여부 적용
			if (disableSaveEdit) {
			  piCanSave = 0; // 저장 불가
			  piCanEdit = 0; // 수정 불가
			}

			long encryptedFileLength = madn.lGetEncryptFileSize(piAclFlag, pstrDocLevel, pstrUserId, pstrFileName, plFileSize,
					pstrOwnerId, pstrCompanyId, pstrGroupId, pstrPositionId, pstrGrade, pstrFileId, piCanSave, piCanEdit,
					piBlockCopy, piOpenCount, piPrintCount, piValidPeriod, piSaveLog, piPrintLog, piOpenLog, piVisualPrint,
					piImageSafer, piRealTimeAcl, pstrDocumentTitle, pstrCompanyName, pstrGroupName, pstrPositionName,
					pstrUserName, pstrUserIp, pstrServerOrigin, piExchangePolicy, piDrmFlag, iBlockSize, strMachineKey,
					strFileVersion, strMultiUserID, strMultiUserName, strEnterpriseID, strEnterpriseName, strDeptID, strDeptName,
					strPositionLevel, strSecurityLevel, strSecurityLevelName, strPgCode, strCipherBlockSize, strCreatorID,
					strCreatorName, strOnlineContorl, strOfflinePolicy, strValidPeriodType, strUsableAlways, strPriPubKey,
					strCreatorCompanyId, strCreatorDeptId, strCreatorGroupId, strCreatorPositionId, strFileSize,
					strHeaderUpdateTime, strReserved01, strReserved02, strReserved03, strReserved04, strReserved05,
					decryptedInputStream);

			log.debug("Size of encrypted file : " + encryptedFileLength);

			if (encryptedFileLength > 0) {
				String resultCode = madn.strMadn(encryptedOutputStream);
				log.debug("Encryption result code : " + resultCode);
				log.debug("Size of encrypted file : " + encryptedFileLength);

				return encrypted;
			} else {
				String errorCode = madn.strGetErrorCode();
				String errorMessage = madn.strGetErrorMessage(errorCode);
				log.error("Decryption error code : " + errorCode);
				log.error("Decryption error message : " + errorMessage);
				throw new RuntimeException(errorCode + " " + errorMessage);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public File decrypt(InputStream inputStream, String fileName, long fileSize) throws Exception {
		ClassPathResource resource = new ClassPathResource(drmInfo);
		String path = Paths.get(resource.getURI()).toString();

		Madec madec = new Madec(path);

		File decrypted = new File(drmDir + File.separator + fileName);
		FileUtils.forceMkdirParent(decrypted);

		try (BufferedInputStream encryptedInputStream = new BufferedInputStream(inputStream);
				BufferedOutputStream decryptedOutputStream = new BufferedOutputStream(new FileOutputStream(decrypted));) {

			long decryptedFileLength = madec.lGetDecryptFileSize(fileName, fileSize, encryptedInputStream);

			log.debug("Size of decrypted file : " + decryptedFileLength);

			if (decryptedFileLength > 0) {
				String resultCode = madec.strMadec(decryptedOutputStream);
				log.debug("Decryption result code : " + resultCode);
				log.debug("Size of decrypted file : " + decryptedFileLength);

				return decrypted;
			} else {
				String errorCode = madec.strGetErrorCode();
				String errorMessage = madec.strGetErrorMessage(errorCode);
				log.error("Decryption error code : " + errorCode);
				log.error("Decryption error message : " + errorMessage);
				throw new RuntimeException(errorCode + " " + errorMessage);
			}
		}
	}

	@Override
	public boolean check(InputStream inputStream, String fileName, long fileSize) throws Exception {
		ClassPathResource resource = new ClassPathResource(drmInfo);
		String path = Paths.get(resource.getURI()).toString();

		MaFileChk maFileChk = new MaFileChk(path);

		long fileChkFileSize = maFileChk.lGetFileChkFileSize(DboxStringUtils.replaceSymbols(fileName), fileSize, inputStream);

		if (fileChkFileSize > 0) {
			log.debug("DBG Decryption - Start!!!! \n ");
			String strRetCode = maFileChk.strMaFileChk();
			log.debug("DBG return code = " + strRetCode);

			return StringUtils.equals(DrmResultType.SUCCESS.getValue(), strRetCode);
		} else {
			String strErrorCode = maFileChk.strGetErrorCode();
			if (!StringUtils.equals(DrmResultType.PLAIN_FILE.getValue(), strErrorCode)) {
				log.error(
						"ERR [ErrorCode] " + strErrorCode + " [ErrorDescription] " + maFileChk.strGetErrorMessage(strErrorCode));
			}

			return false;
		}
	}
}
