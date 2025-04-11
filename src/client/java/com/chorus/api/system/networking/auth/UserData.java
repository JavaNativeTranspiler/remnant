package com.chorus.api.system.networking.auth;

import cc.polymorphism.annot.IncludeReference;
import lombok.Getter;
import lombok.Setter;

@IncludeReference
@Getter
@Setter
public class UserData {
    private String username;
    private String email;
    private String licenseKey;
    private String expiryDate;
    private String licenseType;

    private UserData() {}

    public boolean isLicenseValid() {
        if (licenseType != null && licenseType.equalsIgnoreCase("Lifetime")) {
            return true;
        }
        return expiryDate != null && !expiryDate.equals("N/A");
    }

    public static UserDataBuilder builder() {
        return new UserDataBuilder();
    }

    public static class UserDataBuilder {
        private final UserData userData;

        private UserDataBuilder() {
            userData = new UserData();
        }

        public UserDataBuilder username(String username) {
            userData.setUsername(username);
            return this;
        }

        public UserDataBuilder email(String email) {
            userData.setEmail(email);
            return this;
        }

        public UserDataBuilder licenseKey(String licenseKey) {
            userData.setLicenseKey(licenseKey);
            return this;
        }

        public UserDataBuilder expiryDate(String expiryDate) {
            userData.setExpiryDate(expiryDate);
            return this;
        }

        public UserDataBuilder licenseType(String licenseType) {
            userData.setLicenseType(licenseType);
            return this;
        }

        public UserData build() {
            return userData;
        }
    }
}