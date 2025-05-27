package com.authentication.demo.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.authentication.demo.Model.CollectionModel;
import com.authentication.demo.Model.CommentModel;
import com.authentication.demo.Model.ItemModel;
import com.authentication.demo.Model.UserModel;
import com.authentication.demo.Repository.CommentRepository;

@Service

public class CommentService {

  private final CommentRepository commentRepository;
  private final UserService userService;
  private final CollectionService collectionService;
  private final ItemService itemService;

  public CommentService(CommentRepository commentRepository, UserService userService,
      CollectionService collectionService, ItemService itemService) {
    this.commentRepository = commentRepository;
    this.userService = userService;
    this.collectionService = collectionService;
    this.itemService = itemService;
  }

  public Map<String, String> createComment(Map<String, String> params) {
    // Validate input parameters
    if (params.get("content") == null || params.get("content").isEmpty()) {
      throw new RuntimeException("Comment content is required");
    }

    if (params.get("collectionId") == null) {
      throw new RuntimeException("Collection ID is required");
    }

    Long collectionId = Long.valueOf(params.get("collectionId"));
    CollectionModel collection = collectionService.getCollectionById(collectionId);

    // Create comment
    CommentModel comment = new CommentModel();
    Optional<UserModel> optionalUser = userService.getCurrentUser();
    if (optionalUser.isPresent()) {
      comment.setUser(optionalUser.get());
    } else {
      throw new RuntimeException("User not found");
    }
    comment.setCollection(collection);
    comment.setContent(params.get("content"));
    comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    commentRepository.save(comment);

    return Map.of("message", "Comment created successfully");

  }

  public Map<String, String> createCommentFromIndex(Map<String, String> params) {
    // Validate input parameters
    if (params.get("content") == null || params.get("content").isEmpty()) {
      throw new RuntimeException("Comment content is required");
    }

    if (params.get("collectionId") == null) {
      throw new RuntimeException("Collection ID is required");
    }

    Long collectionId = Long.valueOf(params.get("collectionId"));
    CollectionModel collection = collectionService.getCollectionById(collectionId);

    // Create comment
    CommentModel comment = new CommentModel();
    Optional<UserModel> optionalUser = userService.getCurrentUser();
    if (optionalUser.isPresent()) {
      comment.setUser(optionalUser.get());
    } else {
      throw new RuntimeException("User not found");
    }
    comment.setCollection(collection);
    comment.setContent(params.get("content"));
    comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    commentRepository.save(comment);

    return Map.of("message", "Comment created successfully");

  }

  public Map<String, String> createCommentItem(Map<String, String> params) {
    // Validate input parameters
    if (params.get("content") == null || params.get("content").isEmpty()) {
      throw new RuntimeException("Comment content is required");
    }

    if (params.get("itemId") == null) {
      throw new RuntimeException("Collection ID is required");
    }

    Long itemId = Long.valueOf(params.get("itemId"));
    ItemModel item = itemService.getItemById(itemId);

    // Create comment
    CommentModel comment = new CommentModel();
    Optional<UserModel> optionalUser = userService.getCurrentUser();
    if (optionalUser.isPresent()) {
      comment.setUser(optionalUser.get());
    } else {
      throw new RuntimeException("User not found");
    }
    comment.setItem(item);
    comment.setContent(params.get("content"));
    comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    commentRepository.save(comment);

    return Map.of("message", "Comment created successfully");

  }

  public Long getLatestCommentUrl() {

    UserModel currentUser = userService.getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
    Long userId = currentUser.getId();
    Long latestCommentId = commentRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
        .orElseThrow(() -> new RuntimeException("No comments found for user")).getId();
    return latestCommentId;

  }

  public CommentModel getCommentById(Long commentId) {
    return commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
  }

  public List<CommentModel> getCommentsByItemId(Long itemId) {
    return commentRepository.findByItemIdOrderByCreatedAtAsc(itemId);
  }
  

}
