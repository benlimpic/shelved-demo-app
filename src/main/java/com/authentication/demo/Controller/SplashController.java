package com.authentication.demo.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.ListUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.authentication.demo.Model.CollectionModel;
import com.authentication.demo.Model.CommentModel;
import com.authentication.demo.Model.ItemModel;
import com.authentication.demo.Model.LikeModel;
import com.authentication.demo.Model.ReplyModel;
import com.authentication.demo.Model.UserModel;
import com.authentication.demo.Repository.LikeRepository;
import com.authentication.demo.Repository.UserRepository;
import com.authentication.demo.Service.CollectionService;
import com.authentication.demo.Service.ItemService;
import com.authentication.demo.Service.LikeService;
import com.authentication.demo.Service.UserService;


@Controller
@Profile("prod")
public class SplashController {

    private final UserRepository repository;
    private final UserService userService;
    private final CollectionService collectionService;
    private final ItemService itemService;
    private final LikeService likeService;
    private final LikeRepository likeRepository;

    public SplashController(
        UserRepository repository,
        UserService userService,
        CollectionService collectionService,
        ItemService itemService,
        LikeService likeService,
        LikeRepository likeRepository
        ) {
        this.repository = repository;
        this.userService = userService;
        this.collectionService = collectionService;
        this.itemService = itemService;
        this.likeService = likeService;
        this.likeRepository = likeRepository;
    }

    @GetMapping("/")
    public String splash(Model model) {

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




