package com.example.bookstore.security;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    public LoginUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            return new LoginUserDetails(user);
        }

        throw new UsernameNotFoundException(username);
    }
    //로그인 하면 username이 이쪽으로 들어옴

}