package com.leogouchon.hubscore.player_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PlayerRequestDTO {
    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = "^[\\p{L}\\s'\\-]+$",
            message = "Firstname contains unsupported characters"
    )
    private String firstname;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = "^[\\p{L}\\s'\\-]+$",
            message = "Lastname contains unsupported characters"
    )
    private String lastname;

    @NotEmpty(message = "At least one teamId is required")
    private List<UUID> teamIds;

    public PlayerRequestDTO(String firstname, String lastname, List<UUID> teamIds) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.teamIds = teamIds;
    }
}
