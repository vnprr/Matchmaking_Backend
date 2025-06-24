package com.matchmaking.backend.service.section;

import com.matchmaking.backend.model.section.UserProfileSectionDefinition;
import com.matchmaking.backend.model.section.UserProfileSectionDefinitionChangeDTO;
import com.matchmaking.backend.model.section.UserProfileSectionDefinitionDTO;
import com.matchmaking.backend.repository.UserProfileSectionDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileSectionAdminService {

    private final UserProfileSectionDefinitionRepository sectionDefinitionRepository;
    private final UserProfileSectionInitializerService sectionInitializerService;


    /**
     * Pobiera wszystkie definicje sekcji profilu użytkownika
     */
    @Transactional(readOnly = true)
    public List<UserProfileSectionDefinitionDTO> getAllSectionDefinitions() {
        return sectionDefinitionRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pobiera definicję sekcji profilu po ID
     */
    @Transactional(readOnly = true)
    public UserProfileSectionDefinitionDTO getSectionDefinitionById(Long id) {
        return mapToDTO(findSectionById(id));
    }

    /**
     * Tworzy nową definicję sekcji profilu użytkownika
     * Nowa sekcja jest dodawana na końcu listy i sekcje są przenumerowywane
     */
    @Transactional
    public UserProfileSectionDefinitionDTO createSectionDefinition(UserProfileSectionDefinitionChangeDTO dto) {
        int sectionsCount = sectionDefinitionRepository.findAllByOrderByDisplayOrderAsc().size();

        UserProfileSectionDefinition definition = new UserProfileSectionDefinition();
        definition.setName(dto.getName());
        definition.setDisplayOrder(sectionsCount);
        definition.setRequired(dto.isRequired());
        definition.setVisible(dto.isVisible());
        definition.setDescription(dto.getDescription());

        UserProfileSectionDefinition saved = sectionDefinitionRepository.save(definition);
        reorderAllSections();

        // Dodajemy pustą zawartość sekcji dla wszystkich użytkowników
        if (saved.isVisible()) {
            sectionInitializerService.initializeSectionForAllUsers(saved);
        }

        return mapToDTO(saved);
    }

    /**
     * Aktualizuje definicję sekcji profilu
     */
    @Transactional
    public UserProfileSectionDefinitionDTO updateSectionDefinition(Long id, UserProfileSectionDefinitionChangeDTO dto) {
        UserProfileSectionDefinition definition = findSectionById(id);

        definition.setName(dto.getName());
        definition.setRequired(dto.isRequired());
        definition.setVisible(dto.isVisible());
        definition.setDescription(dto.getDescription());

        return mapToDTO(sectionDefinitionRepository.save(definition));
    }

    /**
     * Usuwa definicję sekcji i przenumerowuje pozostałe sekcje
     */
    @Transactional
    public void deleteSectionDefinition(Long id) {
        UserProfileSectionDefinition definition = findSectionById(id);

        // Usuwamy zawartość sekcji dla wszystkich użytkowników
        sectionInitializerService.deleteSectionContentForAllUsers(definition);

        sectionDefinitionRepository.deleteById(id);
        reorderAllSections();
    }

    /**
     * Aktualizuje kolejność wyświetlania wszystkich sekcji
     */
    @Transactional
    public void updateSectionsOrder(List<UserProfileSectionDefinitionDTO> sections) {
        List<UserProfileSectionDefinition> allSections = sectionDefinitionRepository.findAllByOrderByDisplayOrderAsc();
        Map<Long, UserProfileSectionDefinition> sectionsMap = allSections.stream()
                .collect(Collectors.toMap(UserProfileSectionDefinition::getId, Function.identity()));

        // Naprawiono błąd - nie używamy zmiennej i w lambdzie
        for (int i = 0; i < sections.size(); i++) {
            UserProfileSectionDefinition section = sectionsMap.get(sections.get(i).getId());
            if (section != null) {
                section.setDisplayOrder(i);
            }
        }

        sectionDefinitionRepository.saveAll(sectionsMap.values());
    }

    /**
     * Zmienia pozycję sekcji o jeden poziom w górę lub w dół
     * @param id identyfikator sekcji do przesunięcia
     * @param moveUp true - w górę, false - w dół
     * @return zaktualizowana sekcja
     */
    @Transactional
    public UserProfileSectionDefinitionDTO moveSection(Long id, boolean moveUp) {
        List<UserProfileSectionDefinition> sections = sectionDefinitionRepository.findAllByOrderByDisplayOrderAsc();
        int currentIndex = findSectionIndexById(sections, id);

        int targetIndex = moveUp ? currentIndex - 1 : currentIndex + 1;

        // Sprawdź czy przesunięcie jest możliwe
        if (moveUp && currentIndex <= 0) {
            throw new IllegalArgumentException("Nie można przesunąć sekcji w górę");
        } else if (!moveUp && currentIndex >= sections.size() - 1) {
            throw new IllegalArgumentException("Nie można przesunąć sekcji w dół");
        }

        // Pobierz sekcje i zamień ich kolejność
        UserProfileSectionDefinition current = sections.get(currentIndex);
        UserProfileSectionDefinition adjacent = sections.get(targetIndex);

        swapDisplayOrder(current, adjacent);
        sectionDefinitionRepository.saveAll(List.of(current, adjacent));

        return mapToDTO(current);
    }

    /**
     * Przesuwa sekcję o jeden poziom w górę (zmniejsza displayOrder)
     */
    @Transactional
    public UserProfileSectionDefinitionDTO moveUp(Long id) {
        return moveSection(id, true);
    }

    /**
     * Przesuwa sekcję o jeden poziom w dół (zwiększa displayOrder)
     */
    @Transactional
    public UserProfileSectionDefinitionDTO moveDown(Long id) {
        return moveSection(id, false);
    }

    /**
     * Przenumerowuje wszystkie sekcje, zapewniając ciągłą numerację od 0 do n-1
     */
    @Transactional
    public void reorderAllSections() {
        List<UserProfileSectionDefinition> sections = sectionDefinitionRepository.findAllByOrderByDisplayOrderAsc();

        for (int i = 0; i < sections.size(); i++) {
            sections.get(i).setDisplayOrder(i);
        }

        sectionDefinitionRepository.saveAll(sections);
    }

    // Metody pomocnicze

    private UserProfileSectionDefinition findSectionById(Long id) {
        return sectionDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sekcja o podanym ID nie istnieje"));
    }

    private int findSectionIndexById(List<UserProfileSectionDefinition> sections, Long id) {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void swapDisplayOrder(UserProfileSectionDefinition first, UserProfileSectionDefinition second) {
        int tempOrder = first.getDisplayOrder();
        first.setDisplayOrder(second.getDisplayOrder());
        second.setDisplayOrder(tempOrder);
    }

    private UserProfileSectionDefinitionDTO mapToDTO(UserProfileSectionDefinition definition) {
        UserProfileSectionDefinitionDTO dto = new UserProfileSectionDefinitionDTO();
        dto.setId(definition.getId());
        dto.setName(definition.getName());
        dto.setDisplayOrder(definition.getDisplayOrder());
        dto.setRequired(definition.isRequired());
        dto.setVisible(definition.isVisible());
        dto.setDescription(definition.getDescription());
        return dto;
    }
}