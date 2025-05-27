package com.authentication.demo.Service;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.authentication.demo.Model.LikeModel;
import com.authentication.demo.Model.UserModel;
import com.authentication.demo.Repository.CollectionRepository;
import com.authentication.demo.Repository.CommentRepository;
import com.authentication.demo.Repository.ItemRepository;
import com.authentication.demo.Repository.LikeRepository;
import com.authentication.demo.Repository.ReplyRepository;
import com.authentication.demo.Repository.UserRepository;

@Service

public class LikeService {

  private final LikeRepository likeRepository;
  private final CollectionRepository collectionRepository;
  private final UserRepository userRepository;
  private final ItemRepository itemRepository;
  private final CommentRepository commentRepository;
  private final ReplyRepository replyRepository;

  public LikeService(
    LikeRepository likeRepository,
    CollectionRepository collectionRepository,
    UserRepository userRepository,
    ItemRepository itemRepository,
    CommentRepository commentRepository,
    ReplyRepository replyRepository) {
    this.likeRepository = likeRepository;
    this.collectionRepository = collectionRepository;
    this.userRepository = userRepository;
    this.itemRepository = itemRepository;
    this.commentRepository = commentRepository;
    this.replyRepository = replyRepository;
  }

  public void toggleLike(Long userId, Long collectionId) {
    UserModel currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    List<LikeModel> existingLikes = likeRepository.findAllByCollectionId(collectionId);
    boolean alreadyLiked = existingLikes.stream()
        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
    if (alreadyLiked) {
      // If the user already liked the comment, remove the like
      LikeModel existingLike = existingLikes.stream()
          .filter(like -> like.getUser().getId().equals(currentUser.getId()))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Like not found"));
      likeRepository.delete(existingLike);
    } else {
      // If the user has not liked the comment, add a new like
      LikeModel newLike = new LikeModel();
      newLike.setUser(currentUser);
      newLike.setCollection(collectionRepository.findById(collectionId)
          .orElseThrow(() -> new RuntimeException("Collection not found")));
      likeRepository.save(newLike);
    }
  }

  public Integer countLikes(Long collectionId) {
    return likeRepository.countByCollectionId(collectionId);
  }

  public void toggleLikeItem(Long userId, Long itemId) {
    UserModel currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    List<LikeModel> existingLikes = likeRepository.findAllByItemId(itemId);
    boolean alreadyLiked = existingLikes.stream()
        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
    if (alreadyLiked) {
      // If the user already liked the comment, remove the like
      LikeModel existingLike = existingLikes.stream()
          .filter(like -> like.getUser().getId().equals(currentUser.getId()))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Like not found"));
      likeRepository.delete(existingLike);
    } else {
      // If the user has not liked the comment, add a new like
      LikeModel newLike = new LikeModel();
      newLike.setUser(currentUser);
      newLike.setItem(itemRepository.findById(itemId)
          .orElseThrow(() -> new RuntimeException("Collection not found")));
      likeRepository.save(newLike);
    }

  }

  public Integer countLikesItem(Long itemId) {
    return likeRepository.countByItemId(itemId);
  }

  //From the index page

  public void toggleLikeFromIndex(Long userId, Long collectionId) {
    UserModel currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    List<LikeModel> existingLikes = likeRepository.findAllByCollectionId(collectionId);
    boolean alreadyLiked = existingLikes.stream()
        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
    if (alreadyLiked) {
      // If the user already liked the collection, remove the like
      LikeModel existingLike = existingLikes.stream()
          .filter(like -> like.getUser().getId().equals(currentUser.getId()))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Like not found"));
      likeRepository.delete(existingLike);
    } else {
      // If the user has not liked the collection, add a new like
      LikeModel newLike = new LikeModel();
      newLike.setUser(currentUser);
      newLike.setCollection(collectionRepository.findById(collectionId)
          .orElseThrow(() -> new RuntimeException("Collection not found")));
      likeRepository.save(newLike);
    }
  }

  public Integer countLikesComment(Long commentId) {
    return likeRepository.countByCommentId(commentId);
  }


  public void toggleLikeComment(Long userId, Long commentId) {
    UserModel currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    List<LikeModel> existingLikes = likeRepository.findAllByCommentId(commentId);
    boolean alreadyLiked = existingLikes.stream()
        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
    if (alreadyLiked) {
      // If the user already liked the collection, remove the like
      LikeModel existingLike = existingLikes.stream()
          .filter(like -> like.getUser().getId().equals(currentUser.getId()))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Like not found"));
      likeRepository.delete(existingLike);
    } else {
      // If the user has not liked the collection, add a new like
      LikeModel newLike = new LikeModel();
      newLike.setUser(currentUser);
      newLike.setComment(commentRepository.findById(commentId)
          .orElseThrow(() -> new RuntimeException("Comment not found")));
      likeRepository.save(newLike);
    }

  }

  public void toggleLikeReply(Principal principal, Long replyId) {
    
    String username = principal.getName();
    UserModel currentUser = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    List<LikeModel> existingLikes = likeRepository.findAllByReplyId(replyId);
    boolean alreadyLiked = existingLikes.stream()
        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
    if (alreadyLiked) {
      // If the user already liked the reply, remove the like
      LikeModel existingLike = existingLikes.stream()
          .filter(like -> like.getUser().getId().equals(currentUser.getId()))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Like not found"));
      likeRepository.delete(existingLike);
    } else {
      // If the user has not liked the reply, add a new like
      LikeModel newLike = new LikeModel();
      newLike.setUser(currentUser);
      newLike.setReply(replyRepository.findById(replyId)
          .orElseThrow(() -> new RuntimeException("Reply not found")));
      likeRepository.save(newLike);
    }

  }

  public Integer countLikesReply(Long replyId) {
    return likeRepository.countByReplyId(replyId);
  }
}
