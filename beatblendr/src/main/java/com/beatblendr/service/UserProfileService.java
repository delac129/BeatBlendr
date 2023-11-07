package com.beatblendr.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beatblendr.entity.UserDetailsRepository;
import com.beatblendr.entity.UserDetails;
import se.michaelthelin.spotify.model_objects.specification.User;


@Service
public class UserProfileService {

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    public UserDetails insertOrUpdateUserDetails(User user, String accessToken, String refreshToken, long expirationTimeMillis){
        UserDetails userDetails = userDetailsRepository.findByRefId(user.getId());

        if(Objects.isNull(userDetails)){
            userDetails = new UserDetails();
        }

        userDetails.setUserName(user.getDisplayName());
        userDetails.setEmailId(user.getEmail());
        userDetails.setAccessToken(accessToken);
        userDetails.setRefreshToken(refreshToken);
        userDetails.setRefId(user.getId());
        userDetails.setExpirationTimeMillis(expirationTimeMillis);
        return userDetailsRepository.save(userDetails);
    }
    
}
