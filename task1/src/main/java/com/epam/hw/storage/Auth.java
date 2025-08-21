package com.epam.hw.storage;

import com.epam.hw.entity.User;
import com.epam.hw.repository.UserRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Getter
public class Auth {
    private static final Logger logger = LoggerFactory.getLogger(Auth.class);

    private User loggedInUser = null;

    private final UserRepository userRepository;

    @Autowired
    public Auth(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean logIn(String username, String password) {
        logger.debug("Attempting login for username '{}'", username);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(password)) {
                if (user.getTrainee() != null || user.getTrainer() != null) {
                    loggedInUser = user;
                    logger.info("User '{}' logged in successfully", username);
                    return true;
                } else {
                    logger.warn("User '{}' login failed: not a trainee or trainer", username);
                }
            } else {
                logger.warn("User '{}' login failed: incorrect password", username);
            }
        } else {
            logger.warn("User '{}' login failed: user not found", username);
        }
        return false;
    }


    public boolean logOut() {
        if (loggedInUser != null) {
            logger.info("User '{}' logged out", loggedInUser.getUsername());
            loggedInUser = null;
            return true;
        }
        logger.debug("Logout attempted but no user was logged in");
        return false;
    }

}
