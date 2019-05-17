package othellocrown.account;

import org.springframework.web.bind.annotation.*;
import othellocrown.account.common.error.BadRequestException;
import othellocrown.account.common.utils.Jwt;
import othellocrown.account.entity.Account;
import othellocrown.account.model.AccountRegistrationData;
import othellocrown.account.model.Credentials;
import othellocrown.account.model.LoginData;
import othellocrown.account.repository.AccountRepository;

import java.util.Optional;

@RestController
@CrossOrigin
public class AccountController {

    private Jwt jwt;
    private AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository, Jwt jwt) {
        this.accountRepository = accountRepository;
        this.jwt = jwt;
    }

    @PostMapping("/register/")
    public void register(@RequestBody AccountRegistrationData accountRegistrationData) {
        String username = accountRegistrationData.getUsername();
        Optional<Account> maybeAccount = accountRepository.findByUsername(username);
        if (maybeAccount.isPresent()) {
            throw new BadRequestException("Username is already registered");
        }

        String email = accountRegistrationData.getEmail();
        maybeAccount = accountRepository.findByEmail(email);
        if (maybeAccount.isPresent()) {
            throw new BadRequestException("Email is already registered");
        }

        String password = accountRegistrationData.getPassword();
        Account account = new Account(email, username, password, false);

        accountRepository.save(account);
    }

    @PostMapping("/login/")
    public Credentials login(@RequestBody LoginData loginData) {
        Optional<Account> maybeAccount = accountRepository.findByUsername(loginData.getUsername());
        if (!maybeAccount.isPresent()) {
            throw new BadRequestException("Username is not registered");
        }

        Account account = maybeAccount.get();
        if (!account.isPasswordMatch(loginData.getPassword())) {
            throw new BadRequestException("Wrong password");
        }

        return new Credentials(jwt.encode(account));
    }

    @GetMapping("/profile/")
    public Account profile(@RequestHeader(value = "Authorization") String token) {
        return jwt.decode(token);
    }

    @GetMapping("/ping/")
    public void ping() {

    }
}
