package com.matchmaking.backend.service.user.profile;

import com.matchmaking.backend.model.Role;
import com.matchmaking.backend.model.User;
import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.user.profile.UserProfileContextDTO;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileContextService {

    private final UserService userService;
    private final UserProfileRepository userProfileRepository;

    /**
     * Pobiera kontekst profilu dla danego ID profilu
     * @param profileId ID profilu
     * @return kontekst profilu (informacje o dostępie)
     */
    @Transactional(readOnly = true)
    public UserProfileContextDTO getProfileContext(Long profileId) {
        // Pobierz bieżącego użytkownika
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByEmail(currentUserEmail);

        // Pobierz profil
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profil o podanym ID nie istnieje"));

        UserProfileContextDTO context = new UserProfileContextDTO();
        context.setProfileId(profile.getId());
        //context.setUserId(profile.getUser().getId());

        // Użytkownik może edytować profil, jeśli jest jego właścicielem lub administratorem
        boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        context.setEditable(isOwner || isAdmin);
        context.setOwner(isOwner);
        context.setViewable(true);

        return context;
    }

    /**
     * Pobiera kontekst profilu dla bieżącego użytkownika
     * @return kontekst profilu (informacje o dostępie)
     */
    @Transactional
    public UserProfileContextDTO getCurrentUserProfileContext() {
        // Pobierz bieżącego użytkownika
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByEmail(currentUserEmail);

        // Pobierz profil
        UserProfile profile = userProfileRepository.findById(currentUser.getProfile().getId())
                .orElseThrow(() -> new IllegalArgumentException("Profil o podanym ID nie istnieje"));

        return getProfileContext(profile.getId());
    }

    /**
     * Sprawdza, czy bieżący użytkownik może edytować profil o danym ID
     * @param profileId ID profilu
     * @return true, jeśli użytkownik może edytować profil
     */
    @Transactional(readOnly = true)
    public boolean canEdit(Long profileId) {
        return getProfileContext(profileId).isEditable();
    }

    /**
     * Sprawdza, czy bieżący użytkownik jest właścicielem profilu o danym ID
     * @param profileId ID profilu
     * @return true, jeśli użytkownik jest właścicielem profilu
     */
    @Transactional(readOnly = true)
    public boolean isOwner(Long profileId) {
        return getProfileContext(profileId).isOwner();
    }
}