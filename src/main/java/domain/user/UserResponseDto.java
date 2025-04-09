package domain.user;

import java.time.LocalDateTime;
import domain.user.User.UserProvider;
import domain.user.User.UserStatus;
import lombok.Builder;

public record UserResponseDto(
    UserProvider userProvider,
    String email,
    String name,
    String birth,
    String phone,
    UserStatus userStatus,
    boolean marketingReceivedStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
    ) {
  
    @Builder
    public UserResponseDto {}

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
            .userProvider(user.getUserProvider())
            .email(user.getEmail())
            .name(user.getName())
            .birth(user.getBirth())
            .phone(user.getPhone())
            .userStatus(user.getUserStatus())
            .marketingReceivedStatus(user.getMarketingReceivedStatus())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
