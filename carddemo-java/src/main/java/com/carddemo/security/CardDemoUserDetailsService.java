package com.carddemo.security;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CardDemoUserDetailsService implements UserDetailsService {
    private final UserSecurityRepository userSecurityRepository;

    public CardDemoUserDetailsService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserSecurity user = userSecurityRepository.findById(username.toUpperCase())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        String role = "A".equals(user.getUsrType()) ? "ADMIN" : "USER";
        return new User(user.getUsrId(), user.getUsrPwd(),
            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
