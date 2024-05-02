package com.dongkuksystems.dbox.daos.type.code;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.code.CodeLogviewAuth; 

@Primary
@Repository
public class CodeDaoImpl implements CodeDao {
  private CodeMapper codeMapper;

  public CodeDaoImpl(CodeMapper codeMapper) {
    this.codeMapper = codeMapper;
  }
  
  @Override
  public Code selectOne(String objectId) {
  	return codeMapper.selectOne(objectId);
  }

  @Override
  public List<Code> selectList(CodeFilterDto codeFilterDto) {
    return codeMapper.selectList(codeFilterDto);
  }
  
  @Override
  public List<CodeLogviewAuth> selectLogview(String codeType, CodeFilterDto codeFilterDto) {
    return codeMapper.selectLogview(codeType, codeFilterDto);
  }

  @Override
  public List<CodeDetailDto> selectMenuList(String uUserId, CodeFilterDto codeFilterDto) {
    return codeMapper.selectMenuList(uUserId, codeFilterDto);
  }

  @Override
  public Code selectOneByOther(String uCodeType, String uCodeVar1, String uCodeVar2) {
  
    return codeMapper.selectOneByOther(uCodeType, uCodeVar1, uCodeVar2);
  }
  
  @Override
  public List<CodeDetailDto> getHistoryMenuList(String uUserId, String uUserOrgId, CodeFilterDto codeFilterDto) {
    return codeMapper.getHistoryMenuList(uUserId, uUserOrgId, codeFilterDto);
  }
  
}
