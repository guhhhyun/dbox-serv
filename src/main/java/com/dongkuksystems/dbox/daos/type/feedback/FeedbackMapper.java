package com.dongkuksystems.dbox.daos.type.feedback;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackFilterDto;
import com.dongkuksystems.dbox.models.type.feedback.Feedback; 

public interface FeedbackMapper {
  public Optional<Feedback> selectOne(@Param("objectId") String objectId);
  public List<Feedback> selectList(@Param("feedback") FeedbackFilterDto feedbackFilterDto);
  public int selectCount(@Param("feedback") FeedbackFilterDto feedbackFilterDto);
  public List<Feedback> getFeedbackList(@Param("uDocKey") String dataId);
  public List<Feedback> getCommentList(@Param("uDocKey") String dataId, @Param("uLevel") int uLevel, @Param("uGroup") int uGroup);
  public List<Feedback> getFeedbackListByLevel(@Param("uDocKey") String dataId, @Param("uLevel") int uLevel);
  public List<Feedback> getFeedbackListByGroup(@Param("uDocKey") String dataId, @Param("uGroup") int uGroup);
  public Feedback getCommentOne(@Param("rObjectId") String commentId);
  
}
		