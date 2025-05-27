package com.authentication.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.authentication.demo.Model.CommentModel;
import com.authentication.demo.Repository.CommentRepository;

@Service
public class CommentQueryService {

  private final CommentRepository commentRepository;

  public CommentQueryService(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  public List<CommentModel> getCommentsByCollectionIdDesc(Long collectionId) {
    return commentRepository.findByCollectionIdOrderByCreatedAtDesc(collectionId);
  }

  public List<CommentModel> getCommentsByCollectionIdAsc(Long collectionId) {
    return commentRepository.findByCollectionIdOrderByCreatedAtAsc(collectionId);
  }

  public List<CommentModel> getCommentsByItemIdDesc(Long itemId) {
    return commentRepository.findByItemIdOrderByCreatedAtDesc(itemId);
  }

  public List<CommentModel> getCommentsByItemIdAsc(Long itemId) {
    return commentRepository.findByItemIdOrderByCreatedAtAsc(itemId);
  }

  public Integer countByCollectionId(Long collectionId) {
    return commentRepository.countByCollectionId(collectionId);
  }

  public Integer countByItemId(Long itemId) {
    return commentRepository.countByItemId(itemId);
  }
}

