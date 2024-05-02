package com.dongkuksystems.dbox.daos.type.code;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.code.CodeLogviewAuth;

public interface CodeMapper {
	public Code selectOne(@Param("objectId") String objectId);
	public List<Code> selectList(@Param("code") CodeFilterDto codeFilterDto);
	public List<CodeLogviewAuth> selectLogview(@Param("codeType") String codeType, @Param("code") CodeFilterDto codeFilterDto);
	public List<CodeDetailDto> selectMenuList(@Param("uUserId") String uUserId, @Param("code") CodeFilterDto codeFilterDto);
	public Code selectOneByOther(@Param("uCodeType") String uCodeType, @Param("uCodeVal1") String uCodeVal1, @Param("uCodeVal2") String uCodeVal2);
	public List<CodeDetailDto> getHistoryMenuList(@Param("uUserId") String uUserId, @Param("uUserOrgId") String uUserOrgId, @Param("code") CodeFilterDto codeFilterDto);
}
