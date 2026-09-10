package com.dodostore.dodostore.service.impl;

import com.dodostore.dodostore.entity.CommentEntity;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.repository.CommentRepository;
import com.dodostore.dodostore.repository.ProductRepository;
import com.dodostore.dodostore.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<CommentEntity> findByProductId(Long productId) {
        return commentRepository.findByProductId(productId);
    }

    @Override
    public void AddComment(CommentEntity newComment, Long productId) {

        newComment.setCreateDate(new Date());
        newComment.setProduct(productRepository.findByIdAndStatus(productId, StatusEnum.Active));
        commentRepository.save(newComment);

    }
}
