function perform_action(method, contentType, topicID, answerID) {
    let xhr = new XMLHttpRequest();
    let  url = 'http://localhost:9000/topics/' + topicID;
    let id;
    if(contentType === 'answer') {
        url = url + '/' + 'answers' + '/' + answerID;
        id = answerID;
    } else {
        id = topicID;

    }
    let secr = 'secret-' + id;
    let msg = 'msg-' + id;
    let lab = 'label-' + id;
    let button = 'button-' + id;
    let button_del = 'button-action-' + id;

    if(method === 'DELETE') {
        secr = 'del-secret-' + id;
        msg = 'del-msg-' + id;
        lab = 'del-label-' + id;
        button = 'del-button-' + id;
        button_del = 'del-button-action-' + id;
    } else if(method === 'PUT') {
        secr = 'mod-secret-' + id;
        msg = 'mod-msg-' + id;
        lab = 'mod-label-' + id;
        button = 'mod-button-' + id;
        button_del = 'mod-button-action-' + id;
    }

    xhr.open(method, url, true);
    let secret = document.getElementById(secr).value;
    xhr.setRequestHeader("WWW-Authenticate", secret);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && (xhr.status === 204 || xhr.status === 201 )) {
            if(method === 'DELETE') {
                document.getElementById(lab).innerText = contentType + ' deleted';
            } else {
                document.getElementById(lab).innerText = contentType + ' modified';
            }
            document.getElementById(msg).innerText = xhr.responseText;
            document.getElementById(button).addEventListener('click', function(){
                window.location.href = "http://localhost:9000/topics";
            });
            document.getElementById(secr).style.display = 'none';
            document.getElementById(button_del).style.display = 'none';
        } else if(xhr.status === 401) {
            document.getElementById(msg).innerText = xhr.responseText;
        } else {
            document.getElementById(lab).innerText = 'Internal error';
            document.getElementById(msg).innerText = xhr.responseText;
        }
    };

    if(method === 'DELETE') {
        xhr.send();
    } else {
        let newContent = 'mod-content-' + id;
        let content = document.getElementById(newContent).value;
        xhr.send(content);
    }
}

function form_to_json(contentType) {
    let a = document.getElementById("nickname").value;
    let b = document.getElementById("email").value;
    let c = "";
    if (contentType === 'topics') {
        c = document.getElementById("subject").value;
    }
    let d = document.getElementById("content").value;
    return `{"user":{"nick":"${a}","email":"${b}"},"subject":"${c}","content":"${d}"}`
}

function send_request(url) {
    let routing = url;
    let xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:9000/' + routing, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 201) {
            document.getElementById('label').innerText = 'Your secret';
            document.getElementById('msg').innerText = xhr.responseText;
            document.getElementById('cancel').addEventListener('click', function(){
                window.location.href = "http://localhost:9000/topics";
            });
            $('#myModal').modal({backdrop: 'static', keyboard: false});
        } else if(xhr.status === 401) {
            document.getElementById('label').innerText = 'Invalid input';
            document.getElementById('msg').innerText = xhr.responseText;
            $('#myModal').modal({backdrop: 'static', keyboard: false});
        } else {
            document.getElementById('label').innerText = 'Internal error';
            document.getElementById('msg').innerText = xhr.responseText;
            $('#myModal').modal({backdrop: 'static', keyboard: false});
        }
    };
    xhr.send(form_to_json(url));
}

function getHost(path) {
    return location.host + path
}
