package vsu.cs.is.infsysserver.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.security.entity.temp.UserRepository;
import vsu.cs.is.infsysserver.security.util.UserMapper;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("Пизда, не нашли такого юзера"));
        return UserMapper.mapUserToUserDetails(user);
    }
}
