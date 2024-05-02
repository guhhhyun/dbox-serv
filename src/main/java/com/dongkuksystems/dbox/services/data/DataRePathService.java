package com.dongkuksystems.dbox.services.data;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;

public interface DataRePathService {

	//중복된 프로젝트명 확인 후 최종 폴더명 반환(root용)
	String getChekedPjtName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception;
	//중복된 연구과제명
	String getChekedRscsName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception;
	//중복된 폴더명 확인 후 최종 폴더명 반환(root용)
	String getChekedFolderName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_UpFolId, String ps_FolName, DPath dto, String s_FolId) throws Exception;
	//중복된 파일명 확인 후 최종 폴더명 반환(root용)
	String getChekedDocNameScnd(IDfSession  idf_Sess, String ps_CabinetCode, String ps_u_fol_id, String ps_DocName, DPath dto, String ps_DocId , String ps_Extr) throws Exception;
	
	//이동처리
	String moveFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception;	
	String moveFolderAndFile(UserSession userSession, IDfSession idfSess, DPath dto, List<String> folList,Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception;	
	
  	void addMoveAuthBaseRe(UserSession userSession, IDfSession idfSession, String rObjectId, String ps_RcevCab, String objType, DPath dto, IDfDocument idfNewDoc) throws Exception;

	IDfDocument saveAsNewFiles(IDfSession idfAdminSess, IDfDocument idfDoc, DPath dto ) throws Exception;
	IDfDocument saveAsNewImpFiles(IDfSession idfAdminSess, IDfDocument idfDoc , DPath dto) throws Exception;
	
}
