package et.edu.aau.onlinemarketplace.Service;

import et.edu.aau.onlinemarketplace.Dtos.Request.UserRequestDTO;
import et.edu.aau.onlinemarketplace.Dtos.Response.UserResponseDTO;
import et.edu.aau.onlinemarketplace.Entity.User;
import et.edu.aau.onlinemarketplace.Entity.Profile;
import et.edu.aau.onlinemarketplace.Exception.ResourceNotFoundException;
import et.edu.aau.onlinemarketplace.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(UserRequestDTO request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        // Create profile
        Profile profile = new Profile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setCountry(request.getCountry());
        profile.setUser(user);

        user.setProfile(profile);

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return convertToDTO(user);
    }

    public UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return convertToDTO(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update user fields
        user.setEmail(request.getEmail());

        // Update profile
        Profile profile = user.getProfile();
        if (profile != null) {
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setPhoneNumber(request.getPhoneNumber());
            profile.setAddress(request.getAddress());
            profile.setCity(request.getCity());
            profile.setCountry(request.getCountry());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        if (user.getProfile() != null) {
            UserResponseDTO.ProfileResponseDTO profileDTO = new UserResponseDTO.ProfileResponseDTO();
            profileDTO.setFirstName(user.getProfile().getFirstName());
            profileDTO.setLastName(user.getProfile().getLastName());
            profileDTO.setPhoneNumber(user.getProfile().getPhoneNumber());
            profileDTO.setAddress(user.getProfile().getAddress());
            profileDTO.setCity(user.getProfile().getCity());
            profileDTO.setCountry(user.getProfile().getCountry());
            dto.setProfile(profileDTO);
        }

        return dto;
    }
}