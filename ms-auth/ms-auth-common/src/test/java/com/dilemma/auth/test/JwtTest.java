package com.dilemma.auth.test;

import com.dilemma.auth.entity.UserInfo;
import com.dilemma.auth.utils.JwtUtils;
import com.dilemma.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {
    private static final String pubKeyPath = "D:/tmp/rsa/rsa.pub";

    private static final String priKeyPath = "D:/tmp/rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4NzAyNDI5OH0.W2JvFmdvVFD0EPxVyFIiMjE3jL00s5Fi_UwqLArHkOgnRTg0VgOcXqyvFtRsms1PRATHwm7d85WN5oINOTItSeSwA6ExO1mXoLhQQDLVpg5Ra97gVZEIrlo_bms2aLKZ8hwZECb45lOtiCB91ank482mZvwl9pM7vsmch1xEK0c";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
