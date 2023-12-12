package com.beatblendr.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer> {
    //finding a user by their refID;
    UserDetails findByRefId(String refId);
}
