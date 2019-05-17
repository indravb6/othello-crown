package othellocrown.scoreboard;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.http.Header;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import othellocrown.scoreboard.entity.Scoreboard;
import othellocrown.scoreboard.model.ScoreboardCreateData;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScoreboardApplication.class,
        properties = "spring.profiles.active=test",
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScoreboardApplicationTests {

	private Gson gson = new Gson();
	private ScoreboardCreateData scoreboardCreateData = new ScoreboardCreateData("budi", true);

	@Value("${appKey}")
	private String appKey;

	@LocalServerPort
	private int port;

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

	public void canPing() {
		Response response = newRequest(null, null).get("/ping/");

		Assert.assertEquals(200, response.statusCode());
	}

	public void cantCreateScoreboardWithoutAppKeyHeader() {
		Response response = newRequest(null, scoreboardCreateData).post("/scoreboards/");

		Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
		Assert.assertEquals(400, response.statusCode());
		Assert.assertEquals("Missing request header 'Authorization' for method parameter of type String", responseBody.get("message"));
	}

	public void cantCreateScoreboardWithInvalidAppKeyHeader() {
		Response response = newRequest("hehe", scoreboardCreateData).post("/scoreboards/");

		Map responseBody = gson.fromJson(response.getBody().asString(), Map.class);
		Assert.assertEquals(401, response.statusCode());
		Assert.assertEquals("Credential invalid!", responseBody.get("message"));
	}

	public void canCreateNewScoreboard() {
		Response response = newRequest(appKey, scoreboardCreateData).post("/scoreboards/");

		Scoreboard scoreboard = gson.fromJson(response.getBody().asString(), Scoreboard.class);
		Assert.assertEquals(200, response.statusCode());
		Assert.assertEquals(scoreboardCreateData.getUsername(), scoreboard.getUsername());
		Assert.assertEquals(25, scoreboard.getScore());
		Assert.assertEquals(1, scoreboard.getTotalWin());
		Assert.assertEquals(0, scoreboard.getTotalDefeat());
	}

	public void canUpdateScoreboardIfUsernameExistThenAdd100ScoreToWinner() {
		Response response = newRequest(appKey, scoreboardCreateData).post("/scoreboards/");

		Scoreboard scoreboard = gson.fromJson(response.getBody().asString(), Scoreboard.class);
		Assert.assertEquals(200, response.statusCode());
		Assert.assertEquals(scoreboardCreateData.getUsername(), scoreboard.getUsername());
		Assert.assertEquals(50, scoreboard.getScore());
		Assert.assertEquals(2, scoreboard.getTotalWin());
		Assert.assertEquals(0, scoreboard.getTotalDefeat());
	}

	public void canUpdateScoreboardIfUsernameExistThenMinus100ScoreToWinner() {
		ScoreboardCreateData newScoreboardCreateData = new ScoreboardCreateData(scoreboardCreateData.getUsername(), false);
		Response response = newRequest(appKey, newScoreboardCreateData).post("/scoreboards/");

		Scoreboard scoreboard = gson.fromJson(response.getBody().asString(), Scoreboard.class);
		Assert.assertEquals(200, response.statusCode());
		Assert.assertEquals(scoreboardCreateData.getUsername(), scoreboard.getUsername());
		Assert.assertEquals(25, scoreboard.getScore());
		Assert.assertEquals(2, scoreboard.getTotalWin());
		Assert.assertEquals(1, scoreboard.getTotalDefeat());
	}

	public void canGetTop10Scoreboard() {
		Response response = newRequest(null, null).get("/scoreboards/");
		Assert.assertEquals(200, response.statusCode());
	}

	@Test
	public void basicFlow() {
		canPing();
		cantCreateScoreboardWithoutAppKeyHeader();
		cantCreateScoreboardWithInvalidAppKeyHeader();
		canCreateNewScoreboard();
		canUpdateScoreboardIfUsernameExistThenAdd100ScoreToWinner();
		canUpdateScoreboardIfUsernameExistThenMinus100ScoreToWinner();
		canGetTop10Scoreboard();
	}

}
