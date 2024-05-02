package com.dongkuksystems.dbox.services.feedback;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.feedback.FeedbackDao;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackCreateDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackDetailDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackFatchDto;
import com.dongkuksystems.dbox.models.table.etc.GwJobTitle;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocImp;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.feedback.Feedback;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.doc.DocImpService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl extends AbstractCommonService implements FeedbackService {

	public final FeedbackDao feedbackDao;
	public final AuthService authService;
	public final DocDao docDao;
	public final UserService userService;
	public final CodeService codeService;
	public final DocImpService docImpService;
	public final DeptMgrDao deptMgrDao;
	public final NotiConfigDao notiConfigDao;
	public final GwDeptDao deptDao;
	public final NotificationService notificationService;
	
	public FeedbackServiceImpl(FeedbackDao feedbackDao, AuthService authService, DocDao docDao,
			UserService userService, CodeService codeService, DocImpService docImpService,
			DeptMgrDao deptMgrDao, NotiConfigDao notiConfigDao, GwDeptDao deptDao, NotificationService notificationService) {
		this.feedbackDao = feedbackDao;
		this.authService = authService;
		this.docDao = docDao;
		this.userService = userService;
		this.codeService = codeService;
		this.docImpService = docImpService;
		this.deptMgrDao = deptMgrDao;
		this.notiConfigDao = notiConfigDao;
		this.deptDao = deptDao;
		this.notificationService = notificationService;
	}

	
//	 비공개 여부가 체크되어 있을 경우 작성자, 직급, 부서 제외 // 완
//	 -권한에 따른 처리-
//	   대상 자료에 대한 READ 권한이 없을 경우 권한없음 처리 (403 Forbidden)	// 완
//	   비공개 체크된 문서의 경우 현재 권한설정 무시하고 제한등급 기준으로 권한 확인하여 처리	// 완

//	 모바일에서 요청 시 예외 처리	// 완
//	   회장/부회장/대표이사 외에는 제한등급 문서 권한없음 처리 (403 Forbidden)	// 완
	@Override
	public List<FeedbackDetailDto> getFeedbackList(UserSession userSession, String dataId, HttpServletRequest request) throws Exception {
		final ModelMapper modelMapper = getModelMapper();
		
		Doc docData = docDao.selectOne(dataId).orElse(new Doc());
		Boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;
		Boolean isDocAuth = authService.checkDocAuth(dataId, userSession.getDUserId(), GrantedLevels.READ.getLevel());
		if(!(isDocAuth)) {
			throw new ForbiddenException("권한이 없습니다.");
		}
		if (isMobile) {
	          // 특별사용자 리스트 (회장/부회장/각 회사 대표)
	          Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();

	          // 현재 사용자의 특별사용자 여부 확인
	          boolean isSpecial = specialUserIdSet.contains(userSession.getDUserId());
	          String uSecLevel = docData.getUSecLevel();

	          // 특별사용자가 아니고 제한문서일 경우 에러
	          if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel))
	            throw new ForbiddenException("모바일에서 제한등급 문서 조회 불가");
	    }
		
		List<Feedback> feedbackList = feedbackDao.getFeedbackList(dataId);
		List<FeedbackDetailDto> feedbackDetailList = feedbackList.stream().map((item) -> {
			FeedbackDetailDto feedbackDetailDto = modelMapper.map(item, FeedbackDetailDto.class);
			VUser feedbackUser = Optional.ofNullable(item.getUserDetail()).orElse(new VUser());
			feedbackDetailDto.setUCreateUser(feedbackUser.getUserId());
			feedbackDetailDto.setUCreateUserName(feedbackUser.getDisplayName());
			feedbackDetailDto.setUCreateUserJobTitleCode(feedbackUser.getTitleCode());
			feedbackDetailDto.setUCreateUserJobTitleName(Optional.ofNullable(feedbackUser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
			feedbackDetailDto.setUCreateUseOrgId(feedbackUser.getOrgId());
			feedbackDetailDto.setUCreateUserOrgName(feedbackUser.getOrgNm());
			feedbackDetailDto.setUFeedbackNm(feedbackList.size()+"건");
			return feedbackDetailDto;
		}).collect(Collectors.toList());
		
		
		
		return feedbackDetailList;
	}

//	알람
//	  관리자가 설정한 알람 방식으로 자료 작성자 전체에게 알람 발송/ 회사 전부 알람만 보내는거로 통일임 / FB_W
	@Override
	public String createFeedback(UserSession userSession, FeedbackCreateDto dto, String dataId) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		Doc reqDoc = docDao.selectOne(dataId).orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
		String uDocKey = reqDoc.getUDocKey();
		List<DocRepeating> repeatOne = docDao.selectRepeatingOne(uDocKey, true);
		Map<String, List<DocRepeating>> mapByEditor = repeatOne.stream().collect(Collectors.groupingBy(DocRepeating::getUEditor));
		List<String> editorsToNotify = new ArrayList<>(mapByEditor.keySet());
		String cabinetCode = reqDoc.getUCabinetCode();
		String comCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
		List<Feedback> feed = feedbackDao.getFeedbackListByLevel(dataId, 0);
		IDfPersistentObject idf_PObj = null;
		IDfPersistentObject idf_PObj2 = null;
		boolean isDocImp = false;
		Optional<DocImp> optDocImp = docImpService.selectOne(dataId);
	    isDocImp = optDocImp.isPresent();
	    if ( isDocImp ) {
			throw new BadRequestException("중요보관소 문서 조회 불가");
		}
		idfSession.beginTrans();
		try {

			if (!idfSession.isConnected()) {
				throw new Exception("DCTM Session 가져오기 실패");
			}
			idf_PObj = FeedbackCreateDto.CreateFeedback(idfSession, dto);
			idf_PObj.setInt("u_group", (feed.size()+1));
			idf_PObj.setString("u_doc_key", reqDoc.getUDocKey());
			idf_PObj.save();

			for (String editor : editorsToNotify) {
				if(editor.equals("dmadmin")) continue;
				NotiConfig notiData = notiConfigDao.selectOneByCodes(comCode, "FB_W");
				if (StringUtils.isNotEmpty(editor)) {
					VUser editorData = userService.selectOneByUserId(editor).orElse(new VUser());
					List<String> editorEmail = new ArrayList<>();
					if(null ==editorData.getEmail() || editorData.getEmail().equals("")) {
						System.out.println("#Exception: E-Mail is Not Found(" + editor+")");
					}else {
					    editorEmail.add(editorData.getEmail());
					}
					if ("Y".equals(notiData.getUAlarmYn())) {
						idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
						idf_PObj2.setString("u_msg_type", "FB_W");
						idf_PObj2.setString("u_sender_id", userSession.getDUserId());
						idf_PObj2.setString("u_receiver_id", editor);
						idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 Feedback이 등록되었습니다.");
						idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
						idf_PObj2.setString("u_sent_date", new DfTime().toString());
						idf_PObj2.setString("u_action_need_yn", "N");
						idf_PObj2.save();
					}
					if ("Y".equals(notiData.getUEmailYn())) {
						if(editorEmail.size() > 0 && !editorEmail.get(0).equals("")) {
							StringBuffer content = new StringBuffer();
							content.append("<html> ");
							content.append("<body>");
							content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");
							content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; " + reqDoc.getTitle() + " 문서의 Feedback이 등록되었습니다.</font>");
							content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>");
							content.append(" </body></html>");
							notificationService.sendMail("dbox@dongkuk.com", editorEmail, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + "문서의 Feedback이 등록되었습니다."
									, content.toString());
						}else {
							System.out.println("#feedbackID:" +editor +" 이메일 주소 불량");
						}
					}
					if ("Y".equals(notiData.getUMmsYn())) {
						if(null !=editorData.getMobileTel() && !editorData.getMobileTel().equals("")) {
						    String mobileTel = editorData.getMobileTel().replace("-", "");
						    notificationService.sendKakao(editor, mobileTel, "dbox_alarm_002", reqDoc.getTitle() + " 문서의 Feedback이 등록되었습니다.");
						}else {
				    		System.out.println("feedback ID_카카오:" +editorData.getUserId() +" 핸드폰 폰번호 불량");
				    	}
					}
				}

			}
      // TODO 알림 insert
      // TODO 문서 작성자들에 전부 전송.

			idfSession.commitTrans();
		} catch (Exception e) {
			throw e;
		} finally {
			if (idfSession != null) {
				if (idfSession.isTransactionActive()) {
					idfSession.abortTrans();
				}
				if (idfSession.isConnected()) {
					sessionRelease(userSession.getUser().getUserId(), idfSession);
				}
			}
		}

		return idf_PObj.getObjectId().getId();

	}


	@Override
	public String patchFeedback(UserSession userSession, FeedbackFatchDto dto, String dataId, String feedbackId)
			throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		Doc reqDoc = docDao.selectOne(dataId).orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
		IDfPersistentObject idf_PObj = null;
		
		idfSession.beginTrans();
		try {
			if (idfSession == null || !idfSession.isConnected()) {
				throw new Exception("DCTM Session 가져오기 실패");
			}
			
			idf_PObj = FeedbackFatchDto.PatchFeedback(userSession, idfSession, dto, feedbackId);
			idf_PObj.save();

			idfSession.commitTrans();
		} catch (Exception e) {
			throw e;
		} finally {
			if (idfSession != null) {
				if (idfSession.isTransactionActive()) {
					idfSession.abortTrans();
				}
				if (idfSession.isConnected()) {
					sessionRelease(userSession.getUser().getUserId(), idfSession);
				}
			}
		}
		
		return idf_PObj.getObjectId().getId();
	}

	// 피드백을 삭제할 경우 같은 문답그룹의 댓글들까지 모두 삭제해야할 듯?
	@Override
	public String deleteFeedback(UserSession userSession, String feedbackId) throws Exception {
		
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfPersistentObject idf_PObj = null;
		IDfPersistentObject idf_PObj2 = null;
		Feedback feedbackData = feedbackDao.selectOne(feedbackId).orElse(new Feedback());
		List<Feedback> feedbackList = feedbackDao.getFeedbackListByGroup(feedbackData.getUDocKey(), feedbackData.getUGroup());
		idfSession.beginTrans();
		try {
			if (idfSession == null || !idfSession.isConnected()) {
				throw new Exception("DCTM Session 가져오기 실패");
			}
			idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(feedbackId));
			if(!(userSession.getDUserId().equals(idf_PObj.getString("u_create_user")))) {
				throw new ForbiddenException("권한이 없습니다.");
			}
			for(int i=0; i<feedbackList.size(); i++) {
				idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(feedbackList.get(i).getRObjectId()));
				idf_PObj2.destroy();
			}	
			idfSession.commitTrans();
		} catch (Exception e) {
			throw e;
		} finally {
			if (idfSession != null) {
				if (idfSession.isTransactionActive()) {
					idfSession.abortTrans();
				}
				if (idfSession.isConnected()) {
					sessionRelease(userSession.getUser().getUserId(), idfSession);
				}
			}
		}
		return idf_PObj.getObjectId().getId();
	}

	// 알람
	//  관리자가 설정한 알람 방식으로 피드백 작성자에게 알람 발송 / FB_C
	// TODO feedbackId = feedbackObjId
	@Override
	public String createComment(UserSession userSession, FeedbackCreateDto dto, String dataId, String feedbackId) throws Exception {

		IDfSession idfSession = this.getIdfSession(userSession);
		Doc docData = docDao.selectOne(dataId).orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
		Feedback feed = feedbackDao.selectOne(feedbackId).orElse(new Feedback());
		List<Feedback> commentList = feedbackDao.getCommentList(dataId, 1, feed.getUGroup());
		String cabinetCode = docData.getUCabinetCode();
		String comCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
		VUser createUser = userService.selectOneByUserId(feed.getUCreateUser()).orElse(new VUser());
		List<String> createUserEmail = new ArrayList<>();
		createUserEmail.add(createUser.getEmail());
		IDfPersistentObject idf_PObj;
		IDfPersistentObject idf_PObj2;
		Optional<DocImp> optDocImp = docImpService.selectOne(dataId);
		boolean isDocImp = optDocImp.isPresent();
		if (isDocImp) {
			throw new BadRequestException("중요보관소 문서 조회 불가");
		}
		idfSession.beginTrans();
		try {

			if (!idfSession.isConnected()) {
				throw new Exception("DCTM Session 가져오기 실패");
			}
			idf_PObj = FeedbackCreateDto.CreateFeedback(idfSession, dto);
//			VUser user = userService.selectOneByUserId(idf_PObj.getString("u_create_user")).orElse(new VUser());
			idf_PObj.setString("u_doc_key", docData.getUDocKey());
			// 대댓글 부터는 댓글의 order와 같게?
			if (feed.getULevel() >= 1) {
				idf_PObj.setInt("u_order", feed.getUOrder());
			} else {
				idf_PObj.setInt("u_order", commentList.size() + 1);
			}
			idf_PObj.setInt("u_level", feed.getULevel() + 1);
			idf_PObj.setInt("u_group", (feed.getUGroup()));
			idf_PObj.save();

			NotiConfig notiData = notiConfigDao.selectOneByCodes(comCode, "FB_C");
			if ("Y".equals(notiData.getUAlarmYn())) {
				idf_PObj2 = idfSession.newObject("edms_noti");
				idf_PObj2.setString("u_msg_type", "FB_C");
				idf_PObj2.setString("u_sender_id", userSession.getDUserId());
				idf_PObj2.setString("u_receiver_id", feed.getUCreateUser());
				idf_PObj2.setString("u_msg", "'" + docData.getTitle() + "'" + " 문서의 Feedback에 댓글이 등록되었습니다.");
				idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
				idf_PObj2.setString("u_sent_date", new DfTime().toString());
				idf_PObj2.setString("u_action_need_yn", "N");
				idf_PObj2.save();
			}
			if ("Y".equals(notiData.getUEmailYn())) {
				StringBuffer content = new StringBuffer();
				content.append("<html> ");
				content.append("<body>");
				content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");
				content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; " + docData.getTitle() + " 문서의 Feedback에 댓글이 등록되었습니다.</font>");
				content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>");
				content.append(" </body></html>");
				notificationService.sendMail("dbox@dongkuk.com", createUserEmail, "[D'Box]" + "'" + docData.getTitle() + "'" + "문서의 Feedback에 댓글이 등록되었습니다."
						, content.toString());
			}
			if ("Y".equals(notiData.getUMmsYn())) {
				String mobileTel = createUser.getMobileTel().replace("-", "");
				notificationService.sendKakao(feed.getUCreateUser(), mobileTel, "dbox_alarm_003", docData.getTitle() + " 문서의 Feedback에 댓글이 등록되었습니다.");
			}


			idfSession.commitTrans();
		} catch (Exception e) {
			throw e;
		} finally {
			if (idfSession.isTransactionActive()) {
				idfSession.abortTrans();
			}
			if (idfSession.isConnected()) {
				sessionRelease(userSession.getUser().getUserId(), idfSession);
			}
		}

		return idf_PObj.getObjectId().getId();
	}


	@Override
	public String patchComment(UserSession userSession, String feedbackId, String commentId, FeedbackFatchDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfPersistentObject idf_PObj = null;
		
		idfSession.beginTrans();
		try {
			if (idfSession == null || !idfSession.isConnected()) {
				throw new Exception("DCTM Session 가져오기 실패");
			}
			
			idf_PObj = FeedbackFatchDto.PatchFeedback(userSession, idfSession, dto, commentId);
			idf_PObj.save();

			idfSession.commitTrans();
		} catch (Exception e) {
			throw e;
		} finally {
			if (idfSession != null) {
				if (idfSession.isTransactionActive()) {
					idfSession.abortTrans();
				}
				if (idfSession.isConnected()) {
					sessionRelease(userSession.getUser().getUserId(), idfSession);
				}
			}
		}
		
		return idf_PObj.getObjectId().getId();
	}
	
	@Override
	public String deleteComment(UserSession userSession, String commentId) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfPersistentObject idf_PObj = null;
		
		idfSession.beginTrans();
		try {
			if (idfSession == null || !idfSession.isConnected()) {
				throw new Exception("DCTM Session 가져오기 실패");
			}
			
			idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(commentId));
			if(!(userSession.getDUserId().equals(idf_PObj.getString("u_create_user")))) {
				throw new ForbiddenException("권한이 없습니다.");
			}
			idf_PObj.destroy();
			

			idfSession.commitTrans();
		} catch (Exception e) {
			throw e;
		} finally {
			if (idfSession != null) {
				if (idfSession.isTransactionActive()) {
					idfSession.abortTrans();
				}
				if (idfSession.isConnected()) {
					sessionRelease(userSession.getUser().getUserId(), idfSession);
				}
			}
		}
		return idf_PObj.getObjectId().getId();
	}
	
	
	
}
