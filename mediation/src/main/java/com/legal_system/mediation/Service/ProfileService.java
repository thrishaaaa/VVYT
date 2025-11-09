package com.legal_system.mediation.Service;

import com.legal_system.mediation.model.UserDetails;
import com.legal_system.mediation.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private UserDetailsRepository userRepository;

    public UserDetails getUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public void updateProfile(UserDetails userDetails) {
        UserDetails existingUser = getUserById(userDetails.getId());
        existingUser.setName(userDetails.getName());
        existingUser.setPhone_no(userDetails.getPhone_no());
        existingUser.setAddress(userDetails.getAddress());
        existingUser.setCity(userDetails.getCity());
        existingUser.setState(userDetails.getState());
        existingUser.setPincode(userDetails.getPincode());
        userRepository.save(existingUser);
    }

    public void deleteAccount(int userId) {
        UserDetails user = getUserById(userId);
        userRepository.delete(user);
    }
}
