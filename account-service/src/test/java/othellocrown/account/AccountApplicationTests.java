package othellocrown.account;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import othellocrown.account.entity.Account;
import othellocrown.account.model.AccountRegistrationData;
import othellocrown.account.model.Credentials;
import othellocrown.account.model.LoginData;
import othellocrown.account.repository.AccountRepository;

import java.util.Map;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AccountApplication.class,
        properties = "spring.profiles.active=test",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountApplicationTests {
    private Gson gson = new Gson();
    private AccountRegistrationData accountRegistrationData = new AccountRegistrationData(
            "budi@gmail.com", "buditampan", "mypass");

    @Autowired
    private AccountRepository accountRepository;

    @LocalServerPort
    private int port;

    private String token;

    @Before
    public void setBaseUri() {
        RestAssured.baseURI = String.format("http://localhost:%s", port);
    }

    private RequestSpecification newRequest(String credentials, Object body) {
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        if (credentials != null) {
            request.header(new Header("Authorization", credentials));
        }
        if (body != null) {
            request.body(body);
        }
        return request;
    }

    private void guestCanPing() {
        Response response = newRequest(null, null).get("/ping/");

        Assert.assertEquals(200, response.getStatusCode());
    }

    private void guestCanRegister() {
        Response response = newRequest(null, accountRegistrationData).post("/register/");

        Assert.assertEquals(200, response.getStatusCode());

        Optional<Account> maybeAccount = accountRepository.findByUsername(accountRegistrationData.getUsername());
        Assert.assertTrue(maybeAccount.isPresent());

        Account account = maybeAccount.get();
        Assert.assertEquals(account.getEmail(), accountRegistrationData.getEmail());
        Assert.assertEquals(account.getUsername(), accountRegistrationData.getUsername());
    }

    private void guestCantRegisterWrongUsernameFormat() {
        AccountRegistrationData accountRegistrationDataFail = new AccountRegistrationData(
                "budi2@gmail.com", "budi aku", "mypass");
        Response response = newRequest(null, accountRegistrationDataFail).post("/register/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Username can only contain alphanumeric");
    }

    private void guestCantRegisterWrongUsernameLength() {
        AccountRegistrationData accountRegistrationDataFail = new AccountRegistrationData(
                "budi2@gmail.com", "a", "mypass");
        Response response = newRequest(null, accountRegistrationDataFail).post("/register/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Username length must be between 3 and 10");
    }

    private void guestCantRegisterWrongEmailFormat() {
        AccountRegistrationData accountRegistrationDataFail = new AccountRegistrationData(
                "budigmail.com", "budi2", "mypass");
        Response response = newRequest(null, accountRegistrationDataFail).post("/register/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Wrong Email Format");
    }

    private void guestCantRegisterWrongEmailLength() {
        AccountRegistrationData accountRegistrationDataFail = new AccountRegistrationData(
                "om", "budi2", "mypass");
        Response response = newRequest(null, accountRegistrationDataFail).post("/register/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Email length must be between 3 and 100");
    }

    private void guestCantRegisterWrongPasswordLength() {
        AccountRegistrationData accountRegistrationDataFail = new AccountRegistrationData(
                "budi2@gmail.com", "budi2", "m");
        Response response = newRequest(null, accountRegistrationDataFail).post("/register/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Password length must be between 5 and 200");
    }

    private void guestCantRegisterWithSameEmail() {
        AccountRegistrationData accountRegistrationData = new AccountRegistrationData(
                this.accountRegistrationData.getEmail(), "budii", "mypass"
        );
        Response response = newRequest(null, accountRegistrationData).post("/register/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Email is already registered");
    }

    private void guestCantRegisterWithSameUsername() {
        AccountRegistrationData accountRegistrationData = new AccountRegistrationData(
                "other@gmail.com", this.accountRegistrationData.getUsername(), "mypass"
        );
        Response response = newRequest(null, accountRegistrationData).post("/register/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Username is already registered");
    }

    private void guestCantLoginWithWrongUsername() {
        LoginData loginData = new LoginData("hmmm", accountRegistrationData.getPassword());
        Response response = newRequest(null, loginData).post("/login/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Username is not registered");
    }

    private void guestCantLoginWithWrongPassword() {
        LoginData loginData = new LoginData(accountRegistrationData.getUsername(), "password");
        Response response = newRequest(null, loginData).post("/login/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Wrong password");
    }

    private void guestCanLogin() {
        LoginData loginData = new LoginData(accountRegistrationData.getUsername(), accountRegistrationData.getPassword());
        Response response = newRequest(null, loginData).post("/login/");
        Credentials credentials = gson.fromJson(response.getBody().asString(), Credentials.class);
        token = credentials.getToken();

        Assert.assertEquals(200, response.getStatusCode());
    }

    private void guestCantRetrieveProfile() {
        Response response = newRequest(null, null).get("/profile/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(400, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"),
                "Missing request header 'Authorization' for method parameter of type String");
    }

    private void userCanRetrieveProfile() {
        Response response = newRequest(String.format("Bearer %s", token), null).get("/profile/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals(responseBody.get("email"), accountRegistrationData.getEmail());
        Assert.assertEquals(responseBody.get("username"), accountRegistrationData.getUsername());
        Assert.assertNull(responseBody.get("password"));
    }

    private void hackerCantRetrieveProfile() {
        Response response = newRequest("invalid token", null).get("/profile/");

        Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
        Assert.assertEquals(401, response.getStatusCode());
        Assert.assertEquals(responseBody.get("message"), "Your credentials could not be verified");
    }

    @Test
    public void basicFlow() {
        guestCanPing();
        guestCanRegister();
        guestCantRegisterWrongUsernameFormat();
        guestCantRegisterWrongUsernameLength();
        guestCantRegisterWrongEmailFormat();
        guestCantRegisterWrongEmailLength();
        guestCantRegisterWrongPasswordLength();
        guestCantRegisterWithSameEmail();
        guestCantRegisterWithSameUsername();
        guestCantLoginWithWrongUsername();
        guestCantLoginWithWrongPassword();
        guestCanLogin();
        guestCantRetrieveProfile();
        userCanRetrieveProfile();
        hackerCantRetrieveProfile();
    }
}
