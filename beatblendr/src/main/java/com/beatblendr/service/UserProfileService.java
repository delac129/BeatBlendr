package com.beatblendr.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beatblendr.entity.UserDetailsRepository;
import com.beatblendr.entity.UserDetails;
import se.michaelthelin.spotify.model_objects.specification.User;


@Service
public class UserProfileService {

    // @Autowired used to inject the UserDetailsRepository by dependency injection
    // this repository is used for data access on UserDetails entities
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    // this method is to insert a new user's details or update existing ones
    // it takes in a User object, access and refresh tokens, and expiration time for the access token as parameters.
    public UserDetails insertOrUpdateUserDetails(User user, String accessToken, String refreshToken, long expirationTimeMillis){
        UserDetails userDetails = userDetailsRepository.findByRefId(user.getId());

        if(Objects.isNull(userDetails)){
            userDetails = new UserDetails();
        }

        // update the userDetails object with information from the user object and tokens
        userDetails.setUserName(user.getDisplayName());
        userDetails.setEmailId(user.getEmail());
        userDetails.setAccessToken(accessToken);
        userDetails.setRefreshToken(refreshToken);
        userDetails.setRefId(user.getId());
        userDetails.setExpirationTimeMillis(expirationTimeMillis);
        // save the userDetails object to the database and return it.
        return userDetailsRepository.save(userDetails);
    }
    
}
