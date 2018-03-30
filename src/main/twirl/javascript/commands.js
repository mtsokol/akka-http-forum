function modifyTopic(id) {

}

function deleteTopic(id) {
    var xhr = new XMLHttpRequest();
    xhr.open('DELETE', 'http://localhost:9000/topics/' + id, true);
    var secr = 'secret-' + id;
    var msg = 'msg-' + id;
    var lab = 'label-' + id;
    var button = 'button-' + id;
    var button_del = 'button-del' + id;
    var secret = document.getElementById(secr).value;
    xhr.setRequestHeader("WWW-Authenticate", secret);
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 204) {
            document.getElementById(lab).innerText = 'Topic deleted';
            document.getElementById(msg).innerText = xhr.responseText;
            document.getElementById(button).addEventListener('click', function(){
                window.location.href = "http://localhost:9000/topics";
            });
            document.getElementById(secr).style.display = 'none';
            document.getElementById(button_del).style.display = 'none';
        } else if(xhr.status === 401) {
            document.getElementById(lab).innerText = 'Invalid secret';
            document.getElementById(msg).innerText = xhr.responseText;
        } else {
            document.getElementById(lab).innerText = 'Internal error';
            document.getElementById(msg).innerText = xhr.responseText;
        }
    };
    xhr.send();
}

function form_to_json() {
    var a = document.getElementById("nickname").value;
    var b = document.getElementById("email").value;
    var c = document.getElementById("subject").value;
    var d = document.getElementById("content").value;
    var json = `{"user":{"nick":"${a}","email":"${b}"},"subject":"${c}","content":"${d}"}`;
    return json
}

function send_request(url) {

    var routing = url;
    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:9000/' + routing, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 201) {
            document.getElementById('label').innerText = 'Your secret';
            document.getElementById('msg').innerText = xhr.responseText;
            document.getElementById('cancel').addEventListener('click', function(){
                window.location.href = "http://localhost:9000/topics";
            });
            $('#myModal').modal('show');
        } else if(xhr.status === 401) {
            document.getElementById('label').innerText = 'Invalid input';
            document.getElementById('msg').innerText = xhr.responseText;
            $('#myModal').modal('show');
        } else {
            document.getElementById('label').innerText = 'Internal error';
            document.getElementById('msg').innerText = xhr.responseText;
            $('#myModal').modal('show');
        }
    };
    xhr.send(form_to_json());
}