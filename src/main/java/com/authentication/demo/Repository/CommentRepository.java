package com.authentication.demo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.authentication.demo.Model.CommentModel;

@Repository
public interface CommentRepository extends JpaRepository<CommentModel, Long> {

  List<CommentModel> findByCollectionIdOrderByCreatedAtDesc(Long collectionId);

  List<CommentModel> findByCollectionIdOrderByCreatedAtAsc(Long collectionId);

  Integer countByCollectionId(Long collectionId);

  List<CommentModel> findByItemIdOrderByCreatedAtDesc(Long itemId);

  List<CommentModel> findByItemIdOrderByCreatedAtAsc(Long itemId);

  Integer countByItemId(Long itemId);

  Optional<CommentModel> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
