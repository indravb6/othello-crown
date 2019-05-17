var stompClient = null;
var socket = null;

var spectatorToken = null;
var invitationToken = null;
var roomId = null;
var status = null;
var player1 = null;
var player2 = null;

function connect() {
  socket = new SockJS(config.game + "ws");
  stompClient = Stomp.over(socket);
  stompClient.connect({}, () => {
    stompClient.subscribe("/watch/" + spectatorToken, onGetMessage);
    stompClient.subscribe("/chat/" + spectatorToken, onGetChat);
  });
}

function onGetChat(message) {
  const data = JSON.parse(message.body);
  onNewChat(data.from, data.message);
}

function announcementChat(message) {
  onNewChat(
    "announcement",
    message,
    "https://apps.shopifycdn.com/listing_images/c1451d8b1a379ebf9a40dc434ffb121e/icon/037669611c38120e86b353ba626f4b5f.jpg",
    true
  );
}

function onGetMessage(message) {
  const data = JSON.parse(message.body);
  if (data.gameStatus === "GAME_STARTED") {
    player1 = data.player1;
    player2 = data.player2;
    onGameStarted();
    announcementChat("Game started. " + player1 + " VS " + player2);
  } else if (data.gameStatus === "PLAYER1_TURN") {
    if ($("#player1").hasClass("get-turn")) {
      announcementChat("Pass. " + player2 + " can't move this turn");
    }
    $("#player1").addClass("get-turn");
    $("#player2").removeClass("get-turn");
  } else if (data.gameStatus === "PLAYER2_TURN") {
    if ($("#player2").hasClass("get-turn")) {
      announcementChat("Pass. " + player1 + " can't move this turn");
    }
    $("#player1").removeClass("get-turn");
    $("#player2").addClass("get-turn");
  } else if (
    data.gameStatus === "PLAYER1_LEFT" ||
    data.gameStatus === "PLAYER2_LEFT"
  ) {
    const playerName = data.gameStatus === "PLAYER1_LEFT" ? player1 : player2;
    $("#game-information").html(playerName + " has left the game");
    $("#board").css("filter", " grayscale(70%)");
    announcementChat(playerName + " has left the game");
  } else if (data.gameStatus === "GAME_OVER_PLAYER1_WINNER") {
    $("#game-information").html("Game over, " + player1 + " wins!");
    announcementChat("Game over, " + player1 + " wins!");
  } else if (data.gameStatus === "GAME_OVER_PLAYER2_WINNER") {
    $("#game-information").html("Game over, " + player2 + " wins!");
    announcementChat("Game over, " + player2 + " wins!");
  } else if (data.gameStatus === "GAME_OVER_DRAW") {
    $("#game-information").html("Game over, DRAW!");
    announcementChat("Game over, DRAW!");
  }
  render(data.boxes);
}

function onGameStarted() {
  $("#board").css("filter", "initial");

  $(".will-available-black-circle").addClass("available-black-circle");
  $(".will-available-white-circle").addClass("available-white-circle");
  $(".will-available-black-circle").removeClass("will-available-black-circle");
  $(".will-available-white-circle").removeClass("will-available-black-circle");

  $(".square .available-white-circle").addClass("clickable");

  $("#game-information").html(
    "<b id='player1'>" + player1 + "</b> VS <b id='player2'>" + player2 + "</b>"
  );

  $("#start-button").hide();
  $("#invite-button").hide();
  $("#small-leave-button").hide();
  $("#small-copy-spectator").show();
  $("#leave-button").show();
}

function renderInitialBoard() {
  var initBoardData = [
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"],
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"],
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"],
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"],
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"],
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"],
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"],
    ["EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY", "EMPTY"]
  ];

  render(initBoardData);
}

$(document).ready(() => {
  setInterval(() => {
    gameService.get("ping/", {}, () => {}, () => {});
  }, 60000);
  $("#chat-input").keypress(e => (e.which === 13 ? sendChat() : null));
  checkAuth();
  renderInitialBoard();
  if (checkWatchGame()) return;
  getOnlineStorage(() => {
    if (roomId) {
      if (player2) {
        onGameStarted();
      } else {
        onWaiting();
      }
      connect();
    } else {
      checkInvitedGame();
    }
  });
});

