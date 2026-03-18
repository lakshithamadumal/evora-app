package com.udari.evora.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String profilePicUrl;
    private String phoneNumber;
    private String address;
    private String city;
    private String fcmToken;
}