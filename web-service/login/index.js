function onLoginSuccess(data) {
  $("#login-button").prop("disabled", false);
  $("#login-button").html("Login");
  if (data) {
    localStorage.setItem("token", data.token);
    localStorage.setItem("username", $("#login-username").val());
    if (getQuery("next")) {
      window.location.href = getQuery("next");
    } else {
      window.location.href = "/game/";
    }
  }
}

function login() {
  const username = $("#login-username").val();
  const password = $("#login-password").val();
  const data = {
    username,
    password
  };
  $("#login-button").prop("disabled", true);
  $("#login-button").html("Logging in...");
  accountService.post("login/", data, onLoginSuccess, () => {
    $("#login-button").prop("disabled", false);
    $("#login-button").html("Login");
  });
}

function onRegisterSuccess(data) {
  $("#register-username").val("");
  $("#register-email").val("");
  $("#register-password").val("");
  notifSuccess("Register Success!");
  showLogin();
}

function register() {
  const username = $("#register-username").val();
  const email = $("#register-email").val();
  const password = $("#register-password").val();
  const data = {
    email,
    password,
    username
  };
  accountService.post("register/", data, onRegisterSuccess);
}

$(document).ready(() => {
  if (localStorage.getItem("token")) {
    window.location.href = "/game/";
    return;
  }
  $(".login-form").keypress(e => (e.which === 13 ? login() : null));
  $(".register-form").keypress(e => (e.which === 13 ? register() : null));
  accountService.get("ping/", {}, () => {}, () => {});
  gameService.get("ping/", {}, () => {}, () => {});
  scoreboardService.get("ping/", {}, () => {}, () => {});
});

function showRegister() {
  $("#login-section").hide();
  $("#register-section").show();
}

function showLogin() {
  $("#register-section").hide();
  $("#login-section").show();
}
