package com.kratos.footbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kratos.footbackend.dto.LoginUserDto;
import com.kratos.footbackend.dto.RegisterUserDto;
import com.kratos.footbackend.model.Role;
import com.kratos.footbackend.model.User;
import com.kratos.footbackend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AuthenticationService {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserDto input) {
       
        if (userRepository.existsByIdentifiant(input.getIdentifiant())) {
            throw new RuntimeException("Un utilisateur avec cet identifiant existe déjà");
        }

        User user = new User();
        Role userRole = Role.standard;
        if ("admin".equals(input.getRole())) {
            userRole = Role.admin;
        }
       

        user.setIdentifiant(input.getIdentifiant());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setFullName(input.getFullname());   
        user.setEmail(input.getEmail());         
        user.setRole(userRole);       
        user.setStatus("active");                
        user.setJoinedDate(LocalDateTime.now()); 

        return userRepository.save(user);
    }

    
    public User updateUser(Long userIdent, RegisterUserDto input) {
      
    	User user = userRepository.findById(userIdent)
    		    .orElseThrow(() -> new RuntimeException("User not found"));

      
       
        if (input.getPassword() != null && !input.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        }

        user.setFullName(input.getFullname());
        user.setEmail(input.getEmail());

      
        if (input.getRole() != null) {
            user.setRole(input.getRole().equals("admin") ? Role.admin : Role.standard);
        }
        
        user.setJoinedDate(LocalDateTime.now());
      
        return userRepository.save(user);
    }
    
    
    public void deleteUser(Long id) {
      
    	User user = userRepository.findById(id)
    		    .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé avec l'identifiant : " + id);
        }
        
        // Supprime l'utilisateur
        userRepository.delete(user);
    }
    
    
    
    
    public User authenticate(LoginUserDto input) {
        logger.info("Authentication attempt for user: {}", input.getIdentifiant());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    input.getIdentifiant(),
                    input.getPassword()
                )
            );
        } catch (BadCredentialsException ex) {
            logger.warn("Authentication failed for user: {}", input.getIdentifiant());
            return null;
        }

        User authenticatedUser = userRepository.findByIdentifiant(input.getIdentifiant());
        if (authenticatedUser == null) {
            logger.error("Authentication succeeded but user '{}' not found in database", input.getIdentifiant());
            return null;
        }

        authenticatedUser.setLastActive(LocalDateTime.now());
        userRepository.save(authenticatedUser);
        logger.info("User authenticated successfully: {}", input.getIdentifiant());

        return authenticatedUser;
    }

    
    

	public User loadUserByIdentifiant(String identifiant) {
		return userRepository.findByIdentifiant(identifiant);
	}

	public List<User> searchUsersByUsername(String username) {
		logger.info("Searching for users with username containing: {}", username);
		return userRepository.searchByUsername(username);
	}

	public List<User> getAllUsers() {
		logger.info("Fetching all users");
		return userRepository.findAll();
	}
}