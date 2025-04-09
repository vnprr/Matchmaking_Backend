# Content

# File structure

# Groups

# Controllers
List of controllers that manage the app's data and business logic.

## `UserProfileSectionController`

Controller do administracji sekcjami profilu użytkownika.

Wymaga roli: [`ADMIN`](#admin)

### Endpoints

#### `GET /api/admin/profile-sections`
Pobiera wszystkie sekcje profilu  
Zwraca: Lista obiektów `UserProfileSectionDefinitionDTO`  

#### `GET /api/admin/profile-sections/{id}`

Pobiera sekcję po ID  
Parametry: `id` (Long) - identyfikator sekcji  
Zwraca: `UserProfileSectionDefinitionDTO`

#### `POST /api/admin/profile-sections`  
Tworzy nową sekcję profilu  
Body: `UserProfileSectionDefinitionDTO` - dane sekcji
Zwraca: Utworzony obiekt `UserProfileSectionDefinitionDTO`  
Nowa sekcja jest zawsze dodawana na koniec listy (displayOrder = N)

#### `PUT /api/admin/profile-sections/{id}`

Aktualizuje sekcję profilu  
Parametry: `id` (Long) - identyfikator sekcji  
Body: `UserProfileSectionDefinitionDTO` - dane sekcji  
Zwraca: Zaktualizowany obiekt `UserProfileSectionDefinitionDTO`  
Nie zmienia kolejności wyświetlania (displayOrder)

#### `DELETE /api/admin/profile-sections/{id}`

Usuwa sekcję profilu  
Parametry: `id` (Long) - identyfikator sekcji  
Automatycznie przenumerowuje pozostałe sekcje

`PUT /api/admin/profile-sections/order`
Aktualizuje kolejność sekcji  
Body: Lista obiektów `UserProfileSectionDefinitionDTO` z nową kolejnością  
Zapewnia ciągłą numerację od 0 do n-1

# Services

## `UserProfileSectionAdminService`

Serwis zarządzający logiką biznesową definicji sekcji profilu.

## Metody

#### `getAllSectionDefinitions()`

Zwraca: Lista `UserProfileSectionDefinitionDTO`

Opis: Pobiera wszystkie definicje sekcji posortowane wg kolejności wyświetlania

Adnotacja: @Transactional(readOnly = true)

#### `getSectionDefinitionById(Long id)`

Parametry: `id` - identyfikator sekcji

Zwraca: `UserProfileSectionDefinitionDTO`

Opis: Pobiera definicję sekcji po ID

Wyjątki: IllegalArgumentException gdy sekcja nie istnieje

Adnotacja: @Transactional(readOnly = true)

#### `createSectionDefinition(UserProfileSectionDefinitionDTO dto)`

Parametry: `dto` - dane sekcji

Zwraca: `UserProfileSectionDefinitionDTO` utworzonej sekcji

Opis: Tworzy nową sekcję z numerem displayOrder = liczba istniejących sekcji

Adnotacja: @Transactional

#### `updateSectionDefinition(Long id, UserProfileSectionDefinitionDTO dto)`

Parametry: `id` - identyfikator sekcji, `dto` - dane sekcji

Zwraca: `UserProfileSectionDefinitionDTO` zaktualizowanej sekcji

Opis: Aktualizuje istniejącą sekcję bez zmiany kolejności (displayOrder)

Wyjątki: IllegalArgumentException gdy sekcja nie istnieje

Adnotacja: @Transactional

#### `deleteSectionDefinition(Long id)`

Parametry: `id` - identyfikator sekcji

Opis: Usuwa sekcję i automatycznie przenumerowuje pozostałe

Wyjątki: IllegalArgumentException gdy sekcja nie istnieje

Adnotacja: @Transactional

#### `updateSectionsOrder(List<UserProfileSectionDefinitionDTO> sections)`

Parametry: `sections` - lista sekcji z nową kolejnością

Opis: Aktualizuje kolejność sekcji zapewniając ciągłą numerację od 0 do n-1

Adnotacja: @Transactional

#### `private reorderAllSections()`

Opis: Przenumerowuje wszystkie sekcje zapewniając ciągłą numerację od 0 do n-1

Metoda pomocnicza wywoływana po usunięciu sekcji i updateSectionsOrder