package othellocrown.account.entity;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.validator.constraints.Length;
import othellocrown.account.common.entity.BaseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import othellocrown.account.common.error.BadRequestException;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Account extends BaseEntity {
    @Length(min = 3, max = 100)
    @Column(nullable = false, unique = true)
    private String email;

    @Length(min = 3, max = 10)
    @Column(nullable = false, unique = true)
    private String username;

    @Length(min = 5, max = 200)
    @Column(nullable = false)
    private String password;

    public Account() {
    }

    public Account(String email, String username, Boolean active) {
        setEmail(email);
        setUsername(username);
    }

    public Account(String email, String username, String password, Boolean active) {
        setEmail(email);
        setUsername(username);
        setPassword(password);
    }

    public void setEmail(String email) {
        String regex = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";

        if (email.length() < 3 || email.length() > 100) {
            throw new BadRequestException("Email length must be between 3 and 100");
        }
        if (!email.matches(regex)) {
            throw new BadRequestException("Wrong Email Format");
        }

        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        String regex = "^[\\w]+$";

        if (username.length() < 3 || username.length() > 10) {
            throw new BadRequestException("Username length must be between 3 and 10");
        }
        if (!username.matches(regex)) {
            throw new BadRequestException("Username can only contain alphanumeric");
        }

        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    private void setPassword(String password) {
        if (password.length() < 5 || password.length() > 200) {
            throw new BadRequestException("Password length must be between 5 and 200");
        }

        this.password = DigestUtils.sha1Hex(password);
    }

    public boolean isPasswordMatch(String password) {
        return this.password.equals(DigestUtils.sha1Hex(password));
    }
}
