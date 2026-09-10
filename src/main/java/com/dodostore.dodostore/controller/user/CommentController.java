package com.dodostore.dodostore.controller.user;

import com.dodostore.dodostore.entity.CommentEntity;
import com.dodostore.dodostore.service.CommentService;
import com.dodostore.dodostore.utils.GetUserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private GetUserAuthentication getUserAuthentication;

    @PostMapping("/user/comment/{productId}")
    public String comment(@ModelAttribute("newComment") CommentEntity newComment,
                          @PathVariable Long productId){

        newComment.setUser(getUserAuthentication.getUser());
        commentService.AddComment(newComment, productId);
        
        return "redirect:/home/product/{productId}";
    }
}
