package com.dodostore.dodostore.service;

import com.dodostore.dodostore.entity.CommentEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    // Find By UserId
    List<CommentEntity> findByProductId(Long productId);

    // Add Comment
    void AddComment(CommentEntity newComment, Long productId);
}
