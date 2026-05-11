package com.todo.user_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@JsonPropertyOrder({
        "id",
        "fullName",
        "email",
        "accessToken",
        "refreshToken",
        "avatarUrl",
        "isNewUser",
        "profileComplete"
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAuthResponseDto {

    private String userId;
    private String fullName;
    private String email;
    private String accessToken;
    private String refreshToken;
    private String avatarUrl;
    private boolean isNewUser;
    /**
     * False if Google did not provide a fullName or the user hasn't completed their profile.
     * Frontend should redirect to /complete-profile when this is false.
     */
    private boolean profileComplete;

}