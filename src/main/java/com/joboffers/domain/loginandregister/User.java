package com.joboffers.domain.loginandregister;

import lombok.Builder;

@Builder
public record User(String id, String username, String password) {
}
