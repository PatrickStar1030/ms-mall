package com.dilemma.auth.jwt;


import com.dilemma.auth.config.JwtProperties;
import com.dilemma.auth.entity.UserInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt操作工具类
 */
@Component
@Slf4j
public class JwtOperator {

    @Value("${secret:mall@Login(Auth}*^31)&dilemma%@127.0.0.1:10081@127.0.0.1:10082@127.0.0.1:10083@127.0.0.1:10084@127.0.0.1:10085}")
    private String secret;

    @Value("${expire-time-in-second:1209600}")
    private Long expirationTimeInSecond;
    /**
     * 从token中获取claim
     * @param token token
     * @return
     */
    public Claims getClaimsFromToken(String token){
        try {
            return Jwts.parser()
                    .setSigningKey(this.secret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException e) {
            log.info("token解析错误",e);
            log.info("token值位：{}",token);
            throw new IllegalArgumentException("token invalided");
        }
    }

    /**
     * 获取token过期时间
     * @param token token
     * @return
     */
    public Date getExpirationDateFromToken(String token){
        return getClaimsFromToken(token).getExpiration();
    }


    /**
     * 判断token是否过期
     *
     * @param token token
     * @return 已过期返回true，未过期返回false
     */
    public Boolean isTokenExpired(String token){
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 计算token的过期时间
     *
     * @return 过期时间
     */
    private Date getExpirationTime(){
        return new Date(System.currentTimeMillis() + this.expirationTimeInSecond * 1000);
    }

    /**
     * 为指定用户生成token
     *
     * @param userInfo 填充claims有效载荷
     * @return token
     */
    public String generateToken(UserInfo userInfo){
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",userInfo.getId());
        claims.put("username",userInfo.getUsername());
        Date createTime = new Date();
        Date expirationTime = this.getExpirationTime();
        byte[] keyBytes = this.secret.getBytes();
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(createTime)
                .setExpiration(expirationTime)
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 判断token是否非法
     * @param token token
     * @return 未过期返回true，已经过期false
     */
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }
}
