package com.authentication.demo.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.authentication.demo.Exceptions.CollectionCreationException;
import com.authentication.demo.Model.CollectionModel;
import com.authentication.demo.Model.CommentModel;
import com.authentication.demo.Model.ItemModel;
import com.authentication.demo.Repository.CollectionRepository;

@Service

public class CollectionService {
  private final CollectionRepository repository;
  private final ItemService itemService;
  private final UserService userService;
  private final S3Service s3Service;
  private final ImageService imageService;
  private final CommentQueryService commentQueryService;

  @Value("${aws.s3.AWS_S3_BUCKET_COLLECTION_IMAGES}")
  private String collectionImagesBucket;

  public CollectionService(CollectionRepository repository, ItemService itemService,
      UserService userService, S3Service s3Service, ImageService imageService,
      CommentQueryService commentQueryService) {
    this.repository = repository;
    this.itemService = itemService;
    this.userService = userService;
    this.s3Service = s3Service;
    this.imageService = imageService;
    this.commentQueryService = commentQueryService;
  }

  // GET COLLECTION BY COLLECTION ID
  public CollectionModel getCollectionById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Collection not found with id: " + id));
  }

  // GET COLLECTIONS BY FOLLOWING WITH LOGGING
  public List<CollectionModel> getCollectionsByFollowingWithLogging(Long userId) {
    List<CollectionModel> collections = repository.findAllByFollowing(userId);
    System.out.println("Collections by Following: " + collections.size());
    return collections;
  }

  // GET ALL COLLECTIONS FOR CURRENT USER
  public List<CollectionModel> getAllCollectionsForCurrentUser() {
    Long userId = userService.getCurrentUserId();
    return repository.findAllByUserIdOrderByCreatedAtDesc(userId);
  }

  // CREATE COLLECTION
  public Map<String, String> createCollection(Map<String, String> params, MultipartFile collectionImage)
      throws IOException {

    try {
      // Validate input parameters
      if (collectionImage == null) {
        throw new CollectionCreationException("Collection image cannot be null");
      }
      if (collectionImage.isEmpty()) {
        throw new CollectionCreationException("Collection image cannot be empty");
      }

      if (params.get("title") == null || params.get("title").isEmpty()) {
        throw new CollectionCreationException("Title is required");
      }

      // Save collection logic
      MultipartFile processedFile = imageService.processImage(collectionImage);

      Long userId = userService.getCurrentUserId();
      Timestamp createdAt = new Timestamp(System.currentTimeMillis());
      Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

      CollectionModel newCollection = new CollectionModel();
      newCollection.setUser(userService.getUserById(userId));
      newCollection.setTitle(params.get("title"));
      newCollection.setCaption(params.get("caption"));
      newCollection.setImageUrl(saveCollectionImage(processedFile));
      newCollection.setCreatedAt(createdAt);
      newCollection.setUpdatedAt(updatedAt);

      repository.save(newCollection);
      return Map.of("message", "Collection created successfully");
    } catch (CollectionCreationException e) {
      throw new CollectionCreationException(e.getMessage(), e);
    } catch (IOException | RuntimeException e) {
      throw new CollectionCreationException(e.getMessage(), e);
    }
  }

  // SAVE COLLECTION IMAGE
  public String saveCollectionImage(MultipartFile collectionImage) throws IOException {
    String bucketName = collectionImagesBucket;
    String filename = UUID.randomUUID().toString() + "-" + collectionImage.getOriginalFilename();

    // Validate the file
    if (collectionImage.isEmpty()) {
      throw new IllegalArgumentException("Collection Image file is empty");
    }

    // Upload the file to S3
    try {
      System.out.println("Starting file upload to S3...");
      File tempFile = File.createTempFile("upload-", collectionImage.getOriginalFilename());
      collectionImage.transferTo(tempFile);
      try (InputStream inputStream = new FileInputStream(tempFile)) {
        String contentType = collectionImage.getContentType(); // Get content type from MultipartFile
        s3Service.uploadFile(bucketName, filename, inputStream, contentType);
      }
      tempFile.deleteOnExit();
      System.out.println("File uploaded to S3 successfully.");

      // Generate a presigned URL
      String fileUrl = s3Service.generatePresignedUrl(bucketName, filename);
      System.out.println("Generated S3 URL: " + fileUrl);

      return fileUrl;
    } catch (IOException | RuntimeException e) {
      System.err.println("Error in saveProfilePicture: " + e.getMessage());
      throw new RuntimeException("Failed to upload collection image to S3", e);
    }
  }

  // GET COLLECTIONS FOR CURRENT USER
  public List<CollectionModel> getCollectionsForCurrentUser() {
    // Fetch the current user's ID using UserService
    Long userId = userService.getCurrentUserId();

    // RETURN COLLECTION BY USER ID AND SORTED BY CREATED AT DESC
    return repository.findAllByUserIdOrderByCreatedAtDesc(userId);
  }

  // GET ALL COLLECTIONS ORDERED BY CREATED AT DESC
  public List<CollectionModel> getAllCollectionsByDesc() {
    List<CollectionModel> collections = repository.findAllByOrderByCreatedAtDesc();
    if (collections == null || collections.isEmpty()) {
      throw new CollectionCreationException("No collections found");
    }
    return collections;
  }

  // GET ALL COLLECTIONS BY FOLLOWING
  public List<CollectionModel> getCollectionsByFollowing(Long userId) {
    return repository.findAllByFollowing(userId);
  }

  // DELETE COLLECTION
  public void deleteCollection(Long collectionId) {
    try {
      // Validate and fetch the collection
      CollectionModel collection = getCollectionById(collectionId);
      if (collection == null) {
        throw new CollectionCreationException("Collection not found");
      }

      if (collection.getImageUrl() != null) {
        String oldKey = extractKeyFromUrl(collection.getImageUrl());
        System.out.println("\u001B[32mEDeleting old profile picture: \nshelved-collection-images-benlimpic \n" + oldKey + "\u001B[0m");
        s3Service.deleteFile("shelved-collection-images-benlimpic", oldKey);
      }

      // Delete the collection itself
      repository.delete(collection);
    } catch (NumberFormatException e) {
      throw new CollectionCreationException("Invalid collection ID", e);
    } catch (RuntimeException e) {
      throw new CollectionCreationException("Unexpected error occurred while deleting collection", e);
    }
  }

  // UPDATE COLLECTION
  public Map<String, String> updateCollection(Map<String, String> params, MultipartFile collectionImage)
      throws IOException {
    try {
      // Validate input parameters
      if (params.get("id") == null || params.get("id").isEmpty()) {
        throw new CollectionCreationException("Collection ID is required");
      }

      Long collectionId = Long.valueOf(params.get("id"));
      CollectionModel collection = getCollectionById(collectionId);
      if (collection == null) {
        throw new CollectionCreationException("Collection not found");
      }

      // Update collection properties
      if (params.get("title") != null && !params.get("title").isEmpty()) {
        collection.setTitle(params.get("title"));
      }
      if (params.get("caption") != null && !params.get("caption").isEmpty()) {
        collection.setCaption(params.get("caption"));
      }
      if (collectionImage != null && !collectionImage.isEmpty()) {

        if (collection.getImageUrl() != null) {
          String oldKey = extractKeyFromUrl(collection.getImageUrl());
          System.out.println("\u001B[32mEDeleting old profile picture: \nshelved-collection-images-benlimpic \n" + oldKey + "\u001B[0m");
          s3Service.deleteFile("shelved-collection-images-benlimpic", oldKey);
        }
        
        MultipartFile processedFile = imageService.processImage(collectionImage);
        String imageUrl = saveCollectionImage(processedFile);
        collection.setImageUrl(imageUrl);
      }
      collection.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

      // Save the updated collection
      repository.save(collection);
      return Map.of("message", "Collection updated successfully");
    } catch (NumberFormatException e) {
      throw new CollectionCreationException("Invalid collection ID", e);
    } catch (RuntimeException e) {
      throw new CollectionCreationException("Unexpected error occurred while updating collection", e);
    }
  }

  // GET ALL COLLECTIONS WITH PARTITIONED ITEMS
  public List<CollectionModel> getCollectionsWithPartitionedItems() {
    List<CollectionModel> collections = repository.findAllByOrderByCreatedAtDesc();

    for (CollectionModel collection : collections) {
      // Fetch items for each collection
      List<ItemModel> items = itemService.getAllItemsByCollectionId(collection.getId());
      // Set the items in the collection
      collection.setItems(items);
    }

    return collections;
  }

  // GET ALL COLLECTIONS
  public List<CollectionModel> getAllCollections() {
    List<CollectionModel> collections = repository.findAllByOrderByCreatedAtDesc();
    if (collections == null || collections.isEmpty()) {
      throw new CollectionCreationException("No collections found");
    }
    return collections;
  }

  // GET COLLECTIONS BY USER ID
  public List<CollectionModel> getCollectionsByUserId(Long userId) {
    List<CollectionModel> collections = repository.findAllByUserIdOrderByCreatedAtDesc(userId);
    if (collections == null || collections.isEmpty()) {
      throw new CollectionCreationException("No collections found for user ID: " + userId);
    }
    return collections;
  }

  public List<CollectionModel> searchCollectionsByTitle(String query) {
    return repository.findByTitleContainingIgnoreCase(query);
  }

  public List<CommentModel> getCommentsByCollectionIdDesc(Long collectionId) {
    return commentQueryService.getCommentsByCollectionIdDesc(collectionId);
  }

  public List<CommentModel> getCommentsByCollectionIdAsc(Long collectionId) {
    return commentQueryService.getCommentsByCollectionIdAsc(collectionId);
  }

  public Integer countComments(Long collectionId) {
    return commentQueryService.countByCollectionId(collectionId);
  }

  private String extractKeyFromUrl(String url) {
    int lastSlash = url.lastIndexOf('/');
    return url.substring(lastSlash + 1);
  }
}
