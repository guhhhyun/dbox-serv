package com.dongkuksystems.dbox.daos.table.kakao;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.kakao.KakaoDetail;

public interface KakaoMapper {

  public void insertKakao(@Param("kakaoData") KakaoDetail kakaoData);


}
