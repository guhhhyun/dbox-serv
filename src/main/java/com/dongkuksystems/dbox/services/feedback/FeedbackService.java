package com.dongkuksystems.dbox.services.feedback;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackCreateDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackDetailDto;
import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackFatchDto;


public interface FeedbackService {

	List<FeedbackDetailDto> getFeedbackList(UserSession userSession, String dataId, HttpServletRequest request) throws Exception;

	String createFeedback(UserSession userSession, FeedbackCreateDto dto, String dataId) throws Exception;
	String patchFeedback(UserSession userSession, FeedbackFatchDto dto, String dataId, String feedbackId) throws Exception;
	String deleteFeedback(UserSession userSession, String feedbackId) throws Exception;
	String createComment(UserSession userSession, FeedbackCreateDto dto, String dataId, String feedbackId) throws Exception;
	String patchComment(UserSession userSession, String feedbackId, String commentId, FeedbackFatchDto dto) throws Exception;
	String deleteComment(UserSession userSession, String commentId) throws Exception;

}
