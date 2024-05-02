package com.dongkuksystems.dbox.daos.type.path;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.path.DPath;

@Primary
@Repository
public class PathDaoImpl implements PathDao {
    private PathMapper pathMapper;

    public PathDaoImpl(PathMapper pathMapper) {
      this.pathMapper = pathMapper;
    }

    public String selectOrgNmbyCabinetCode(String psCabinetcode){return pathMapper.selectOrgNmbyCabinetCode(psCabinetcode);} /**-- 이관할 폴더명을 cabinet_code로 가져옴 **/
    
    public List<DPath> selectFolTypeList(String psCabinetcode, String psFolType){return pathMapper.selectFolTypeList(psCabinetcode, psFolType);} /**-- 중요문서함(폴더타입 DI*) 이 있는지 확인 **/
    
    //체크용 함수들 Start-->
    @Override
	public List<DPath> selectEOList( String psFolId, String psPrCode){    return pathMapper.selectEOList(psFolId, psPrCode);  } /**-- 하위에 폴더나 문서가 있는지 체크 **/
    
    @Override
    public List<DPath> selectUpLFList(@Param("psFolId") String psFolId, @Param("psPrCode") String psPrCode){    return pathMapper.selectUpLFList(psFolId, psPrCode);  }; /**-- 상위(전체)에 잠긴 폴더가 있는지 체크**/    
    @Override
	public List<DPath> selectLFList( String psFolId, String psPrCode){    return pathMapper.selectLFList(psFolId, psPrCode);  }; /**-- 하위(전체)에 잠긴 폴더가 있는지 체크**/
    @Override
	public List<DPath> selectLDList( String psFolId, String psPrCode){    return pathMapper.selectLDList(psFolId, psPrCode);  }; /**-- (하위에)편집중인 문서가 있는지 체크**/
    @Override
	public List<DPath> selectCDList( String psFolId, String psPrCode){    return pathMapper.selectCDList(psFolId, psPrCode);  }; /**-- 하위(전체)에 Closed 문서가 있는지 체크**/
    @Override
	public List<DPath> selectDDList( String psFolId, String psPrCode){    return pathMapper.selectDDList(psFolId, psPrCode);  };                                   /**-- 하위(전체)에 삭제 상태(문서는 요청중 포함)인 폴더나 문서가 있는지 체크**/
    @Override
	public List<DPath> selectNAList( String psFolId, String psPrCode,  String pUserId, String authExclusive, String ps_Del, String ps_Status, String ps_JobGubun)
    {   
    	    return pathMapper.selectNAList(psFolId, psPrCode, pUserId, authExclusive, ps_Del, ps_Status, ps_JobGubun);  
    }; /**-- 하위(전체)에 타시스템 첨부(원문)한 문서가 있는지 체크**/
    
    @Override
	public List<DPath> selectATList( String psFolId, String psPrCode){    return pathMapper.selectATList(psFolId, psPrCode);  };                                  /**-- 하위(전체)에 권한 없는 폴더나 문서가 있는지 체크**/
    @Override
	public List<DPath> selectDTList( String psFolId ){    return pathMapper.selectDTList(psFolId);  };                                  /**-- 문서관점 : 타시스템 첨부한 문서**/
	
	@Override
	public List<DPath> selectDocAuthCheck( String docKey, String pUserId, int authLevel){    return pathMapper.selectDocAuthCheck(docKey, pUserId, authLevel);  };                                  /**-- 문서관점 : 타시스템 첨부한 문서**/
    
	@Override
	public String selectFolderPath( String psFolId){    return pathMapper.selectFolderPath( psFolId );  };                                         /**-- 폴더경로 조회**/
    //체크용 함수들 End-->
	
	@Override
	public DPath selectAlermType( String psEventCode,  String psCabinet ) { return pathMapper.selectAlermType( psEventCode,  psCabinet );} /**-- 이벤트별로 알림유형 조회하는 처리**/

	@Override
	public String selectTakeoutDocsRobjectIdByDocId( String psDocId)  { return pathMapper.selectTakeoutDocsRobjectIdByDocId( psDocId);}                                   /**-- 반출함 삭제 요청id 조회 **/
	
	
	@Override
	public String selectTemporaryRelManagerId(String psOrgId)  { return pathMapper.selectTemporaryRelManagerId( psOrgId);}                             /**-- 승인자ID 검색 **/
}
