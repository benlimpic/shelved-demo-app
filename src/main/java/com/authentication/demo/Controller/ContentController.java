package com.authentication.demo.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.ListUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.authentication.demo.Interface.PopularEntry;
import com.authentication.demo.Model.CollectionModel;
import com.authentication.demo.Model.CommentModel;
import com.authentication.demo.Model.ItemModel;
import com.authentication.demo.Model.LikeModel;
import com.authentication.demo.Model.ReplyModel;
import com.authentication.demo.Model.UserModel;
import com.authentication.demo.Repository.LikeRepository;
import com.authentication.demo.Repository.UserRepository;
import com.authentication.demo.Service.CollectionService;
import com.authentication.demo.Service.CommentService;
import com.authentication.demo.Service.FollowService;
import com.authentication.demo.Service.ItemService;
import com.authentication.demo.Service.LikeService;
import com.authentication.demo.Service.PopularService;
import com.authentication.demo.Service.UserService;

@Controller
public class ContentController {

    private final UserRepository repository;
    private final UserService userService;
    private final CollectionService collectionService;
    private final ItemService itemService;
    private final FollowService followService;
    private final LikeService likeService;
    private final LikeRepository likeRepository;
    private final CommentService commentService;
    private final PopularService popularService;

    public ContentController(UserRepository repository, UserService userService, CollectionService collectionService,
            ItemService itemService, FollowService followService, LikeService likeService,
            LikeRepository likeRepository,
            CommentService commentService, PopularService popularService) {
        this.popularService = popularService;
        this.repository = repository;
        this.userService = userService;
        this.collectionService = collectionService;
        this.itemService = itemService;
        this.followService = followService;
        this.likeService = likeService;
        this.likeRepository = likeRepository;
        this.commentService = commentService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping(path = {"/index"})
    public String index(Model model) {

        // Fetch current user
        Optional<UserModel> currentUserOptional = userService.getCurrentUser();
        if (currentUserOptional.isEmpty()) {
            return "redirect:/login"; // Redirect to login if user is not authenticated
        }
        UserModel currentUser = currentUserOptional.get();

        // Fetch Users
        Map<Long, UserModel> users = userService.getUsersMappedById();
        model.addAttribute("users", users);

        // Fetch collections by following
        List<CollectionModel> collections = collectionService.getCollectionsByFollowing(currentUser.getId());

        // Partition items for each collection
        Map<Long, List<List<ItemModel>>> partitionedItemsByCollection = new HashMap<>();
        for (CollectionModel collection : collections) {
            // Fetch the collection owner
            UserModel collectionOwner = userService.getUserById(collection.getUser().getId());
            if (collectionOwner != null) {
                collection.setUser(collectionOwner); // Set the owner in the collection
            }

            // Fetch the number of likes for the collection
            collection.setLikeCount(likeService.countLikes(collection.getId()));

            // NUMBER OF COMMENTS AND REPLIES
            List<CommentModel> comments = collectionService.getCommentsByCollectionIdAsc(collection.getId());
            int commentReplyCount = 0;
            for (CommentModel comment : comments) {
                List<ReplyModel> replies = comment.getReplies();
                commentReplyCount += replies.size() + 1;
            }

            collection.setCommentCount(commentReplyCount);

            // Fetch the isLiked status for the collection
            List<LikeModel> likes = likeRepository.findAllByCollectionId(collection.getId());
            collection.setIsLiked(likes.stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId())));

            // Fetch items for each collection
            List<ItemModel> items = itemService.getAllItemsByCollectionId(collection.getId());
            List<List<ItemModel>> partitionedItems = items != null ? ListUtils.partition(items, 3) : new ArrayList<>();
            partitionedItemsByCollection.put(collection.getId(), partitionedItems);
        }

        // Add data to the model
        model.addAttribute("collections", collections);
        model.addAttribute("partitionedItemsByCollection", partitionedItemsByCollection);
        model.addAttribute("users", users);

