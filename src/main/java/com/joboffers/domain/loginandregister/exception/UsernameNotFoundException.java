package com.joboffers.domain.loginandregister.exception;

public class UsernameNotFoundException extends  RuntimeException{
    public UsernameNotFoundException(String userNotFound) {
        super(userNotFound);
    }
}
