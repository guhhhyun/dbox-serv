package com.dongkuksystems.dbox.daos.type.feedback;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.feedback.FeedbackFilterDto;
import com.dongkuksystems.dbox.models.type.feedback.Feedback;

@Primary
@Repository
public class FeedbackDaoImpl implements FeedbackDao {
	private FeedbackMapper feedbackMapper;

	public FeedbackDaoImpl(FeedbackMapper feedbackMapper) {
		this.feedbackMapper = feedbackMapper;
	}
	
	
	
	@Override
	public List<Feedback> getFeedbackList(String dataId) {
	
		return feedbackMapper.getFeedbackList(dataId);
	}

	@Override
	public Optional<Feedback> selectOne(String rObjectId) {
		return feedbackMapper.selectOne(rObjectId);
	}
	
	@Override
	public List<Feedback> selectList(FeedbackFilterDto feedbackFilterDto) {
		return feedbackMapper.selectList(feedbackFilterDto);
	}
	
	@Override
	public int selectCount(FeedbackFilterDto feedbackFilterDto) {
		return feedbackMapper.selectCount(feedbackFilterDto);
	}

	@Override
	public List<Feedback> getCommentList(String dataId, int uLevel, int uGroup) {
	
		return feedbackMapper.getCommentList(dataId, uLevel, uGroup);
	}

	@Override
	public List<Feedback> getFeedbackListByLevel(String dataId, int uLevel) {
		
		return feedbackMapper.getFeedbackListByLevel(dataId, uLevel);
	}

	@Override
	public List<Feedback> getFeedbackListByGroup(String dataId, int uGroup) {
		
		return feedbackMapper.getFeedbackListByGroup(dataId, uGroup);
	}
	
  @Override
  public Feedback getCommentOne(String commentId) {

    return feedbackMapper.getCommentOne(commentId);
  }
	
	
}
