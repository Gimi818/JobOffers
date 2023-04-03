package com.joboffers.domain.loginandregister;

import com.joboffers.domain.loginandregister.dto.RegisterUserDto;
import com.joboffers.domain.loginandregister.dto.RegistrationResultDto;
import com.joboffers.domain.loginandregister.dto.UserDto;
import com.joboffers.domain.loginandregister.exception.UsernameNotFoundException;
import lombok.AllArgsConstructor;

import static com.joboffers.domain.loginandregister.exception.UserMapper.mapper;
@AllArgsConstructor
public class LoginAndRegisterFacade {
    private static final String USER_NOT_FOUND = "User not found";

    private final LoginRepository repository;

    public UserDto findByUsername(String username) {
        return repository.findByUsername(username)
                .map(mapper::mapFromUserToUserDto)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    }


    public RegistrationResultDto register(RegisterUserDto registerUserDto) {
        final User user = mapper.mapFromRegistrationUserDtoToUser(registerUserDto);
        User savedUser = repository.save(user);
        return new RegistrationResultDto(savedUser.id(), true, savedUser.username());
    }

}
