package com.dongkuksystems.dbox.services.data;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateBatchReqDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateReqDto;
import com.dongkuksystems.dbox.models.dto.type.data.RegistDataDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.type.user.UserPreset;

public interface DataPathService {
    //권한, 잠김, 삭제여부등 체크
	DPath getCheckList(UserSession userSession, String ps_Gubun, String ps_FolId, String ps_PrCode, boolean isMobile, DPath dto) throws Exception; 
	List<DPath> selectDTList(String ps_DocKey) throws Exception; //파일아이디로 해당 파일의 첨부여부 확인
	List<DPath> selectNAList(String ps_FolId,  String psPrCode, String ps_UserId, String ps_AuthExclusive, String ps_Del, String psStatus, String ps_JobGubun) throws Exception;
	
	List<DPath> selectDocAuthCheck(String docKey,  String ps_UserId, int pi_AuthLevel) throws Exception; //파일 권한 확인

	//중복된 프로젝트명 확인 후 최종 폴더명 반환(root용)
	String getChekedPjtName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception;
    
	//중복된 연구과제명
	String getChekedRscsName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception;
	
	//중복된 폴더명 확인 후 최종 폴더명 반환(root용)
	String getChekedFolderName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_UpFolId, String ps_FolName, DPath dto, String s_FolId) throws Exception;
	//중복된 파일명 확인 후 최종 폴더명 반환(root용)
	String getChekedDocName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_u_fol_id, String ps_DocName, DPath dto) throws Exception;
	String getChekedDocNameScnd(IDfSession  idf_Sess, String ps_CabinetCode, String ps_u_fol_id, String ps_DocName, DPath dto, String ps_DocId , String ps_Extr) throws Exception;
	
	public List<DPath> selectFolTypeList(String psCabinetcode,String psFolType) throws Exception; /**-- 중요문서함(폴더타입 DI*) 이 있는지 확인 **/

  	DPath selectAlermType( String psEventCode,  String psCabinet ) throws Exception;/**-- 이벤트별로 알림유형 조회하는 처리**/
	
	//복사처리
	String copyFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto,  Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception;
	String copyFolderAndFile(UserSession userSession, IDfSession idfSess, DPath dto, List<String> folList, Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception;	
	
	IDfDocument saveAsNewFiles(IDfSession idfAdminSess, IDfDocument idfDoc, DPath dto ) throws Exception;
	IDfDocument saveAsNewImpFiles(IDfSession idfAdminSess, IDfDocument idfDoc , DPath dto) throws Exception;
    
	//이동처리
	String moveFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception;	
	String moveFolderAndFile(UserSession userSession, IDfSession idfSess, DPath dto, List<String> folList,Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception;	
	
	//삭제처리
	String deleteFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception;
	String deleteFolderAndFile(UserSession userSession, IDfSession idfSess, DPath dto, List<String> folList, Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception;

	
	//폴더 삭제
	boolean DeleteFolObject(IDfSession idfSess, IDfPersistentObject idf_FolObj, String ps_ReqUser, String ps_FolId, String ps_DelFlag, DPath dto)    throws Exception;
	//파일 삭제
	boolean DeleteDocObject(UserSession userSession,IDfSession  idf_Sess, String ps_ReqUser, String ps_DocId, String ps_DelFlag, DPath dto) throws Exception;
	
	//프로젝트나 연구과제 삭제건수가 많아서 배치등록할 때 u_delete_status는 먼저 Y로 바꿔놓을때 사용한다.
	boolean updateDeleteStatus(UserSession userSession, String ps_ObjId, String ps_Status ) throws Exception;
	
	
	//이관처리
	String transFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception;
	String transFolderAndFile(UserSession userSession, IDfSession idfSess, DPath dto, List<String> folList, Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception;
	
	//폴더이관
  	String TransFol(IDfSession idfSession, String ps_ReqUser, String ps_FolId,String ps_SendCab,String ps_RcevCab,String ps_RcevFolId, DPath dto) throws Exception;
  	//문서이관
  	String TransDoc(IDfSession idfSession, IDfDocument idfDoc,String ps_RcevFolId, String ps_RcevCab, DPath dto) throws Exception;  	
  	
  	String CreateTransFolder(IDfSession  idfSession, String ps_CabCode, String ps_DeptName, DPath dto) throws Exception; //이관폴더 생성
  	
	void addTransBatchObjects(UserSession userSession,  DPath dto) throws Exception;  //이관 배치작업 대상 등록
	void addMcdBatchObjects(UserSession userSession,    DPath dto) throws Exception;  //이동,복사,삭제 배치작업 대상 등록
	
	//공유/협업폴더 지정
  	String shareFol(UserSession userSession, DPath dto,List<String> sFolArray) throws Exception;
  	
  	String selectTakeoutDocsRobjectIdByDocId( String psDocId) throws Exception;                                  /**-- 반출함 삭제 요청id 조회 **/
  	
  	void addMoveAuthBase(UserSession userSession, IDfSession idfSession, String rObjectId, String ps_RcevCab, String objType, DPath dto, IDfDocument idfNewDoc) throws Exception;

	String selectTemporaryRelManagerId(String psOrgId)  throws Exception;    /**-- 승인자ID 검색 **/
	
	public String GetFolderPathFromDCTM(UserSession userSession,String ps_FolId, boolean pb_First)  throws Exception; //폴더의 현재 경로 문자열로 반환	
  	
}
