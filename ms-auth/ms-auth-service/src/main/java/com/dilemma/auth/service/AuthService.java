package com.dilemma.auth.service;

import com.dilemma.auth.client.UserClient;
import com.dilemma.auth.entity.UserInfo;
import com.dilemma.auth.jwt.JwtOperator;
import com.dilemma.user.pojo.User;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtOperator operator;

    public String authentication(String username, String password) {
        try {
            User user = this.userClient.queryUser(username, password);
            if (user == null) {
                return null;
            }
            return operator.generateToken(new UserInfo(user.getId(),user.getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserInfo verifyUser(String token){
        Claims claims = operator.getClaimsFromToken(token);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(Long.valueOf(claims.get("userId").toString()));
        userInfo.setUsername((String) claims.get("username"));
        return userInfo;
    }
}
