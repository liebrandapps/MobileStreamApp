
var curPage = "home";
var url = "/api";
var frm;

function initialize() {
    frm = document.getElementById("upload");
    var headline = document.getElementById("h1");

    headline.innerHTML = "Configuration [saved]"

    frm.addEventListener("input", function () {
        headline.innerHTML = "Configuration [modified]";
        //console.log("Form has changed!");
    });
}

function showPage(page) {
    "use strict";
    if (curPage !== null) {
        document.getElementById(curPage).style.display = 'none';
    }
    curPage = page;
    document.getElementById(curPage).style.display = 'flex';
};

function updateCredsList() {

    var params = { "command" : "credlist" };
    var completeUrl = url + formatParams(params)
    var http = new XMLHttpRequest();
    http.open('GET', completeUrl, true);
    http.setRequestHeader('Accept', 'application/json');
    http.send();
    http.onreadystatechange = function() {
        if(this.readyState == 4) {
            if(this.status == 200) {
                var jsn = JSON.parse(this.responseText)
                var  select = document.getElementById('credsList');
                jsn.creds.foreach(id => {
                })
            }
        }
    }


    var opt = document.createElement('option');
        opt.value = i;
        opt.innerHTML = i;
        select.appendChild(opt);
};

var getJSON = function(url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'json';
    xhr.onload = function() {
      var status = xhr.status;
      if (status === 200) {
        callback(null, xhr.response);
      } else {
        callback(status, xhr.response);
      }
    };
    xhr.send();
};


function chkNeedsAuth(chk) {
    var userId;
    var passwordId;
    if (chk.id === "rqCred_1") {
        userId = "authData_user1";
        passwordId = "authData_pw1";
    }
    if (chk.id === "rqCred_2") {
        userId = "authData_user2";
        passwordId = "authData_pw2";
    }
    if (chk.id === "rqCred_3") {
        userId = "authData_user3";
        passwordId = "authData_pw3";
    }
    if (chk.id === "rqCred_4") {
        userId = "authData_user4";
        passwordId = "authData_pw4";
    }
    if (chk.id === "rqCred_5") {
        userId = "authData_user5";
        passwordId = "authData_pw5";
    }
    if (chk.id === "rqCred_6") {
        userId = "authData_user6";
        passwordId = "authData_pw6";
    }
    if (chk.id === "rqCred_7") {
        userId = "authData_user7";
        passwordId = "authData_pw7";
    }
    if (chk.id === "rqCred_8") {
        userId = "authData_user8";
        passwordId = "authData_pw8";
    }
    var trA = document.getElementById(userId);
    var trB = document.getElementById(passwordId);
    if(chk.checked) {
        trA.style.display = "table-row";
        trB.style.display = "table-row";
    }
    else {
        trA.style.display = "none";
        trB.style.display = "none";
    }
}

function copyURL(receiverIp, serviceId, name) {
    var url= "http://" + receiverIp + "/web/stream.m3u?ref=" + encodeURIComponent(serviceId) + "&name=" + encodeURIComponent(name) + "&device=etc";
    window.prompt("To copy to clipboard: Ctrl+C, Enter", url);
}