function checkWatchGame() {
  spectatorToken = getQuery("watch");
  if (!spectatorToken) return false;
  gameService.post("watch/" + spectatorToken + "/", {}, onWatchGame);
  return true;
}

function onWatchGame(data) {
  player1 = data.player1;
  player2 = data.player2;
  status = "spectator";

  $("#board").css("filter", "initial");
  $("#start-button").hide();
  $("#invite-button").hide();
  $("#small-leave-button").hide();
  $("#leave-button").show();
  if (!player2) {
    $("#game-information").html("Waiting player 2");
  } else {
    $("#game-information").html(
      "<b id='player1'>" +
        player1 +
        "</b> VS <b id='player2'>" +
        player2 +
        "</b>"
    );
  }

  connect();
}

function getOnlineStorage(cb) {
  gameService.get("storage/", data => {
    roomId = data.roomId;
    player1 = data.player1;
    player2 = data.player2;
    status = data.status;
    invitationToken = data.invitationToken;
    spectatorToken = data.spectatorToken;
    cb();
  });
}

function checkAuth() {
  if (!localStorage.getItem("token")) {
    window.location.href = "/login/?next=" + window.location.href;
    return;
  }
}

function onWaiting() {
  $("#board").css("filter", " grayscale(70%)");
  $("#game-information").html("Waiting for your opponent...");

  $("#start-button").hide();
  $("#leave-button").hide();
  $("#invite-button").show();
  $("#small-leave-button").show();

  prefixLink = window.location.origin + "/game/";
  $("#opponent-link").val(prefixLink + "?join=" + invitationToken);
  $("#spectator-link").val(prefixLink + "?watch=" + spectatorToken);
  $("#line-opponent").attr("data-url", prefixLink + "?join=" + invitationToken);
  $("#line-spectator").attr(
    "data-url",
    prefixLink + "?watch=" + spectatorToken
  );
}

function checkInvitedGame() {
  invitationToken = getQuery("join");
  if (!invitationToken) return;
  gameService.post("join/" + invitationToken + "/", {}, onInvitedGame);
}

function onInvitedGame(data) {
  spectatorToken = data.spectatorToken;
  player1 = data.player1;
  player2 = data.player2;
  roomId = data.roomId;
  status = "player2";
  onGameStarted();
  connect();
}

var linkCopied = {
  opponent: null,
  spectator: null
};
function copyLink(type) {
  const copyText = document.getElementById(type + "-link");
  copyText.select();
  document.execCommand("copy");
  if (linkCopied[type]) clearInterval(linkCopied[type]);
  $("#" + type + "-link-copied").show();
  linkCopied[type] = setInterval(() => {
    $("#" + type + "-link-copied").fadeOut();
    clearInterval(linkCopied[type]);
  }, 1000);
}

function copySpectatorLink() {
  prefixLink = window.location.origin + "/game/";
  var copyText = document.createElement("textarea");
  copyText.style.position = "fixed";
  copyText.style.top = 0;
  copyText.style.left = 0;
  copyText.style.width = "2em";
  copyText.style.height = "2em";
  copyText.style.background = "transparent";
  copyText.value = prefixLink + "?watch=" + spectatorToken;
  document.body.appendChild(copyText);
  copyText.select();
  document.execCommand("copy");
  document.body.removeChild(copyText);
  notifSuccess("Link copied!");
}

const pressLog = [];
function press(x, y) {
  const payload = {
    token: getAuthorization(),
    roomId,
    coordinate: { currentRow: x, currentColumn: y }
  };
  pressLog.push({ x, y });
  stompClient.send("/game/play", {}, JSON.stringify(payload));
}

