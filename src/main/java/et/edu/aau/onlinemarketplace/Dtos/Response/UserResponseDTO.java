package et.edu.aau.onlinemarketplace.Dtos.Response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private ProfileResponseDTO profile;
    private LocalDateTime createdAt;

    @Data
    public static class ProfileResponseDTO {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String address;
        private String city;
        private String country;
    }
}
