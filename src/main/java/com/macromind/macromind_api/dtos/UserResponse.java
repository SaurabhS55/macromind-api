package com.macromind.macromind_api.dtos;

import java.sql.Date;
import java.time.LocalDateTime;

import com.macromind.macromind_api.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Date dob;
    private Gender gender;
    private LocalDateTime createdAt;
}
