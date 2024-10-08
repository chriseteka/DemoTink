package com.chrisworks.user;

public interface UserDetailsRetriever {

    UserDetails fetchUserRegistrationByUserId(String userId);
}
