package com.foryou.memberapi.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResDto {

    private String accessToken;
    private String refreshToken;
    private String type;
}
