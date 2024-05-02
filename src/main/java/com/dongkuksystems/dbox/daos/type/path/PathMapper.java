package com.dongkuksystems.dbox.daos.type.path;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.path.DPath;

public interface PathMapper {  
	//체크용 함수들 Start-->
	
    public String selectOrgNmbyCabinetCode(@Param("psCabinetcode") String psCabinetcode); /**-- 이관할 폴더명을 cabinet_code로 가져옴 **/
    
    
    public List<DPath> selectFolTypeList(@Param("psCabinetcode") String psCabinetcode, @Param("psFolType") String psFolType); /**-- 중요문서함(폴더타입 DI*) 이 있는지 확인 **/
    
	public List<DPath> selectEOList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode); /**-- 하위에 폴더나 문서가 있는지 체크 **/
	
	public List<DPath> selectUpLFList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode); /**-- 상위(전체)에 잠긴 폴더가 있는지 체크**/
	public List<DPath> selectLFList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode); /**-- 하위(전체)에 잠긴 폴더가 있는지 체크**/
	public List<DPath> selectLDList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode); /**-- (하위에)편집중인 문서가 있는지 체크**/
	public List<DPath> selectCDList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode); /**-- 하위(전체)에 Closed 문서가 있는지 체크**/
	public List<DPath> selectDDList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode);                              /**-- 하위(전체)에 삭제 상태(문서는 요청중 포함)인 폴더나 문서가 있는지 체크**/
	
	public List<DPath> selectNAList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode
			                      , @Param("userId") String pUserId,  @Param("authExclusive") String authExclusive
			                      , @Param("psDel")  String ps_Del,   @Param("psStatus") String psStatus
			                      , @Param("psJobGubun") String psJobGubun);  /**-- 하위(전체)에 권한 없는 폴더나 문서가 있는지 체크**/

	public List<DPath> selectATList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode);  /**-- 하위(전체)에 타시스템 첨부(원문)한 문서가 있는지 체크**/
	
	public List<DPath> selectDTList(@Param("psFolId") String psFolId);                                   /**-- 문서관점 : 타시스템 첨부한 문서**/
	
	public List<DPath> selectDocAuthCheck(@Param("docKey") String docKey, @Param("userId") String pUserId, @Param("authLevel") int authLevel);  /**-- 파일에 권한 있는지 체크  **/
	
	public String selectFolderPath(@Param("psFolId") String psFolId);                                   /**-- 폴더경로 조회**/
	
	public DPath selectAlermType(@Param("psEventCode") String psEventCode, @Param("psCabinet") String psCabinet ); /**-- 이벤트별로 알림유형 조회하는 처리**/

	public String selectTakeoutDocsRobjectIdByDocId(@Param("docId") String psDocId);                                   /**-- 반출함 삭제 요청id 조회 **/
	
	public String selectTemporaryRelManagerId(@Param("psOrgId") String psOrgId);                                   /**-- 승인자ID 검색 **/
	
	//체크용 함수들 End-->
	
}
