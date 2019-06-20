package com.tingfeng.config;

public class CasConfig {

    /**
     * CAS登录地址的token
     */
    public static String GET_TOKEN_URL = "https://cas.example.org:8443/cas/v1/tickets";
    public static String GET_OAUTH_CODE_URL = "https://cas.example.org:8443/cas/oauth2.0/authorize";
    public static String GET_OAUTH_ACCESSTOKEN_URL = "https://cas.example.org:8443/cas/oauth2.0/accessToken";
    public static String GET_OAUTH_PROFILE_URL = "https://cas.example.org:8443/cas/oauth2.0/profile";

    /**
     * 设置Cookie的有效时长（1小时）
     */
    public static int COOKIE_VALID_TIME = 1 * 60 * 60;

    /*
     * 设置Cookie的有效时长（1小时）
     */
    public static String COOKIE_NAME = "UToken";

}
