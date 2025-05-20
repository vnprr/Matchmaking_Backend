# Matchmaking Application - Backend Server

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [Authentication Flow](#authentication-flow)
- [Services](#services)
- [Controllers](#controllers)
- [Repositories](#repositories)
- [Configuration](#configuration)
- [Exception Handling](#exception-handling)
- [Validation](#validation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Development Guidelines](#development-guidelines)

## Overview
This is a backend service for a matchmaking application built with Spring Boot. The application provides functionality for user registration, profile management, matchmaking recommendations, chat, and notifications. It is designed to facilitate connections between users based on their profiles and preferences.

## Architecture
The application follows a layered architecture pattern:

1. **Presentation Layer**: REST controllers that handle HTTP requests and responses
2. **Service Layer**: Business logic implementation
3. **Data Access Layer**: Repositories for database operations
4. **Domain Layer**: Entity models representing the business domain

The application uses Spring Security for authentication and authorization, with both JWT-based authentication and OAuth2 social login support. Data is stored in a PostgreSQL database, accessed via Spring Data JPA. Profile images are stored in Cloudinary.

## Technologies Used
- **Java 17**: Core programming language
- **Spring Boot 3.5.0**: Application framework
- **Spring Security**: Authentication and authorization
  - JWT for token-based authentication
  - OAuth2 for social login
- **Spring Data JPA**: Data access framework
- **PostgreSQL**: Relational database
- **Cloudinary**: Cloud-based image storage
- **Maven**: Dependency management and build tool
- **Lombok**: Reduces boilerplate code through annotations
- **BCrypt**: Password hashing
- **JUnit 5**: Testing framework

## Features
- **User Management**
  - Registration and email verification
  - JWT-based authentication
  - Social login with OAuth2
  - Password reset and change
  - Email change

- **Profile Management**
  - User profile creation and editing
  - Profile sections customization
  - Profile image upload and management
    - Image cropping
    - Setting main profile image
    - Reordering images

- **Matchmaking**
  - User recommendations based on profiles
  - Accepting or rejecting recommendations

- **Communication**
  - Real-time chat between matched users
  - Conversation management

- **Notifications**
  - System notifications for important events
  - Marking notifications as read

- **Administration**
  - User management
  - Role management
  - Profile section configuration
  - Application settings

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── matchmaking/
│   │           └── backend/
│   │               ├── config/         # Configuration classes
│   │               │   ├── CloudinaryConfig.java           # Cloudinary configuration
│   │               │   ├── JpaConfig.java                  # JPA configuration
│   │               │   ├── JwtAuthenticationFilter.java    # JWT filter
│   │               │   ├── JwtUtil.java                    # JWT utilities
│   │               │   ├── OAuth2LoginSuccessHandler.java  # OAuth2 handler
│   │               │   ├── PasswordConfig.java             # Password encoding
│   │               │   └── SecurityConfig.java             # Security configuration
│   │               │
│   │               ├── controller/     # REST API controllers
│   │               │   ├── AdminConfigController.java      # Admin configuration
│   │               │   ├── AdminUserController.java        # Admin user management
│   │               │   ├── AuthController.java             # Authentication
│   │               │   ├── ChatController.java             # Chat functionality
│   │               │   ├── ImageController.java            # Image management
│   │               │   ├── NotificationController.java     # Notifications
│   │               │   ├── RecommendationController.java   # Recommendations
│   │               │   ├── UserProfileController.java      # User profiles
│   │               │   └── UserProfileSectionController.java # Profile sections
│   │               │
│   │               ├── exception/      # Custom exceptions
│   │               │   └── ResourceNotFoundException.java  # Resource not found
│   │               │
│   │               ├── mapper/         # Object mappers
│   │               │
│   │               ├── model/          # Domain models
│   │               │   ├── auth/                # Authentication models
│   │               │   ├── chat/                # Chat models
│   │               │   ├── notification/        # Notification models
│   │               │   ├── recommendation/      # Recommendation models
│   │               │   ├── user/                # User-related models
│   │               │   │   └── profile/         # Profile models
│   │               │   │       ├── image/       # Profile image models
│   │               │   │       └── section/     # Profile section models
│   │               │   ├── User.java            # User entity
│   │               │   ├── UserProfile.java     # User profile entity
│   │               │   ├── Role.java            # User roles
│   │               │   └── Gender.java          # Gender enumeration
│   │               │
│   │               ├── repository/     # Data access layer
│   │               │   ├── AppConfigRepository.java
│   │               │   ├── ConversationRepository.java
│   │               │   ├── MessageRepository.java
│   │               │   ├── NotificationRepository.java
│   │               │   ├── UserProfileImageRepository.java
│   │               │   ├── UserProfileRepository.java
│   │               │   ├── UserProfileSectionContentRepository.java
│   │               │   ├── UserProfileSectionDefinitionRepository.java
│   │               │   ├── UserRecommendationRepository.java
│   │               │   └── UserRepository.java
│   │               │
│   │               ├── service/        # Business logic
│   │               │   ├── notification/        # Notification services
│   │               │   ├── recommendation/      # Recommendation services
│   │               │   ├── user/                # User services
│   │               │   │   └── profile/         # Profile services
│   │               │   │       ├── image/       # Image services
│   │               │   │       └── section/     # Section services
│   │               │   ├── AdminService.java
│   │               │   ├── AppConfigService.java
│   │               │   ├── AuthService.java
│   │               │   ├── ChatService.java
│   │               │   ├── IdEncryptionService.java
│   │               │   ├── JwtService.java
│   │               │   ├── MailService.java
│   │               │   └── UserService.java
│   │               │
│   │               └── validation/     # Validation logic
│   │                   ├── StrongPassword.java          # Password validation annotation
│   │                   └── StrongPasswordValidator.java # Password validator
│   │
│   └── resources/                      # Application resources
│       └── application.properties      # Application configuration
│
└── test/                               # Test classes
    └── java/
        └── com/
            └── matchmaking/
                └── backend/
                    └── BackendApplicationTests.java  # Basic application test
```

## Domain Model

### Core Entities

#### User
The central entity representing a registered user in the system.
- **Fields**:
  - `id`: Unique identifier
  - `email`: User's email address (unique)
  - `password`: Hashed password
  - `verificationCode`: Code for email verification
  - `lastVerificationEmailSentAt`: Timestamp of last verification email
  - `enabled`: Account status
  - `role`: User role (USER, ADMIN)
  - `provider`: Authentication provider (LOCAL, GOOGLE, FACEBOOK)
  - `resetTokenExpiration`: Password reset token expiration
  - `createdAt`: Account creation timestamp
  - `updatedAt`: Account update timestamp
  - `failedLoginAttempts`: Count of failed login attempts
  - `accountLockedUntil`: Account lockout timestamp
- **Relationships**:
  - One-to-One with UserProfile

#### UserProfile
Contains personal information about a user.
- **Fields**:
  - `id`: Unique identifier
  - `firstName`: User's first name
  - `lastName`: User's last name
  - `gender`: User's gender
  - `dateOfBirth`: User's date of birth
  - `bio`: User's biography
  - `createdAt`: Profile creation timestamp
  - `updatedAt`: Profile update timestamp
- **Relationships**:
  - One-to-One with User
  - One-to-Many with UserProfileImage

#### UserRecommendation
Represents a potential match between two users.
- **Fields**:
  - `id`: Unique identifier
  - `status`: Recommendation status (NEW, VIEWED, ACCEPTED, REJECTED)
  - `createdAt`: Creation timestamp
  - `viewedAt`: Timestamp when viewed
- **Relationships**:
  - Many-to-One with User (firstUser)
  - Many-to-One with User (secondUser)
  - Many-to-One with User (createdBy)

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Cloudinary account (for image storage)
- SMTP server for email functionality (optional)

### Configuration
1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/matchmaking-app.git
   cd matchmaking-app
   ```

2. Configure database connection in `application.properties`
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/matchmaking
   spring.datasource.username=postgres
   spring.datasource.password=yourpassword
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

3. Set up Cloudinary credentials in `application.properties`
   ```properties
   cloudinary.cloud-name=your-cloud-name
   cloudinary.api-key=your-api-key
   cloudinary.api-secret=your-api-secret
   ```

4. Configure JWT settings
   ```properties
   jwt.secret=your-secret-key
   jwt.expiration=86400000
   ```

5. Configure OAuth2 providers (optional)
   ```properties
   spring.security.oauth2.client.registration.google.client-id=your-client-id
   spring.security.oauth2.client.registration.google.client-secret=your-client-secret
   spring.security.oauth2.client.registration.google.scope=email,profile

   spring.security.oauth2.client.registration.facebook.client-id=your-client-id
   spring.security.oauth2.client.registration.facebook.client-secret=your-client-secret
   spring.security.oauth2.client.registration.facebook.scope=email,public_profile
   ```

6. Configure email settings (optional)
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

### Building and Running
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## API Documentation

### Authentication

#### Registration and Login
- `POST /api/auth/register` - Register a new user
  - Request: `RegisterRequestDTO` (email, password, firstName, lastName)
  - Response: Success message or error

- `POST /api/auth/login` - Login and get JWT token
  - Request: `LoginRequestDTO` (email, password)
  - Response: `AuthResponseDTO` (token, expiresIn)

- `GET /api/auth/verify` - Verify email address
  - Query Parameter: `code` (verification code)
  - Response: Success message or error

#### Password Management
- `POST /api/auth/forgot-password` - Request password reset
  - Request: `PasswordResetRequestDTO` (email)
  - Response: Success message

- `POST /api/auth/reset-password` - Reset password with token
  - Request: `PasswordResetDTO` (token, newPassword)
  - Response: Success message or error

- `POST /api/auth/change-password` - Change password (authenticated)
  - Request: `PasswordChangeDTO` (currentPassword, newPassword)
  - Response: Success message or error

#### Email Management
- `POST /api/auth/change-email` - Change email address (authenticated)
  - Request: `EmailChangeDTO` (password, newEmail)
  - Response: Success message or error

- `POST /api/auth/resend-verification-email` - Resend verification email
  - Query Parameter: `email`
  - Response: Success message or error

### User Profile

#### Profile Management
- `GET /api/profile` - Get current user's profile
  - Response: `UserProfileDTO`

- `PUT /api/profile` - Update user profile
  - Request: `UserProfileDTO`
  - Response: Updated `UserProfileDTO`

- `GET /api/profile/{id}` - Get profile by ID
  - Path Variable: `id` (profile ID)
  - Response: `UserProfileDTO`

#### Profile Sections
- `GET /api/profile/sections` - Get current user's profile sections
  - Response: List of `UserProfileSectionContentDTO`

- `PUT /api/profile/sections/{id}` - Update profile section content
  - Path Variable: `id` (section ID)
  - Request: `UserProfileSectionContentDTO`
  - Response: Updated `UserProfileSectionContentDTO`

### Images

#### Profile Images
- `GET /api/images/main` - Get current user's main profile image
  - Response: `UserProfileImageDTO` or null

- `GET /api/images` - Get all current user's profile images
  - Response: List of `UserProfileImageDTO`

- `GET /api/images/profile/{profileId}/main` - Get main image of a profile
  - Path Variable: `profileId`
  - Response: `UserProfileImageDTO` or null

- `GET /api/images/profile/{profileId}` - Get all images of a profile
  - Path Variable: `profileId`
  - Response: List of `UserProfileImageDTO`

#### Image Management
- `POST /api/images/upload` - Upload profile image
  - Request: MultipartFile
  - Response: `UserProfileImageDTO`

- `PUT /api/images/{id}/crop` - Crop an image
  - Path Variable: `id` (image ID)
  - Request: `ImageCropDTO` (x, y, width, height)
  - Response: `UserProfileImageDTO`

- `PUT /api/images/{id}/main` - Set image as main profile picture
  - Path Variable: `id` (image ID)
  - Response: Success message

- `DELETE /api/images/{id}` - Delete profile image
  - Path Variable: `id` (image ID)
  - Response: Success message

- `PUT /api/images/order` - Update images order
  - Request: `UserProfileImageOrderDTO` (list of image IDs in order)
  - Response: Success message

### Recommendations

#### Matchmaking
- `GET /api/recommendations` - Get matchmaking recommendations
  - Response: List of `UserRecommendationDTO`

- `GET /api/recommendations/{id}` - Get specific recommendation
  - Path Variable: `id` (recommendation ID)
  - Response: `UserRecommendationDTO`

- `PUT /api/recommendations/{id}/accept` - Accept a recommendation
  - Path Variable: `id` (recommendation ID)
  - Response: Success message

- `PUT /api/recommendations/{id}/reject` - Reject a recommendation
  - Path Variable: `id` (recommendation ID)
  - Response: Success message

- `PUT /api/recommendations/{id}/view` - Mark recommendation as viewed
  - Path Variable: `id` (recommendation ID)
  - Response: Success message

### Chat

#### Conversations
- `GET /api/chat/conversations` - Get user's conversations
  - Response: List of `ConversationDTO`

- `GET /api/chat/conversations/{id}` - Get specific conversation
  - Path Variable: `id` (conversation ID)
  - Response: `ConversationDTO`

- `POST /api/chat/conversations` - Create new conversation
  - Request: `ConversationCreateDTO` (participantId)
  - Response: `ConversationDTO`

#### Messages
- `GET /api/chat/conversations/{id}/messages` - Get messages in a conversation
  - Path Variable: `id` (conversation ID)
  - Query Parameters: `page`, `size` (pagination)
  - Response: Page of `MessageDTO`

- `POST /api/chat/conversations/{id}/messages` - Send a message
  - Path Variable: `id` (conversation ID)
  - Request: `MessageCreateDTO` (content)
  - Response: `MessageDTO`

- `PUT /api/chat/conversations/{id}/read` - Mark conversation as read
  - Path Variable: `id` (conversation ID)
  - Response: Success message

### Notifications
- `GET /api/notifications` - Get user's notifications
  - Query Parameters: `page`, `size` (pagination)
  - Response: Page of `NotificationDTO`

- `PUT /api/notifications/{id}/read` - Mark notification as read
  - Path Variable: `id` (notification ID)
  - Response: Success message

- `PUT /api/notifications/read-all` - Mark all notifications as read
  - Response: Success message

### Admin

#### User Management
- `GET /api/admin/users` - Get all users (admin only)
  - Query Parameters: `page`, `size`, `sort` (pagination and sorting)
  - Response: Page of `AdminUserListDTO`

- `GET /api/admin/users/{id}` - Get user by ID (admin only)
  - Path Variable: `id` (user ID)
  - Response: `AdminUserDTO`

- `PUT /api/admin/users/{id}/role` - Change user role (admin only)
  - Path Variable: `id` (user ID)
  - Request: `Role` (enum value)
  - Response: Updated `AdminUserDTO`

- `DELETE /api/admin/users/{id}` - Delete user (admin only)
  - Path Variable: `id` (user ID)
  - Response: Success message

#### Profile Section Management
- `GET /api/admin/profile-sections` - Get all profile sections (admin only)
  - Response: List of `UserProfileSectionDefinitionDTO`

- `GET /api/admin/profile-sections/{id}` - Get section by ID (admin only)
  - Path Variable: `id` (section ID)
  - Response: `UserProfileSectionDefinitionDTO`

- `POST /api/admin/profile-sections` - Create new profile section (admin only)
  - Request: `UserProfileSectionDefinitionDTO`
  - Response: Created `UserProfileSectionDefinitionDTO`

- `PUT /api/admin/profile-sections/{id}` - Update a profile section (admin only)
  - Path Variable: `id` (section ID)
  - Request: `UserProfileSectionDefinitionDTO`
  - Response: Updated `UserProfileSectionDefinitionDTO`

- `DELETE /api/admin/profile-sections/{id}` - Delete a profile section (admin only)
  - Path Variable: `id` (section ID)
  - Response: Success message

- `PUT /api/admin/profile-sections/order` - Update section order (admin only)
  - Request: List of `UserProfileSectionDefinitionDTO` with new order
  - Response: Success message

#### Application Configuration
- `GET /api/admin/config` - Get application configuration (admin only)
  - Response: `AppConfig`

- `PUT /api/admin/config` - Update application configuration (admin only)
  - Request: `AppConfig`
  - Response: Updated `AppConfig`

## Authentication Flow

### JWT Authentication
1. User submits login credentials (email/password)
2. Server validates credentials
3. If valid, server generates JWT token
4. Token is returned to client
5. Client includes token in Authorization header for subsequent requests
6. Server validates token for each protected request

### OAuth2 Authentication
1. User initiates OAuth2 login (Google, Facebook)
2. User is redirected to OAuth2 provider
3. User authenticates with provider
4. Provider redirects back to application with authorization code
5. Server exchanges code for access token
6. Server retrieves user info from provider
7. If user exists, they are logged in; if not, a new account is created
8. JWT token is generated and returned to client

### Password Reset
1. User requests password reset
2. Server generates reset token and sends email
3. User clicks link in email
4. User submits new password with token
5. Server validates token and updates password

## Services

### `AuthService`
Handles user authentication, registration, and account management.

#### Methods
- `register(RegisterRequestDTO request)` - Registers a new user
- `verifyUser(String code)` - Verifies a user's email
- `login(LoginRequestDTO request)` - Authenticates a user and returns JWT
- `resendVerificationEmail(String email)` - Resends verification email
- `requestPasswordReset(String email)` - Initiates password reset
- `resetPassword(String token, String newPassword)` - Resets password
- `changePassword(PasswordChangeDTO request)` - Changes user password
- `changeEmail(EmailChangeDTO request)` - Changes user email

### `UserService`
Manages user-related operations.

#### Methods
- `getUserById(Long id)` - Gets user by ID
- `getUserByEmail(String email)` - Gets user by email
- `getCurrentUser()` - Gets currently authenticated user
- `createUser(User user)` - Creates a new user
- `updateUser(User user)` - Updates an existing user
- `deleteUser(Long id)` - Deletes a user

### `ImageService`
Manages user profile images.

#### Methods
- `getMainProfileImage(Long userProfileId)` - Gets main profile image
- `getProfileImages(Long userProfileId)` - Gets all profile images
- `uploadImage(MultipartFile file)` - Uploads a new image
- `cropImage(Long imageId, ImageCropDTO cropDTO)` - Crops an image
- `setMainImage(Long imageId)` - Sets an image as main
- `deleteImage(Long imageId)` - Deletes an image
- `updateImagesOrder(UserProfileImageOrderDTO orderDTO)` - Updates image order

### `ChatService`
Manages conversations and messages between users.

#### Methods
- `getUserConversations()` - Gets user's conversations
- `getConversation(Long id)` - Gets a specific conversation
- `createConversation(Long participantId)` - Creates a new conversation
- `getMessages(Long conversationId, int page, int size)` - Gets messages
- `sendMessage(Long conversationId, String content)` - Sends a message
- `markConversationAsRead(Long conversationId)` - Marks conversation as read

### `RecommendationService`
Manages user matchmaking recommendations.

#### Methods
- `getUserRecommendations()` - Gets recommendations for current user
- `getRecommendation(Long id)` - Gets a specific recommendation
- `acceptRecommendation(Long id)` - Accepts a recommendation
- `rejectRecommendation(Long id)` - Rejects a recommendation
- `viewRecommendation(Long id)` - Marks recommendation as viewed

### `NotificationService`
Manages user notifications.

#### Methods
- `getUserNotifications(int page, int size)` - Gets user's notifications
- `markAsRead(Long id)` - Marks a notification as read
- `markAllAsRead()` - Marks all notifications as read
- `createNotification(User user, String message, String type)` - Creates notification

### `IdEncryptionService`
Service for encrypting and decrypting identifiers.

#### Methods
- `encryptId(Long id)` - Encrypts user ID to public ID
- `decryptId(String publicId)` - Decrypts public ID to actual user ID

### `UserProfileSectionAdminService`
Service managing the business logic of profile section definitions.

#### Methods
- `getAllSectionDefinitions()` - Gets all section definitions sorted by display order
- `getSectionDefinitionById(Long id)` - Gets section definition by ID
- `createSectionDefinition(UserProfileSectionDefinitionDTO dto)` - Creates a new section
- `updateSectionDefinition(Long id, UserProfileSectionDefinitionDTO dto)` - Updates an existing section
- `deleteSectionDefinition(Long id)` - Deletes a section and renumbers the rest
- `updateSectionsOrder(List<UserProfileSectionDefinitionDTO> sections)` - Updates section order

## Controllers

### `AuthController`
Handles authentication-related endpoints.

#### Endpoints
- `POST /api/auth/register` - Register a new user
- `GET /api/auth/verify` - Verify email address
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/resend-verification-email` - Resend verification email
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password
- `POST /api/auth/change-password` - Change password
- `POST /api/auth/change-email` - Change email

### `UserProfileController`
Handles user profile management.

#### Endpoints
- `GET /api/profile` - Get current user's profile
- `PUT /api/profile` - Update user profile
- `GET /api/profile/{id}` - Get profile by ID

### `ImageController`
Handles profile image management.

#### Endpoints
- `GET /api/images/main` - Get current user's main profile image
- `GET /api/images` - Get all current user's profile images
- `GET /api/images/profile/{profileId}/main` - Get main image of a profile
- `GET /api/images/profile/{profileId}` - Get all images of a profile
- `POST /api/images/upload` - Upload profile image
- `PUT /api/images/{id}/crop` - Crop an image
- `PUT /api/images/{id}/main` - Set image as main profile picture
- `DELETE /api/images/{id}` - Delete profile image
- `PUT /api/images/order` - Update images order

### `ChatController`
Handles chat functionality.

#### Endpoints
- `GET /api/chat/conversations` - Get user's conversations
- `GET /api/chat/conversations/{id}` - Get specific conversation
- `POST /api/chat/conversations` - Create new conversation
- `GET /api/chat/conversations/{id}/messages` - Get messages in a conversation
- `POST /api/chat/conversations/{id}/messages` - Send a message
- `PUT /api/chat/conversations/{id}/read` - Mark conversation as read

### `RecommendationController`
Handles matchmaking recommendations.

#### Endpoints
- `GET /api/recommendations` - Get matchmaking recommendations
- `GET /api/recommendations/{id}` - Get specific recommendation
- `PUT /api/recommendations/{id}/accept` - Accept a recommendation
- `PUT /api/recommendations/{id}/reject` - Reject a recommendation
- `PUT /api/recommendations/{id}/view` - Mark recommendation as viewed

### `NotificationController`
Handles user notifications.

#### Endpoints
- `GET /api/notifications` - Get user's notifications
- `PUT /api/notifications/{id}/read` - Mark notification as read
- `PUT /api/notifications/read-all` - Mark all notifications as read

### `AdminUserController`
Handles admin user management.

#### Endpoints
- `GET /api/admin/users` - Get all users
- `GET /api/admin/users/{id}` - Get user by ID
- `PUT /api/admin/users/{id}/role` - Change user role
- `DELETE /api/admin/users/{id}` - Delete user

### `UserProfileSectionController`
Handles user profile sections.

#### Endpoints
- `GET /api/admin/profile-sections` - Get all profile sections
- `GET /api/admin/profile-sections/{id}` - Get section by ID
- `POST /api/admin/profile-sections` - Create new profile section
- `PUT /api/admin/profile-sections/{id}` - Update a profile section
- `DELETE /api/admin/profile-sections/{id}` - Delete a profile section
- `PUT /api/admin/profile-sections/order` - Update section order

## Repositories

### `UserRepository`
Repository for User entity.

#### Methods
- `findByEmail(String email)` - Find user by email
- `findByVerificationCode(String code)` - Find user by verification code
- `existsByEmail(String email)` - Check if user exists by email

### `UserProfileRepository`
Repository for UserProfile entity.

#### Methods
- `findByUserId(Long userId)` - Find profile by user ID

### `UserProfileImageRepository`
Repository for UserProfileImage entity.

#### Methods
- `findByUserProfileAndIsMainTrue(UserProfile profile)` - Find main image of a profile
- `findByUserProfileOrderByDisplayOrderAsc(UserProfile profile)` - Find all images of a profile
- `findByIdAndUserProfile(Long id, UserProfile profile)` - Find image by ID and profile
- `countByUserProfile(UserProfile profile)` - Count images of a profile

### `UserRecommendationRepository`
Repository for UserRecommendation entity.

#### Methods
- `findByFirstUserOrSecondUser(User user1, User user2)` - Find recommendations for a user
- `findByFirstUserAndSecondUser(User user1, User user2)` - Find recommendation between two users
- `findByIdAndFirstUserOrIdAndSecondUser(Long id, User user1, Long id2, User user2)` - Find recommendation by ID for a user

### `ConversationRepository`
Repository for Conversation entity.

#### Methods
- `findByParticipantsContaining(User user)` - Find conversations for a user
- `findByIdAndParticipantsContaining(Long id, User user)` - Find conversation by ID for a user

### `MessageRepository`
Repository for Message entity.

#### Methods
- `findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable)` - Find messages in a conversation
- `countByConversationAndRecipientAndReadFalse(Conversation conversation, User recipient)` - Count unread messages

## Configuration

### `SecurityConfig`
Configures security for the application.

#### Features
- JWT Authentication
- OAuth2 Login
- CORS Configuration
- Authorization Rules
- Password Encoding

### `CloudinaryConfig`
Configures Cloudinary for image storage.

#### Features
- Cloudinary Bean Configuration
- Upload Preset Configuration

### `JwtAuthenticationFilter`
Filter for JWT authentication.

#### Features
- Token Extraction
- Token Validation
- User Authentication

### `OAuth2LoginSuccessHandler`
Handler for successful OAuth2 login.

#### Features
- User Creation/Update
- JWT Token Generation
- Redirect to Frontend

## Exception Handling

### `ResourceNotFoundException`
Exception thrown when a requested resource is not found.

#### Usage
- Thrown when a user, profile, image, or other entity is not found
- Includes a message with details about the missing resource

## Validation

### `StrongPassword`
Custom validation annotation for strong passwords.

#### Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

### `StrongPasswordValidator`
Validator implementation for the StrongPassword annotation.

#### Features
- Password Strength Validation
- Custom Error Messages

## Testing

The application includes basic test setup with JUnit 5 and Spring Boot Test.

### `BackendApplicationTests`
Basic test class that verifies the application context loads successfully.

#### Tests
- `contextLoads()` - Verifies Spring application context loads

## Deployment

### Prerequisites
- Java 17 runtime
- PostgreSQL database
- Cloudinary account
- SMTP server (optional)

### Production Configuration
1. Configure database connection for production
   ```properties
   spring.datasource.url=jdbc:postgresql://production-db-host:5432/matchmaking
   spring.datasource.username=production-user
   spring.datasource.password=production-password
   ```

2. Configure security settings
   ```properties
   jwt.secret=strong-production-secret-key
   spring.security.oauth2.client.registration.google.client-id=production-client-id
   spring.security.oauth2.client.registration.google.client-secret=production-client-secret
   ```

3. Configure CORS for production
   ```properties
   cors.allowed-origins=https://your-frontend-domain.com
   ```

### Deployment Options
1. **JAR Deployment**
   - Build the application: `mvn clean package`
   - Run the JAR: `java -jar target/backend-0.0.1-SNAPSHOT.jar`

2. **Docker Deployment**
   - Build Docker image: `docker build -t matchmaking-app .`
   - Run container: `docker run -p 8080:8080 matchmaking-app`

3. **Cloud Deployment**
   - Deploy to AWS Elastic Beanstalk
   - Deploy to Azure App Service
   - Deploy to Google Cloud Run

## Troubleshooting

### Common Issues

#### Authentication Problems
- **Issue**: JWT token not working
  - **Solution**: Check token expiration and secret key configuration

- **Issue**: OAuth2 login failing
  - **Solution**: Verify client ID and secret, check redirect URI configuration

#### Database Issues
- **Issue**: Connection failures
  - **Solution**: Check database credentials and connection string

- **Issue**: Migration errors
  - **Solution**: Review schema changes, consider using Flyway or Liquibase for migrations

#### Image Upload Problems
- **Issue**: Images not uploading
  - **Solution**: Verify Cloudinary credentials and configuration

- **Issue**: Image processing errors
  - **Solution**: Check image size and format, ensure Cloudinary service is available

### Logging
- Enable debug logging for troubleshooting:
  ```properties
  logging.level.com.matchmaking.backend=DEBUG
  logging.level.org.springframework.security=DEBUG
  ```

## Development Guidelines

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Use Lombok annotations to reduce boilerplate

### Architecture Principles
- Maintain separation of concerns
- Follow the Single Responsibility Principle
- Use DTOs for data transfer between layers
- Keep controllers thin, business logic in services

### Testing Guidelines
- Write unit tests for services
- Write integration tests for controllers
- Use MockMvc for testing REST endpoints
- Use Mockito for mocking dependencies

### Git Workflow
- Use feature branches for new features
- Create pull requests for code review
- Write meaningful commit messages
- Keep commits focused on single changes

### Documentation
- Document all public APIs
- Update README when adding new features
- Document configuration properties
- Add comments for complex algorithms
