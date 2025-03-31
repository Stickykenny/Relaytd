package me.stky.relaytd.api.service;

import me.stky.relaytd.api.model.User;
import me.stky.relaytd.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository UserRepository;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user  = UserRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new User(user.getId(), user.getUsername(), user.getPassword(), "ROLE_"+user.getRole());
    }


}