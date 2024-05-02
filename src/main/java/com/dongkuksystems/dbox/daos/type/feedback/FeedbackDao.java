package com.dongkuksystems.dbox.daos.type.feedback;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackFilterDto;
import com.dongkuksystems.dbox.models.type.feedback.Feedback; 

public interface FeedbackDao {
  public Optional<Feedback> selectOne(String objectId);
  public List<Feedback> selectList(FeedbackFilterDto feedbackFilterDto);
  public int selectCount(FeedbackFilterDto feedbackFilterDto);
  public List<Feedback> getFeedbackList(String dataId);
  public List<Feedback> getCommentList(String dataId, int uLevel, int uGroup);
  public List<Feedback> getFeedbackListByLevel(String dataId, int uLevel);
  public List<Feedback> getFeedbackListByGroup(String dataId, int uGroup);
  public Feedback getCommentOne(String commentId);
}
