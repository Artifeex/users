package ru.sandr.users.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.sandr.users.security.utils.CustomUserDetails;
import ru.sandr.users.user.service.UserService;

@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * @param loginInput может быть как username(табельный номер/номер зачетки/admin), так и email
     * @return CustomUserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        var user = userService.getUserByUsernameOrEmail(loginInput)
                              .orElseThrow(() -> new UsernameNotFoundException(loginInput));
        return new CustomUserDetails(user);
    }
}
