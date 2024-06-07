package com.example.gamecards.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest
{
    private String firstName;
    private String lastName;
    private String email;
    private String password;


}
