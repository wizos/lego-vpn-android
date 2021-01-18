package com.yjsoft.tenonvpn.util;

public enum LanguageType {

    CHINESE("中文"),
    ENGLISH("ENGLISH");

    private String language;

    LanguageType(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language == null ? "" : language;
    }
}
