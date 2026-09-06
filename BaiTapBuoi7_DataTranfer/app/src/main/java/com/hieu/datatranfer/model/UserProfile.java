package com.hieu.datatranfer.model;

import java.io.Serializable;


public class UserProfile implements Serializable {

    private String name;
    private String email;
    private String gender;
    private String phone;
    private String address;


    private String avatarUri;

    public UserProfile() {
    }

    public UserProfile(String name, String email, String gender, String phone, String address, String avatarUri) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.avatarUri = avatarUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", avatarUri='" + avatarUri + '\'' +
                '}';
    }
}