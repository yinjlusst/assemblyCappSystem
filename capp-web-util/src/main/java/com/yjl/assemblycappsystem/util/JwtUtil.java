package com.yjl.assemblycappsystem.util;

import io.jsonwebtoken.*;

import java.util.Map;

public class JwtUtil {

    /**
     * 加密算法
     *
     * @param key   服务器秘钥
     * @param param 用户的基本信息
     * @param salt  盐值，在不同的时间time，浏览器ip 盐值是不同的
     *              上面三个参数组合在一起，为token
     * @return
     */
    public static String encode(String key, Map<String, Object> param, String salt) {
        if (salt != null) {
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }


    /**
     * 解密算法
     *
     * @param token
     * @param key
     * @param salt
     * @return
     */
    public static Map<String, Object> decode(String token, String key, String salt) {
        Claims claims = null;
        if (salt != null) {
            key += salt;
        }
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            return null;
        }
        return claims;
    }
}
