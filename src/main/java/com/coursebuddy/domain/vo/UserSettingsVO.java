package com.coursebuddy.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsVO {
    private Long userId;
    private Boolean notifyEmail;
    private Boolean notifyPush;
    private String privacyProfile;
    private String language;
    private String theme;
    private String timezone;
}