function render(data) {
  let board = "";
  $("#board").html("");
  const isDisplay = player1 && player2 ? "" : "will-";
  for (let i = 0; i < data.length; i++) {
    board += "<div class='board-row'>";
    for (let j = 0; j < data[i].length; j++) {
      if (data[i][j] == "WHITE") {
        board += "<div class='square'><div class='white-circle'></div></div>";
      } else if (data[i][j] == "BLACK") {
        board += "<div class='square'><div class='black-circle'></div></div>";
      } else if (data[i][j] == "CLICKABLE_WHITE" && status == "player2") {
        board +=
          "<div class='square'><div class='" +
          isDisplay +
          "available-white-circle' onclick='press(" +
          i +
          "," +
          j +
          ")'></div></div>";
      } else if (data[i][j] == "CLICKABLE_BLACK" && status == "player1") {
        board +=
          "<div class='square'><div class='" +
          isDisplay +
          "available-black-circle' onclick='press(" +
          i +
          "," +
          j +
          ")'></div></div>";
      } else {
        board += "<div class='square'></div>";
      }
    }
    board += "</div>";
  }
  $("#board").append(board);
}

function createNewGame() {
  $("#game-information").html("Creating a new game...");
  $("#start-button").prop("disabled", true);
  gameService.post("create/", {}, onCreatedNewGame);
}

function onCreatedNewGame(data) {
  status = "player1";

  prefixLink = window.location.origin + "/game/";

  invitationToken = data.invitationToken;
  spectatorToken = data.spectatorToken;
  roomId = data.roomId;

  onWaiting();
  $("#invite-button").click();

  connect();
}

function leave() {
  if (
    (status !== "player1" && status !== "player2") ||
    $("#game-information")
      .html()
      .indexOf(" has left the game") !== -1
  ) {
    window.location.href = "/game/";
    return;
  }

  gameService.post(
    "leave/",
    {},
    () => {
      window.location.href = "/game/";
    },
    () => {
      window.location.href = "/game/";
    }
  );
}

function getUsername() {
  if (status === "player1") return player1;
  else if (status === "player2") return player2;
  else return localStorage.getItem("username");
}

function onNewChat(from, chat, profilePic, isAnnouncement) {
  let newMessage = "";
  newMessage += `<div class="chat ${
    from === getUsername() ? "self" : "friend"
  }">`;
  newMessage += `<div class="user-photo"> <img src="${profilePic ||
    `https://api.adorable.io/avatars/60/${from}.png`}" alt=""> </div>`;

  if (from !== getUsername()) {
    newMessage += `<div><span class='chat-username'>${from}</span>`;
  }
  newMessage += `<p class="chat-message ${
    isAnnouncement ? "announcement" : ""
  }">${sanitizeHTML(chat)}</p>`;
  if (from !== getUsername()) {
    newMessage += "</div>";
  }

  newMessage += "</div>";
  $(".chatlogs").append(newMessage);
  $(".chatlogs").scrollTop($(".chatlogs")[0].scrollHeight);
}

function sendChat() {
  const message = $("#chat-input").val();
  const payload = { token: getAuthorization(), message };
  stompClient.send("/game/chat/" + spectatorToken, {}, JSON.stringify(payload));
  $("#chat-input").val("");
}

var sanitizeHTML = function(str) {
  const temp = document.createElement("div");
  temp.textContent = str;
  return temp.innerHTML;
};

function fetchScoreboard() {
  scoreboardService.get("scoreboards/", data => {
    let body = "";
    let i = 1;
    data.forEach(row => {
      body += "<tr>";
      body += `<td>${i++}</td>`;
      body += `<td>${row.username}</td>`;
      body += `<td>${row.score}</td>`;
      body += `<td>${row.totalWin}</td>`;
      body += `<td>${row.totalDefeat}</td>`;
      body += "</tr>";
    });
    const table = `<table class="table table-hover">
    <thead>
      <tr>
        <th>Rank</th>
        <th>Username</th>
        <th>Score</th>
        <th>Win</th>
        <th>Defeat</th>
      </tr>
    </thead>
    <tbody>
    ${body}
    </tbody>
  </table>`;

    $("#scoreboard").html(table);
  });
}
