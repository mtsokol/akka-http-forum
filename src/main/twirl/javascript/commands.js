function performAction(method, contentType, topicID, answerID) {
    let xhr = new XMLHttpRequest();
    let url;
    let id;
    if (contentType === 'answer') {
        url = topicID + '/' + 'answers' + '/' + answerID;
        id = answerID;
    } else {
        id = topicID;
        url = 'topics/' + topicID
    }
    let secret;
    let msg;
    let label;
    let button;
    let button_del;

    if (method === 'DELETE') {
        secret = 'del-secret-' + id;
        msg = 'del-msg-' + id;
        label = 'del-label-' + id;
        button = 'del-button-' + id;
        button_del = 'del-button-action-' + id;
    } else if (method === 'PUT') {
        secret = 'mod-secret-' + id;
        msg = 'mod-msg-' + id;
        label = 'mod-label-' + id;
        button = 'mod-button-' + id;
        button_del = 'mod-button-action-' + id;
    }

    xhr.open(method, url, true);
    let secret_value = document.getElementById(secret).value;
    xhr.setRequestHeader("WWW-Authenticate", secret_value);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && (xhr.status === 204 || xhr.status === 201)) {
            if (method === 'DELETE') {
                document.getElementById(label).innerText = contentType + ' deleted';
            } else {
                document.getElementById(label).innerText = contentType + ' modified';
            }
            document.getElementById(msg).innerText = xhr.responseText;
            document.getElementById(button).addEventListener('click', function () {
                window.location.href = "/topics";
            });
            document.getElementById(secret).style.display = 'none';
            document.getElementById(button_del).style.display = 'none';
        } else if (xhr.status === 401) {
            document.getElementById(msg).innerText = xhr.responseText;
        } else {
            document.getElementById(label).innerText = 'Internal error';
            document.getElementById(msg).innerText = xhr.responseText;
        }
    };

    if (method === 'DELETE') {
        xhr.send();
    } else {
        let newContent = 'mod-content-' + id;
        let content = document.getElementById(newContent).value;
        xhr.send(content);
    }
}

function formToJSON(contentType) {
    let a = document.getElementById("nickname").value;
    let b = document.getElementById("email").value;
    let c = "\"\"";
    if (contentType === 'topics') {
        c = document.getElementById("subject").value;
        c = JSON.stringify(c)
    }
    let d = document.getElementById("content").value;
    d = JSON.stringify(d);
    return `{"user":{"nick":"${a}","email":"${b}"},"subject":${c},"content":${d}}`
}

function sendRequest(url) {
    let xhr = new XMLHttpRequest();
    xhr.open('POST', '/' + url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 201) {
            document.getElementById('label').innerText = 'Your secret';
            document.getElementById('msg').innerText = xhr.responseText;
            document.getElementById('cancel').addEventListener('click', function(){
                window.location.href = "/topics";
            });
            $('#myModal').modal({backdrop: 'static', keyboard: false});
        } else if (xhr.status === 400) {
            document.getElementById('label').innerText = 'Invalid input';
            document.getElementById('msg').innerText = xhr.responseText;
            $('#myModal').modal({backdrop: 'static', keyboard: false});
        } else {
            document.getElementById('label').innerText = 'Internal error';
            document.getElementById('msg').innerText = xhr.responseText;
            $('#myModal').modal({backdrop: 'static', keyboard: false});
        }
    };
    xhr.send(formToJSON(url));
}
