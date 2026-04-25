package com.kratos.footbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kratos.footbackend.model.User;
import com.kratos.footbackend.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        User user = userRepository.findByIdentifiant(identifiant);
        
        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'identifiant: " + identifiant);
        }
        
        // Créer les autorités/rôles
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (user.getRole() != null && "admin".equals(user.getRole().name())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getIdentifiant())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}