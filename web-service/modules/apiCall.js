function getAuthorization() {
  return "Bearer " + localStorage.getItem("token");
}

function get(service, url, cb, cbError) {
  const newUrl = config[service] + url;
  consumeApi(newUrl, "GET", null, cb, cbError);
}

function post(service, url, body, cb, cbError) {
  const newUrl = config[service] + url;
  consumeApi(newUrl, "POST", body, cb, cbError);
}

const accountService = {
  get: (url, cb, cbError) => get("account", url, cb, cbError),
  post: (url, body, cb, cbError) => post("account", url, body, cb, cbError)
};

const gameService = {
  get: (url, cb, cbError) => get("game", url, cb, cbError),
  post: (url, body, cb, cbError) => post("game", url, body, cb, cbError)
};

const scoreboardService = {
  get: (url, cb, cbError) => get("scoreboard", url, cb, cbError)
};

function consumeApi(url, method, data, cb, cbError) {
  let headers = {};
  if (localStorage.getItem("token") !== null) {
    headers["Authorization"] = getAuthorization();
  }

  $.ajax({
    url: url,
    type: method,
    data: JSON.stringify(data),
    headers: headers,
    datatype: "json",
    contentType: "application/json",
    success: function(data) {
      cb(data);
    },
    error: function(jqXHR, textStatus, errorThrown) {
      onError(jqXHR.responseJSON);
      cbError();
    }
  });
}

function onError(body) {
  if (
    body.status === 401 ||
    body.message ===
      "Missing request header 'Authorization' for method parameter of type String"
  ) {
    localStorage.removeItem("token");
    window.location.href = "/login/";
  } else {
    notifError(body.message);
  }
}

function notifError(text) {
  $.notify(
    {
      message: text
    },
    {
      type: "danger",
      placement: {
        from: "top",
        align: "center"
      },
      delay: 600,
      timer: 200
    }
  );
}

function notifSuccess(text) {
  $.notify(
    {
      message: text
    },
    {
      type: "success",
      placement: {
        from: "top",
        align: "center"
      },
      delay: 200,
      timer: 100
    }
  );
}

function getQuery(name) {
  var results = new RegExp("[?&]" + name + "=([^&#]*)").exec(
    window.location.search
  );

  return results !== null ? results[1] || 0 : false;
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "/login/";
}
