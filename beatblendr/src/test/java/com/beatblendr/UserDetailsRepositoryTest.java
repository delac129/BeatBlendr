package com.beatblendr;

import org.springframework.boot.test.context.SpringBootTest;

import com.beatblendr.entity.UserDetails;
import com.beatblendr.entity.UserDetailsRepository;

import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
public class UserDetailsRepositoryTest {
    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Test
    public void testFindByRefId() throws ParseException, SpotifyWebApiException, IOException {

        UserDetails user = userDetailsRepository.findByRefId("abdulrahim24");
        assertNotNull(user, "Result should not be null");
    }
}


