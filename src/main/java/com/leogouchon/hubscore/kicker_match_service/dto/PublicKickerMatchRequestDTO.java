package com.leogouchon.hubscore.kicker_match_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicKickerMatchRequestDTO {
    @NotBlank
    private String code;

    @Valid
    @NotNull
    private KickerMatchRequestDTO match;
}
