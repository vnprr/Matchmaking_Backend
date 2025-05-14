package com.matchmaking.backend.service.user.profile.section;

import com.matchmaking.backend.model.*;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContent;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContentRequestDTO;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionContentChangeDTO;
import com.matchmaking.backend.model.user.profile.section.UserProfileSectionDefinition;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.repository.UserProfileSectionContentRepository;
import com.matchmaking.backend.repository.UserProfileSectionDefinitionRepository;
import com.matchmaking.backend.service.UserService;
import com.matchmaking.backend.service.user.profile.UserProfileContextService;
import io.jsonwebtoken.impl.security.EdwardsCurve;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileSectionService {

    private final UserService userService;
    private final UserProfileSectionDefinitionRepository sectionDefinitionRepository;
    private final UserProfileSectionContentRepository sectionContentRepository;
    private final UserProfileContextService contextService;
    private final UserProfileRepository userProfileRepository;


    /**
     * Pobiera wszystkie dostępne sekcje profilu wraz z ich treścią dla bieżącego użytkownika
     */
    @Transactional(readOnly = true)
    public List<UserProfileSectionContentRequestDTO> getUserProfileSections() {
        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Pobierz wszystkie widoczne definicje sekcji
        List<UserProfileSectionDefinition> definitions =
                sectionDefinitionRepository.findAllByVisibleTrueOrderByDisplayOrderAsc();

        // Pobierz istniejące treści sekcji użytkownika
        List<UserProfileSectionContent> existingContents =
                sectionContentRepository.findByUserProfile(profile);

        // Przygotuj DTO dla każdej sekcji
        List<UserProfileSectionContentRequestDTO> result = new ArrayList<>();

        for (UserProfileSectionDefinition def : definitions) {
            UserProfileSectionContentRequestDTO dto = new UserProfileSectionContentRequestDTO();
            dto.setSectionId(def.getId()); // Używamy tylko istniejącego ID sekcji
            dto.setSectionName(def.getName());
            dto.setRequired(def.isRequired());

            // Szukaj treści dla tej sekcji
            Optional<UserProfileSectionContent> contentOpt = existingContents.stream()
                    .filter(c -> c.getSectionDefinition().getId().equals(def.getId()))
                    .findFirst();

            dto.setContent(contentOpt.map(UserProfileSectionContent::getContent).orElse(""));

            result.add(dto);
        }

        return result;
    }

    /**
     * Pobiera sekcję profilu użytkownika po ID profilu
     * @param userProfileId ID profilu użytkownika
     */
    public List<UserProfileSectionContentRequestDTO> getUserProfileSections(Long userProfileId) {

        if (!contextService.canView(userProfileId)) {
            throw new IllegalArgumentException("Nie masz uprawnień do wyświetlania tego profilu");
        }

        UserProfile profile = userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Profil o podanym ID nie istnieje"));

        // Pobierz wszystkie widoczne definicje sekcji
        List<UserProfileSectionDefinition> definitions =
                sectionDefinitionRepository.findAllByVisibleTrueOrderByDisplayOrderAsc();

        // Pobierz istniejące treści sekcji użytkownika
        List<UserProfileSectionContent> existingContents =
                sectionContentRepository.findByUserProfile(profile);

        // Przygotuj DTO dla każdej sekcji
        List<UserProfileSectionContentRequestDTO> result = new ArrayList<>();

        for (UserProfileSectionDefinition def : definitions) {
            UserProfileSectionContentRequestDTO dto = new UserProfileSectionContentRequestDTO();
            dto.setSectionId(def.getId()); // Używamy tylko istniejącego ID sekcji
            dto.setSectionName(def.getName());
            dto.setRequired(def.isRequired());

            // Szukaj treści dla tej sekcji
            Optional<UserProfileSectionContent> contentOpt = existingContents.stream()
                    .filter(c -> c.getSectionDefinition().getId().equals(def.getId()))
                    .findFirst();

            dto.setContent(contentOpt.map(UserProfileSectionContent::getContent).orElse(""));

            result.add(dto);
        }

        return result;
    }

    /**
     * Aktualizuje treść sekcji profilu użytkownika
     */
    @Transactional
    public void updateUserProfileSection(Long sectionId, UserProfileSectionContentChangeDTO sectionContentChange) {
        String content = sectionContentChange.getContent();

        User user = getCurrentUser();
        UserProfile profile = user.getProfile();

        // Pobierz definicję sekcji
        UserProfileSectionDefinition definition = sectionDefinitionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Sekcja o podanym ID nie istnieje"));

        if (!definition.isVisible()) {
            throw new IllegalArgumentException("Nie można edytować niewidocznej sekcji");
        }

        // Jeśli sekcja jest wymagana, sprawdź czy treść nie jest pusta
        if (definition.isRequired() && (content == null || content.trim().isEmpty())) {
            throw new IllegalArgumentException("Ta sekcja wymaga wypełnienia");
        }

        // Znajdź istniejącą treść lub utwórz nową
        UserProfileSectionContent sectionContent = sectionContentRepository
                .findByUserProfileAndSectionDefinition(profile, definition)
                .orElse(new UserProfileSectionContent());

        // Aktualizuj treść
        if (sectionContent.getId() == null) {
            // Nowa treść - ID będzie przydzielone automatycznie
            sectionContent.setUserProfile(profile);
            sectionContent.setSectionDefinition(definition);
        }
        // W przeciwnym razie używamy istniejącego ID (nie modyfikujemy go)

        sectionContent.setContent(content);
        sectionContentRepository.save(sectionContent);
    }

    /**
     * Aktualizuje wiele sekcji profilu na raz
     */
//    @Transactional
//    public void updateUserProfileSections(List<UserProfileSectionContentChangeDTO> sectionsData) {
//        for (UserProfileSectionContentChangeDTO section : sectionsData) {
//            updateUserProfileSection(section.getSectionId(), section.getContent());
//        }
//    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }
}