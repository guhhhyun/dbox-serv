package com.dongkuksystems.dbox.daos.type.code;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.code.CodeLogviewAuth; 


public interface CodeDao {
	public Code selectOne(String objectId);
  public List<Code> selectList(CodeFilterDto codeFilterDto);
  public List<CodeLogviewAuth> selectLogview(String codeType, CodeFilterDto codeFilterDto);
  public List<CodeDetailDto> selectMenuList(String uUserId, CodeFilterDto codeFilterDto); 
  public Code selectOneByOther(String uCodeType, String uCodeVar1, String uCodeVar2);
  public List<CodeDetailDto> getHistoryMenuList(String uUserId, String uUserOrgId, CodeFilterDto codeFilterDto);
}
