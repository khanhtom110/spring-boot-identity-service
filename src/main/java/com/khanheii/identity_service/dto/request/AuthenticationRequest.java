package com.khanheii.identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data //Sẽ lấy được cả get set
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    String username;
    String password;


}