        return handleAuthentication(model, "index");
    }

    @GetMapping("/profile")
    public String profile(Model model) {

        // FETCH CURRENT USER
        UserModel currentUser = userService.getCurrentUser().orElse(null);
        if (currentUser == null) {
            return "redirect:/login"; // Redirect to login if user is not authenticated
        }

        // FETCH SORTED COLLECTIONS BY USER ID
        List<CollectionModel> collections = collectionService.getCollectionsForCurrentUser();

        // BREAK COLLECTIONS INTO GROUPS OF 3
        List<List<CollectionModel>> partitionedCollections = ListUtils.partition(collections, 3);

        // COUNT COLLECTIONS
        int collectionCount = collections.size();

        int followingCount = followService.countByFollower(currentUser);
        int followersCount = followService.countByFollowed(currentUser);

        model.addAttribute("followingCount", followingCount);
        model.addAttribute("followersCount", followersCount);

        // ADD TO MODEL
        model.addAttribute("collectionCount", collectionCount);
        model.addAttribute("partitionedCollections", partitionedCollections);
        return handleAuthentication(model, "profile");
    }

    @GetMapping("/profile/{id}")
    public String userProfile(@PathVariable("id") Long userId, Model model) {

        UserModel currentUser = userService.getCurrentUser().orElse(null);
        UserModel userProfile = userService.getUserById(userId);

        Boolean isFollwing = followService.isFollowing(currentUser, userProfile);

        // Fetch the user profile
        if (userProfile == null) {
            return "redirect:/index"; // Redirect to index if user profile is not found
        } else if (userProfile.getId().equals(userService.getCurrentUserId())) {
            return "redirect:/profile"; // Redirect to own profile
        }

        int followingCount = followService.countByFollower(userProfile);
        int followersCount = followService.countByFollowed(userProfile);

        model.addAttribute("followingCount", followingCount);
        model.addAttribute("followersCount", followersCount);

        // Fetch collections for the user
        List<CollectionModel> collections = collectionService.getCollectionsByUserId(userId);
        // Partition collections into rows of 3 for display
        List<List<CollectionModel>> partitionedCollections = ListUtils.partition(collections, 3);
        // Count Collections
        int collectionCount = collections.size();
        // Add the attributes to model
        model.addAttribute("isFollowing", isFollwing);
        model.addAttribute("collectionCount", collectionCount);
        model.addAttribute("partitionedCollections", partitionedCollections);
        model.addAttribute("userProfile", userProfile);

        return handleAuthentication(model, "userProfile");
    }

    @GetMapping("/profile/{id}/following")
    public String getUserFollowing(@PathVariable Long id, Model model) {
        UserModel userProfile = userService.getUserById(id);
        if (userProfile == null) {
            return "redirect:/index"; // Handle missing user profile
        }

        List<UserModel> followingList = followService.getFollowing(userProfile);
        model.addAttribute("followingList", followingList);

        model.addAttribute("userProfile", userProfile);

        return handleAuthentication(model, "followingList");
    }

    @GetMapping("/profile/{id}/followers")
    public String getUserFollowers(@PathVariable Long id, Model model) {
        UserModel userProfile = userService.getUserById(id);
        if (userProfile == null) {
            return "redirect:/index"; // Handle missing user profile
        }

        List<UserModel> followersList = followService.getFollowers(userProfile);
        model.addAttribute("followersList", followersList);

        model.addAttribute("userProfile", userProfile);

        return handleAuthentication(model, "followersList");
    }

    @GetMapping("/update-profile")
    public String updateProfile(Model model) {
        return handleAuthentication(model, "updateProfile");
    }

    @GetMapping("/update-user-details")
    public String updateUserDetails(Model model) {
        return handleAuthentication(model, "updateUserDetails");
    }

    @GetMapping("/create-collection")
    public String createCollection(Model model) {
        return handleAuthentication(model, "createCollection");
    }

    @GetMapping("/collection/{id}")
    public String collection(@PathVariable("id") Long collectionId, Model model) {

        // Fetch current user
        Optional<UserModel> currentUserOptional = userService.getCurrentUser();
        if (currentUserOptional.isEmpty()) {
            return "redirect:/login"; // Redirect to login if user is not authenticated
        }
        UserModel currentUser = currentUserOptional.get();

        // Fetch the collection by ID
        CollectionModel collection = collectionService.getCollectionById(collectionId);
        if (collection == null) {
            return "redirect:/profile"; // Redirect if the collection is not found
        }

        // Fetch the collection owner by ID
        UserModel userProfile = userService.getUserById(collection.getUser().getId());
        if (userProfile == null) {
            return "redirect:/profile"; // Redirect if the collection owner is not found
        }

        // Fetch items for the collection
        List<ItemModel> items = itemService.getAllItemsByCollectionId(collectionId);

        // Partition items into rows of 3 for display
        List<List<ItemModel>> partitionedItems = items != null ? ListUtils.partition(items, 3) : List.of();

        // Is the current user the owner of the collection?
        if (collection.getUser().getId().equals(userService.getCurrentUserId())) {
            collection.setIsOwner(true);
        } else {
            collection.setIsOwner(false);
        }

        // Fetch the number of likes for the collection
        Integer likeCount = likeService.countLikes(collectionId);
        collection.setLikeCount(likeCount);

        // Fetch the isLiked status for the collection
        List<LikeModel> likes = likeRepository.findAllByCollectionId(collectionId);
        if (likes.stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId()))) {
            collection.setIsLiked(true);
        } else {
            collection.setIsLiked(false);
        }

        collection.setComments(collectionService.getCommentsByCollectionIdAsc(collectionId));

        List<CommentModel> comments = collection.getComments();
        int commentReplyCount = 0;

        for (CommentModel comment : comments) {

            commentReplyCount += 1;

            List<ReplyModel> replies = comment.getReplies();
            commentReplyCount += replies.size();
        }

        collection.setCommentCount(commentReplyCount);

        // Add data to the model
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("user", currentUser);
        model.addAttribute("collection", collection);
        model.addAttribute("partitionedItems", partitionedItems);

        return handleAuthentication(model, "collection");
    }

    @GetMapping("/collection/{id}/comments")
    public String collectionComment(@PathVariable("id") Long collectionId, Model model) {

        // Fetch current user
        Optional<UserModel> currentUserOptional = userService.getCurrentUser();
        if (currentUserOptional.isEmpty()) {
            return "redirect:/login"; // Redirect to login if user is not authenticated
        }
        UserModel currentUser = currentUserOptional.get();

        // Fetch the collection by ID
        CollectionModel collection = collectionService.getCollectionById(collectionId);
        if (collection == null) {
            return "redirect:/profile"; // Redirect if the collection is not found
        }

        // Fetch the collection owner by ID
        UserModel userProfile = userService.getUserById(collection.getUser().getId());
        if (userProfile == null) {
            return "redirect:/profile"; // Redirect if the collection owner is not found
        }

        // Fetch items for the collection
        List<ItemModel> items = itemService.getAllItemsByCollectionId(collectionId);

        // Partition items into rows of 3 for display
        List<List<ItemModel>> partitionedItems = items != null ? ListUtils.partition(items, 3) : List.of();

        // Is the current user the owner of the collection?
        if (collection.getUser().getId().equals(userService.getCurrentUserId())) {
            collection.setIsOwner(true);
        } else {
            collection.setIsOwner(false);
        }

        // Fetch the number of likes for the collection
        Integer likeCount = likeService.countLikes(collectionId);
        collection.setLikeCount(likeCount);



        // Fetch the isLiked status for the collection
        List<LikeModel> likes = likeRepository.findAllByCollectionId(collectionId);
        if (likes.stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId()))) {
            collection.setIsLiked(true);
        } else {
            collection.setIsLiked(false);
        }

        collection.setComments(collectionService.getCommentsByCollectionIdAsc(collectionId));

        int commentReplyCount = 0;

        for (CommentModel comment : collection.getComments()) {

            // isOwner
            UserModel commentOwner = comment.getUser();
            if (commentOwner != null) {
                if (commentOwner.getId().equals(currentUser.getId())) {
                    comment.setIsOwner(true);
                } else {
                    comment.setIsOwner(false);
                }
            }

            // Fetch the number of likes for the comment
            Integer commentLikeCount = likeService.countLikesComment(comment.getId());
            comment.setLikeCount(commentLikeCount);

            // Fetch the isLiked status for the comment
            List<LikeModel> commentLikes = likeRepository.findAllByCommentId(comment.getId());
            boolean isCommentLiked = commentLikes.stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
            comment.setIsLiked(isCommentLiked);

            System.out.println("Comment: " + comment.getId());

            // Fetch replies for the comment
            List<ReplyModel> replies = comment.getReplies();
            comment.setReplies(replies);

            // COMMENT AND REPLY COUNT
            commentReplyCount += comment.getReplies().size() + 1;

            // Fetch replies for the comment
            for (ReplyModel reply : replies) {
                UserModel replyOwner = reply.getUser();
                if (replyOwner != null) {
                    if (replyOwner.getId().equals(currentUser.getId())) {
                        reply.setIsOwner(true);
                    } else {
                        reply.setIsOwner(false);
                    }
                }

                // Fetch the isLiked status for the comment
                List<LikeModel> replyLikes = likeRepository.findAllByReplyId(reply.getId());
                boolean isReplyLiked = replyLikes.stream()
                        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
                reply.setIsLiked(isReplyLiked);

                // Fetch the number of likes for the reply
                Integer replyLikeCount = likeService.countLikesReply(reply.getId());
                reply.setLikeCount(replyLikeCount);
            }

        }

        collection.setCommentCount(commentReplyCount);

        // Add data to the model
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("user", currentUser);
        model.addAttribute("collection", collection);
        model.addAttribute("partitionedItems", partitionedItems);

        return handleAuthentication(model, "collectionComment");
    }

    @GetMapping("/create-item/{collectionId}")
    public String createItem(@PathVariable("collectionId") Long collectionId, Model model) {
        // Add the collectionId to the model
        model.addAttribute("collectionId", collectionId);
        return handleAuthentication(model, "createItem");
    }

    @GetMapping("/item/{id}")
    public String getItem(@PathVariable("id") Long itemId, Model model) {

        // Fetch current user
        Optional<UserModel> currentUserOptional = userService.getCurrentUser();
        if (currentUserOptional.isEmpty()) {
            return "redirect:/login"; // Redirect to login if user is not authenticated
        }
        UserModel currentUser = currentUserOptional.get();

        // Fetch the item by ID
        ItemModel item = itemService.getItemById(itemId);
        if (item == null) {
            return "redirect:/profile"; // Redirect if the item is not found
        }

        // Fetch the collection associated with the item
        CollectionModel collection = collectionService.getCollectionById(item.getCollection().getId());
        if (collection == null) {
            return "redirect:/profile"; // Redirect if the collection is not found
        }

        // Fetch the item owner by ID
        UserModel userProfile = userService.getUserById(item.getUserId());
        if (userProfile == null) {
            return "redirect:/profile"; // Redirect if the item owner is not found
        }

        // Is the current user the owner of the item?
        if (item.getUserId().equals(currentUser.getId())) {
            item.setIsOwner(true);
        } else {
            item.setIsOwner(false);
        }

        // Fetch the number of likes for the item
        if (item.getId() != null) {
            item.setLikeCount(likeService.countLikesItem(item.getId()));
        } else {
            item.setLikeCount(0);
        }

        // Fetch the isLiked status for the item
        List<LikeModel> likes = likeRepository.findAllByItemId(itemId);
        if (likes.stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId()))) {
            item.setIsLiked(true);
        } else {
            item.setIsLiked(false);
        }

        List<CommentModel> comments = commentService.getCommentsByItemId(itemId);
        item.setComments(comments); // Ensure comments are set on the item
        int commentReplyCount = 0;

        for (CommentModel comment : comments) {

            List<ReplyModel> replies = comment.getReplies();
            commentReplyCount += replies.size() + 1;
        }

        item.setCommentCount(commentReplyCount);

        model.addAttribute("user", currentUser);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("item", item);
        model.addAttribute("collection", collection);

        return handleAuthentication(model, "item");
    }

    @GetMapping("/item/{id}/comments")
    public String getItemComment(@PathVariable("id") Long itemId, Model model) {

        // Fetch current user
        Optional<UserModel> currentUserOptional = userService.getCurrentUser();
        if (currentUserOptional.isEmpty()) {
            return "redirect:/login"; // Redirect to login if user is not authenticated
        }
        UserModel currentUser = currentUserOptional.get();

        // Fetch the item by ID
        ItemModel item = itemService.getItemById(itemId);
        if (item == null) {
            return "redirect:/profile"; // Redirect if the item is not found
        }

        // Fetch the collection associated with the item
        CollectionModel collection = item.getCollection();
        if (collection == null) {
            return "redirect:/profile"; // Redirect if the collection is not found
        }

        // Fetch the item owner by ID
        UserModel userProfile = userService.getUserById(item.getUserId());
        if (userProfile == null) {
            return "redirect:/profile"; // Redirect if the item owner is not found
        }

        // Is the current user the owner of the item?
        if (item.getUserId().equals(userService.getCurrentUserId())) {
            item.setIsOwner(true);
        } else {
            item.setIsOwner(false);
        }

        // Fetch the number of likes for the item
        if (item.getId() != null) {
            item.setLikeCount(likeService.countLikesItem(item.getId()));
        } else {
            item.setLikeCount(0);
        }

        // Fetch the isLiked status for the item
        List<LikeModel> likes = likeRepository.findAllByItemId(itemId);
        if (likes.stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId()))) {
            item.setIsLiked(true);
        } else {
            item.setIsLiked(false);
        }

        item.setComments(commentService.getCommentsByItemId(itemId));

        int commentReplyCount = 0;
        for (CommentModel comment : item.getComments()) {

            // isOwner
            UserModel commentOwner = comment.getUser();
            if (commentOwner != null) {
                if (commentOwner.getId().equals(currentUser.getId())) {
                    comment.setIsOwner(true);
                } else {
                    comment.setIsOwner(false);
                }
            }

            // Fetch the number of likes for the comment
            Integer commentLikeCount = likeService.countLikesComment(comment.getId());
            comment.setLikeCount(commentLikeCount);

            // Fetch the isLiked status for the comment
            List<LikeModel> commentLikes = likeRepository.findAllByCommentId(comment.getId());
            boolean isCommentLiked = commentLikes.stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
            comment.setIsLiked(isCommentLiked);

            System.out.println("Comment: " + comment.getId());

            // Fetch replies for the comment
            List<ReplyModel> replies = comment.getReplies();
            commentReplyCount += replies.size() + 1;

            for (ReplyModel reply : replies) {
                UserModel replyOwner = reply.getUser();
                if (replyOwner != null) {
                    if (replyOwner.getId().equals(currentUser.getId())) {
                        reply.setIsOwner(true);
                    } else {
                        reply.setIsOwner(false);
                    }
                }
                // Fetch the number of likes for the reply
                Integer replyLikeCount = likeService.countLikesReply(reply.getId());
                reply.setLikeCount(replyLikeCount);
                // Fetch the isLiked status for the reply
                List<LikeModel> replyLikes = likeRepository.findAllByReplyId(reply.getId());
                boolean isReplyLiked = replyLikes.stream()
                        .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
                reply.setIsLiked(isReplyLiked);
            }

        }
        item.setCommentCount(commentReplyCount);

        // Add the item and item to the model
        model.addAttribute("user", currentUser);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("item", item);
        model.addAttribute("collection", collection);

        return handleAuthentication(model, "itemComment");
    }

    @GetMapping("/update-item/{id}")
    public String updateItem(@PathVariable("id") Long itemId, Model model) {
        ItemModel item = itemService.getItemById(itemId);
        if (item == null) {
            return "redirect:/profile"; // Handle missing item
        }
        model.addAttribute("item", item);
        return handleAuthentication(model, "updateItem");
    }

    @GetMapping("/update-collection/{id}")
    public String updateCollection(@PathVariable("id") Long collectionId, Model model) {
        // Fetch the collection by ID
        CollectionModel collection = collectionService.getCollectionById(collectionId);
        if (collection == null) {
            return "redirect:/profile"; // Redirect if the collection is not found
        }

        // Add the collection to the model
        model.addAttribute("collection", collection);

        return handleAuthentication(model, "updateCollection");
    }

    @GetMapping("/popular")
    public String popular(Model model) {
        List<PopularEntry> popularEntries = popularService.getTopPopularEntries();
        if (popularEntries == null) {
            popularEntries = new ArrayList<>();
        }
        List<List<PopularEntry>> partitioned = ListUtils.partition(popularEntries, 3);
        model.addAttribute("popularEntries", popularEntries);
        model.addAttribute("partitionedEntries", partitioned);
        return handleAuthentication(model, "popular");
    }

    private String handleAuthentication(Model model, String viewName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null && !authentication.getName().isEmpty()) {
            Optional<UserModel> user = repository.findByUsername(authentication.getName());
            if (user.isPresent()) {
                UserModel userModel = user.get();
                model.addAttribute("user", userModel);
                return viewName;
            }
        }
        return "redirect:/login";
    }

}
