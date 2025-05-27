package com.authentication.demo.Service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Service;

import com.authentication.demo.Model.ReplyModel;
import com.authentication.demo.Repository.LikeRepository;
import com.authentication.demo.Repository.ReplyRepository;


@Service

public class ReplyService {
  private final ReplyRepository replyRepository;
  private final CommentService commentService;
  private final LikeRepository likeRepository;
  private final UserService userService;

  public ReplyService(ReplyRepository replyRepository, CommentService commentService, LikeRepository likeRepository, UserService userService) {
    this.replyRepository = replyRepository;
    this.commentService = commentService;
    this.likeRepository = likeRepository;
    this.userService = userService;
  }

  

  // GET REPLIES BY COMMENT ID
  public List<ReplyModel> getRepliesByCommentId(Long commentId) {
    return replyRepository.findByCommentIdOrderByCreatedAtAsc(commentId);
  }


  //CREATE REPLY
  public ReplyModel createReply(Long commentId, String content, String username) {
    ReplyModel reply = new ReplyModel();

    reply.setContent(content);
    reply.setComment(commentService.getCommentById(commentId));
    reply.setUser(userService.getUserByUsername(username).orElseThrow(() -> new RuntimeException("User not found with username: " + username)));
    reply.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    return replyRepository.save(reply);
  }


  // GET LATEST REPLY URL
  public Long getLatestReplyId() {
    ReplyModel latestReply = replyRepository.findTopByOrderByIdDesc();
    if (latestReply == null) {
      throw new RuntimeException("No replies found");
    }
    return latestReply.getId();
  }

  // GET REPLIES BY REPLY ID
  public ReplyModel getReplyById(Long replyId) {
    return replyRepository.findById(replyId)
        .orElseThrow(() -> new RuntimeException("Reply not found with id: " + replyId));
  }

  // Count reply likes
  public Integer countReplyLikes(Long replyId) {
    return likeRepository.countByReplyId(replyId);
  }



}