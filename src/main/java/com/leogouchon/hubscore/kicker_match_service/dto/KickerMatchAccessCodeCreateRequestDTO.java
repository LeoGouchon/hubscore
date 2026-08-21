package com.leogouchon.hubscore.kicker_match_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class KickerMatchAccessCodeCreateRequestDTO {
    @Size(min = 4, max = 64)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "must contain only alphanumeric characters")
    private String code;

    @Min(4)
    @Max(64)
    private Integer length = 8;

    @Future
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp expiresAt;
}
