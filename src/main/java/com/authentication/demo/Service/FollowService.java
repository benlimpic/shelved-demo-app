package com.authentication.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.authentication.demo.Model.FollowModel;
import com.authentication.demo.Model.UserModel;
import com.authentication.demo.Repository.FollowRepository;

@Service

public class FollowService {

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    public void followUser(UserModel follower, UserModel followed) {
        FollowModel follow = new FollowModel(follower, followed);
        followRepository.save(follow);
    }

    public boolean isFollowing(UserModel follower, UserModel followed) {
        return followRepository.existsByFollowerAndFollowed(follower, followed);
    }

    public void unfollowUser(UserModel follower, UserModel followed) {
        FollowModel follow = followRepository.findByFollowerAndFollowed(follower, followed)
            .orElseThrow(() -> new IllegalArgumentException("Follow relationship not found"));
        followRepository.delete(follow);
    }

    public List<UserModel> getFollowers(UserModel user) {
        return followRepository.findByFollowed(user).stream()
            .map(FollowModel::getFollower)
            .toList();
    }

    public List<UserModel> getFollowing(UserModel user) {
        return followRepository.findByFollower(user).stream()
            .map(FollowModel::getFollowed)
            .toList();
    }

    public int countByFollowed(UserModel user) {
        return (int) followRepository.countByFollowed(user);
    }

    public int countByFollower(UserModel user) {
        return (int) followRepository.countByFollower(user);
    }
}