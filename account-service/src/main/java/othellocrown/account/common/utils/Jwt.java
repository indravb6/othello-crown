package othellocrown.account.common.utils;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import othellocrown.account.common.error.UnauthorizedException;
import othellocrown.account.entity.Account;

import java.security.Key;

@Component
public class Jwt {

    @Value("${jwt.key}")
    private String privateKey;
    private Gson gson = new Gson();

    public String encode(Account account) {
        Key key = Keys.hmacShaKeyFor(privateKey.getBytes());

        String username = account.getUsername();
        String email = account.getEmail();
        JwtPayload jwtPayload = new JwtPayload(username, email);

        String body = gson.toJson(jwtPayload);

        return Jwts.builder().setPayload(body).signWith(key).compact();
    }

    public Account decode(String authorization) {
        String token = authorization.substring(6);
        Key key = Keys.hmacShaKeyFor(privateKey.getBytes());
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new UnauthorizedException("Your credentials could not be verified");
        }

        String email = claims.get("email", String.class);
        String username = claims.get("username", String.class);

        return new Account(email, username, true);
    }
}
