package com.dongkuksystems.dbox.services.kakao;


import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.kakao.KakaoDetail;


public interface KakaoService {
  void insertKakao(String reqUserId, String callphone, String templatecode, String msg) throws Exception;
}
