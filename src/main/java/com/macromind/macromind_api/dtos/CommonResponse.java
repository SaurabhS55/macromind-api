package com.macromind.macromind_api.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommonResponse {
    private String message;
    private String status;
}
