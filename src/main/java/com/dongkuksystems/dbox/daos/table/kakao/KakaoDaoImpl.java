package com.dongkuksystems.dbox.daos.table.kakao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.kakao.KakaoDetail;

@Primary
@Repository
public class KakaoDaoImpl implements KakaoDao{
  private KakaoMapper kakaoMapper;
  
  public KakaoDaoImpl(KakaoMapper kakaoMapper) {
    this.kakaoMapper = kakaoMapper;
  }

  @Override
  public void insertKakao(KakaoDetail kakaoData) {
     kakaoMapper.insertKakao(kakaoData);
  }
  
}
