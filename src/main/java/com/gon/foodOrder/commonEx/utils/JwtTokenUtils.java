package com.sharp.common.utils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtils {
	
	//암복호화에 사용되는 키 값 입니다.
    private @Value("${token.tokenKey}") String secretKey;
    private @Value("${token.accessToken.expireMin}") int accessToken_expireMin;
    private @Value("${token.refreshToken.expireMin}") int refreshToken_expireMin;
	
    public String createAccessToken(String id) {
        Claims claims = Jwts.claims();  //나중에 서버에서 파싱해서 볼 데이터 입니다.
        claims.put("uuid", id);
        Date now = new Date();
        
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) 
                .setClaims(claims) // 데이터를 넣어 줍니다
                .setIssuedAt(now)   // 토큰 발행 일자
                .setExpiration(new Date(now.getTime() + (1000L * 60 * accessToken_expireMin))) // 만료 기간 입니다
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘과 암복호화에 사용할 키를 넣어줍니다
                .compact(); // Token 생성
    }    
    
    // Jwt Token의 유효성 및 만료 기간 검사합니다
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey( secretKey ).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    // Jwt Token에서 데이터를 전달 합니다.
    public Claims getInformation(String token) {
        Claims claims =Jwts.parser().setSigningKey( secretKey ).parseClaimsJws(token).getBody();
        return claims;
    }  
    
    public String createrRefreshToken() {
        Date now = new Date();
        
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) 
                .setIssuedAt(now)   // 토큰 발행 일자
                .setExpiration(new Date(now.getTime() + (1000L * 60 * refreshToken_expireMin))) // 만료 기간 입니다
                .signWith(SignatureAlgorithm.HS256,  secretKey ) // 암호화 알고리즘과 암복호화에 사용할 키를 넣어줍니다
                .compact(); // Token 생성    	
    }

}
