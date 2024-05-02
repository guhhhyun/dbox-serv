package com.dongkuksystems.dbox.services.kakao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.table.kakao.KakaoDao;
import com.dongkuksystems.dbox.models.dto.type.kakao.KakaoDetail;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.user.UserService;

@Service
public class KakaoServiceImpl extends AbstractCommonService implements KakaoService {
  private final UserService userService;
  private final KakaoDao kakaoDao;
  

  public KakaoServiceImpl(UserService userService, KakaoDao kakaoDao) {
    this.userService = userService;
    this.kakaoDao = kakaoDao;
  }


  @Override
  public void insertKakao(String reqUserId, String callphone, String templatecode, String msg) throws Exception {
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String formatedNow = now.format(formatter);
    LocalTime time = LocalTime.now();
    DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HHmmss");
    String timeformatNow = time.format(timeformat);

      VUser user = userService.selectOneByUserId(reqUserId).orElse(new VUser());
      KakaoDetail kakaoData = KakaoDetail.builder()
          .usercode("dbox")
          .biztype("at")
          .yellowidKey("92fb64a3500ed9b9d0e0fd2a2638d2b6e43c80ae")
          .reqname("suerM")
          .reqphone("15884640")
          .msg(msg)
          .reqtime("00000000000000")
          .result("0")
          .kind("T")
          .resend("N")
          .templatecode(templatecode)
          .callphone(callphone)
          .deptcode("T9-YC2-2V")
          .intime(formatedNow+timeformatNow)
          .callname(user.getDisplayName())
          .build();
      
      kakaoDao.insertKakao(kakaoData);

    
    return ;

  }
  
  
  
}
