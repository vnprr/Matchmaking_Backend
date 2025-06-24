package com.matchmaking.backend.model.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "Treść wiadomości nie może być pusta!")
    @Size(max = 2000, message = "Wiadomość nie może przekraczać 2000 znaków!")
    private String content;
}