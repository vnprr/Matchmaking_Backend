package com.matchmaking.backend.service.user.profile;


import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.user.profile.UserProfileContextDTO;
import com.matchmaking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileContextService {

    private final UserService userService;

    /**
     * Sprawdza kontekst dostępu do profilu użytkownika o podanym ID
     * @param profileUserId ID użytkownika, którego profil jest sprawdzany
     * @return DTO z informacjami o uprawnieniach
     */
    @Transactional(readOnly = true)
    public UserProfileContextDTO getProfileContext(Long profileUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Użytkownik niezalogowany
        if ("anonymousUser".equals(email)) {
            return UserProfileContextDTO.builder()
                    .userId(profileUserId)
                    .editable(false)
                    .viewable(false)
                    .build();
        }

        User currentUser = userService.getUserByEmail(email);

        // Właściciel profilu
        boolean isOwner = currentUser.getId().equals(profileUserId);

        // Administrator
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        return UserProfileContextDTO.builder()
                .userId(profileUserId)
                .editable(isOwner || isAdmin)
                .viewable(true)
                .build();
    }

    public UserProfileContextDTO getCurrentUserProfileContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Użytkownik niezalogowany
        if ("anonymousUser".equals(email)) {
            return UserProfileContextDTO.builder()
                    .userId(null)
                    .editable(false)
                    .viewable(false)
                    .build();
        }

        User currentUser = userService.getUserByEmail(email);

        // Właściciel profilu
        return UserProfileContextDTO.builder()
                .userId(currentUser.getId())
                .editable(true)
                .viewable(true)
                .build();
    }
}