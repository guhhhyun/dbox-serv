package com.dongkuksystems.dbox.daos.type.path;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.path.DPath;


public interface PathDao {
	
	public String selectOrgNmbyCabinetCode(String psCabinetcode); /**-- 이관할 폴더명을 cabinet_code로 가져옴 **/
	
	public List<DPath> selectFolTypeList(String psCabinetcode,String psFolType); /**-- 중요문서함(폴더타입 DI*) 이 있는지 확인 **/
	
	//체크용 함수들 Start-->
	public List<DPath> selectEOList( String psFolId, String psPrCode); /**-- 하위에 폴더나 문서가 있는지 체크 **/
	public List<DPath> selectUpLFList(String psFolId, String psPrCode); /**-- 상위(전체)에 잠긴 폴더가 있는지 체크**/
	public List<DPath> selectLFList( String psFolId, String psPrCode); /**-- 하위(전체)에 잠긴 폴더가 있는지 체크**/
	public List<DPath> selectLDList( String psFolId, String psPrCode); /**-- (하위에)편집중인 문서가 있는지 체크**/
	public List<DPath> selectCDList( String psFolId, String psPrCode); /**-- 하위(전체)에 Closed 문서가 있는지 체크**/
	public List<DPath> selectDDList( String psFolId, String psPrCode); /**-- 하위(전체)에 삭제 상태(문서는 요청중 포함)인 폴더나 문서가 있는지 체크**/
	public List<DPath> selectATList( String psFolId, String psPrCode); /**-- 하위(전체)에 타시스템 첨부(원문)한 문서가 있는지 체크**/
	public List<DPath> selectNAList( String psFolId, String psPrCode,  String pUserId, String authExclusive, String ps_Del, String psStatus, String psJobGubun);   /**-- 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크**/

	public List<DPath> selectDTList( String psFolId );                 /**-- 문서관점 : 타시스템 첨부한 문서**/
	
	public List<DPath> selectDocAuthCheck( String docKey,  String pUserId, int authLevel);   /**-- 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크**/
	public String selectFolderPath( String psFolId);                                         /**-- 폴더경로 조회**/
	public DPath selectAlermType( String psEventCode,  String psCabinet ); /**-- 이벤트별로 알림유형 조회하는 처리**/
	
	public String selectTakeoutDocsRobjectIdByDocId( String psDocId);                                   /**-- 반출함 삭제 요청id 조회 **/
	
	public String selectTemporaryRelManagerId(String psOrgId) ;  /**-- 승인자ID 검색 **/
	//체크용 함수들 End-->

}
