package com.matchmaking.backend.service;

import com.matchmaking.backend.model.UserProfile;
import com.matchmaking.backend.model.chat.*;
import com.matchmaking.backend.model.notification.NotificationType;
import com.matchmaking.backend.repository.ConversationRepository;
import com.matchmaking.backend.repository.MessageRepository;
import com.matchmaking.backend.repository.UserProfileRepository;
import com.matchmaking.backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserProfileRepository userProfileRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<ConversationDTO> getUserConversations(Long profileId, Pageable pageable) {
        UserProfile currentProfile = getUserProfileById(profileId);
        Page<Conversation> conversations = conversationRepository
                .findConversationsByProfile(currentProfile, pageable);

        return conversations.map(conversation ->
                mapToConversationDTO(conversation, currentProfile));
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getConversationMessages(
            Long conversationId,
            Long profileId,
            Pageable pageable
    ) {
        Conversation conversation = findConversationById(conversationId);
        validateConversationAccess(conversation, profileId);

        UserProfile currentProfile = getUserProfileById(profileId);
        Page<Message> messages = messageRepository
                .findByConversationOrderByCreatedAtDesc(conversation, pageable);

        return messages.map(message -> mapToMessageDTO(message, currentProfile));
    }

    @Transactional
    public MessageDTO sendMessage(
            Long recipientProfileId,
            MessageRequest messageRequest,
            Long senderProfileId
    ) {
        UserProfile senderProfile = getUserProfileById(senderProfileId);
        UserProfile recipientProfile = getUserProfileById(recipientProfileId);

        Conversation conversation = conversationRepository
                .findConversationBetweenProfiles(senderProfile, recipientProfile)
                .orElseGet(() -> createNewConversation(senderProfile, recipientProfile));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(senderProfile)
                .recipient(recipientProfile)
                .content(messageRequest.getContent())
                .read(false)
                .build();

        message = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        notificationService.createNotification(
                recipientProfile.getUser(),
                NotificationType.MESSAGE,
                senderProfile.getFirstName() + " wysłał(a) ci wiadomość",
                conversation.getId()
        );

        return mapToMessageDTO(message, senderProfile);
    }

    @Transactional
    public void markConversationAsRead(Long conversationId, Long profileId) {
        Conversation conversation = findConversationById(conversationId);
        validateConversationAccess(conversation, profileId);

        UserProfile currentProfile = getUserProfileById(profileId);
        List<Message> unreadMessages = messageRepository
                .findByConversationAndRecipientAndReadFalse(conversation, currentProfile);

        LocalDateTime now = LocalDateTime.now();
        unreadMessages.forEach(message -> {
            message.setRead(true);
            message.setReadAt(now);
        });

        if (!unreadMessages.isEmpty()) {
            messageRepository.saveAll(unreadMessages);
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadMessagesCount(Long profileId) {
        UserProfile profile = getUserProfileById(profileId);
        return conversationRepository.countUnreadMessagesByProfile(profile);
    }

    // Metody pomocnicze

    private Conversation createNewConversation(
            UserProfile firstProfile,
            UserProfile secondProfile
    ) {
        Conversation conversation = Conversation.builder()
                .firstUser(firstProfile)
                .secondUser(secondProfile)
                .lastMessageAt(LocalDateTime.now())
                .build();

        return conversationRepository.save(conversation);
    }

    private ConversationDTO mapToConversationDTO(
            Conversation conversation,
            UserProfile currentProfile
    ) {
        UserProfile otherProfile =
                conversation.getFirstUser().getId().equals(currentProfile.getId())
                        ? conversation.getSecondUser() : conversation.getFirstUser();

        Long unreadCount = messageRepository
                .countUnreadMessagesInConversation(conversation, currentProfile);

        String photoUrl = "https://en.wikipedia.org/wiki/Portal:Cats/Selected_picture#/media/File:Panthera_tigris_sumatran_subspecies.jpg";

        return createConversationDTO(conversation, otherProfile, unreadCount, photoUrl);
    }

    private ConversationDTO createConversationDTO(
            Conversation conversation,
            UserProfile otherProfile,
            Long unreadCount,
            String photoUrl
    ) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setRecipientId(otherProfile.getId());
        dto.setRecipientName(otherProfile.getFirstName() + " " + otherProfile.getLastName());
        dto.setRecipientPhotoUrl(photoUrl != null ? photoUrl : "default-profile-image.jpg");

        String lastMessageContent = "";
        if (!conversation.getMessages().isEmpty()) {
            lastMessageContent = conversation.getMessages().get(0).getContent();
            if (lastMessageContent.length() > 50) {
                lastMessageContent = lastMessageContent.substring(0, 47) + "...";
            }
        }

        dto.setLastMessage(lastMessageContent);
        dto.setLastMessageAt(conversation.getLastMessageAt());
        dto.setHasUnreadMessages(unreadCount > 0);
        dto.setUnreadCount(unreadCount.intValue());

        return dto;
    }

    private MessageDTO mapToMessageDTO(Message message, UserProfile currentProfile) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setContent(message.getContent());
        dto.setRead(message.isRead());
        dto.setReadAt(message.getReadAt());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setOwnMessage(message.getSender().getId().equals(currentProfile.getId()));
        return dto;
    }

    private Conversation findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Konwersacja o podanym ID nie istnieje"));
    }

    private void validateConversationAccess(Conversation conversation, Long profileId) {
        boolean hasAccess = conversation.getFirstUser().getId().equals(profileId) ||
                conversation.getSecondUser().getId().equals(profileId);

        if (!hasAccess) {
            throw new IllegalArgumentException("Brak dostępu do tej konwersacji");
        }
    }

    private UserProfile getUserProfileById(Long profileId) {
        return userProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profil o podanym ID nie istnieje"));
    }
}