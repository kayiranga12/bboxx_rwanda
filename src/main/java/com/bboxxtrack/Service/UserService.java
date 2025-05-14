package com.bboxxtrack.Service;

import com.bboxxtrack.Model.User;
import com.bboxxtrack.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

//    public User getUserById(Long id) {
//        return userRepository.findById(id).orElse(null);
//    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /** new: lookup by id **/
    public User getUserById(Long id) {
        Optional<User> u = userRepository.findById(id);
        return u.orElse(null);

}
}
