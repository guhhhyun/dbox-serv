package com.dongkuksystems.dbox.services.manager.duplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.daos.type.manager.duplication.DuplicationDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.duplication.DuplicationDto;
import com.dongkuksystems.dbox.models.dto.type.manager.duplication.PatchDuplicationDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.manager.duplication.Duplication;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.MailSenderUtils;

@Service
public class DuplicationServiceImpl extends AbstractCommonService implements DuplicationService {

	private final DuplicationDao duplicationDao;
	private final PathDao pathDao;
	private final MailSenderUtils mailSenderUtils;
	private final UserService userService;
	private final NotificationService notificationService;

	public DuplicationServiceImpl(DuplicationDao duplicationDao, PathDao pathDao, MailSenderUtils mailSenderUtils, UserService userService, NotificationService notificationService) {		
		this.duplicationDao = duplicationDao;
		this.pathDao = pathDao;
		this.mailSenderUtils = mailSenderUtils;
		this.userService = userService;
		this.notificationService = notificationService;
	}

	@Override
	public List<Duplication> selectAll(DuplicationDto dto, long offset, int limit) {	
		return duplicationDao.selectAll(dto, offset, limit);
	}
	
	@Override
  public int selectAllCount(DuplicationDto dto) {
    return duplicationDao.selectAllCount(dto);
  }
	
	@Override
	public List<Duplication> selectList(DuplicationDto dto) {	
    List<Duplication> list = duplicationDao.selectList(dto);
    for(Duplication dup : list) {
      String result = pathDao.selectFolderPath(dup.getUFolId());
      dup.setUFolderPath(result);
    }
		return list;
	}	

	@Override
	public String patchDuplication(String rObjectId, UserSession userSession, PatchDuplicationDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);		
		IDfPersistentObject idf_PObj = PatchDuplicationDto.PatchDuplication(rObjectId, idfSession, dto);		
		return idf_PObj.getObjectId().getId();
	}

  @Override
  public Map<String, Integer> sendAllMail(UserSession userSession, DuplicationDto dto) throws Exception {
    int successCnt = 0;
    int failCnt = 0;
    
    for (String hashId : dto.getRContentHashList()) {
      try {
        dto.setRContentHash(hashId);
        List<Duplication> result = duplicationDao.selectList(dto);
        for(Duplication dc: result) {
          VUser reqUser = userService.selectOneByUserId(dc.getURegUser()).orElse(null);
          List<String> userEmail = new ArrayList<>();
          userEmail.add(reqUser.getEmail());
          StringBuffer content = new StringBuffer();       
          content.append("<html> ");   
          content.append("<body>");           
          content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+dc.getTitle()+" 문서는 중복자료입니다.</font>");  
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
          content.append(" </body></html>");          
          notificationService.sendMail(userSession.getUser().getEmail(), userEmail, "'" + dc.getTitle() + "'" + " 문서는 중복자료입니다."
          , content.toString());
          successCnt++;
        }
      } catch (Exception e) {
        failCnt++;
        continue;
      }
    }
    
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("success", successCnt);
    result.put("fail", failCnt);

    return result;
  }

}
