package othellocrown.game.common.utils;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import othellocrown.game.common.error.UnauthorizedException;

import java.security.Key;

@Component
public class Jwt {

    @Value("${jwt.key}")
    private String privateKey;
    private Gson gson = new Gson();

    public String encode(String username) {
        Key key = Keys.hmacShaKeyFor(privateKey.getBytes());
        JwtPayload jwtPayload = new JwtPayload(username);
        String body = gson.toJson(jwtPayload);
        return Jwts.builder().setPayload(body).signWith(key).compact();
    }

    public String decode(String authorization) {
        String token = authorization.substring(6);
        Key key = Keys.hmacShaKeyFor(privateKey.getBytes());
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new UnauthorizedException("Your credentials could not be verified");
        }

        return claims.get("username", String.class);
    }
}
