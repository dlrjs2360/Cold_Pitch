package com.ColdPitch.domain.repository;

import com.ColdPitch.domain.entity.Comment;
import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findAllByPostId(Long postId);

    List<Comment> findAllByParentId(Long replyId);

    List<Comment> findAllByUserId(Long userId);
}