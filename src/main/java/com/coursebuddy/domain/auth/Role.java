package com.coursebuddy.domain.auth;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 角色
 */
@Getter
public enum Role {
    ADMIN("ADMIN"),
    TEACHER("TEACHER"),
    TA("TA"),
    STUDENT("STUDENT");

    @EnumValue
    @JsonValue
    private final String value;

    Role(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Role fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        for (Role role : values()) {
            if (role.value.equals(normalized)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unsupported role: " + value);
    }
}
