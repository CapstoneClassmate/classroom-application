'use strict';

var usernamePage = document.querySelector('#username-page');
var roomSelectorPage = document.querySelector("#room-selector")
var chatPage = document.querySelector('#chat-page');
var sessionChooserPage = document.querySelector('#session-chooser');
const pages = [usernamePage, roomSelectorPage, chatPage, sessionChooserPage];

var usernameForm = document.querySelector('#usernameForm');
var roomSelectorForm = document.querySelector('#roomSelectorForm');
var sessionForm = document.querySelector('#sessionForm');

var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var socket = null;
var username = null;
var room = null;
var role = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function main() {
    loadState();
}

function loadState() {
    let startPageId = window.localStorage.getItem("startPageId") || "session-chooser";
    let currentPageId = getCurrentPageId();
    if (startPageId == "chat-page") {
        let storedUsername = window.localStorage.getItem("username");
        let storedRoom = window.localStorage.getItem("room");
        let storedRole = window.localStorage.getItem("role");
        if ((storedUsername || storedRoom || storedRole) == undefined) {
            startPageId = "session-chooser";
        } else {
            username = storedUsername;
            room = storedRoom;
            role = storedRole;
            stompConnect();
        }
    }
    if (startPageId != currentPageId) {
        document.querySelector("#" + currentPageId).classList.add("hidden");
        document.querySelector("#" + startPageId).classList.remove("hidden");
    }
}

function sessionEvent(value) {
    if (value === "Host") {
        role = "host";
    } else if (value === "Join") {
        role = "member";
    }
    sessionChooserPage.classList.add('hidden');
    roomSelectorPage.classList.remove('hidden');
}

function roomEntered(event) {
    room = document.querySelector('#room').value.trim();
    if (room) {
        console.log("Room " + room);
        roomSelectorPage.classList.add('hidden');
        usernamePage.classList.remove('hidden');
    }
    event.preventDefault();
}

function userNameFormSubmit(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');
        stompConnect();
    }
    event.preventDefault();
}

function stompConnect() {
    if (!socket) {
        socket = new SockJS('/ws');
    }
    if (!stompClient) {
        stompClient = Stomp.over(socket);
    }
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    var chatMessage = {
        sender: username,
        type: 'JOIN',
        content: '',
        roomName: room,
        role: role
    };

    var roomObject = {
        roomName: room,
        host: username
    };

    if (role === "host") {
        // Create the room
        stompClient.send('/app/chat.createRoom', {}, JSON.stringify(roomObject));
        // Subscribe to the master room
        stompClient.subscribe('/room/' + room, onMessageReceived);
    } else if (role === "member") {
        // Add the user to the room
        stompClient.send('/app/chat.addUser', {}, JSON.stringify(chatMessage));
        // Subscribe to the indivudals room
        stompClient.subscribe('/room/' + room + '/' + username, onMessageReceived);
    }
    // Send the join messsage for both.
    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(chatMessage))
    connectingElement.classList.add('hidden');
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT',
            roomName: room,
            role: role
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function getCurrentPageId() {
    let currentPage = pages.find(page => !page.classList.contains("hidden"));
    if (currentPage === undefined) {
        return undefined;
    } else {
        return currentPage.id;
    }
}

function storeStartPage() {
    window.localStorage.setItem("startPageId", getCurrentPageId());
}

function storeUserDetails() {
    window.localStorage.setItem("username", username);
    window.localStorage.setItem("room", room);
    window.localStorage.setItem("role", role);
}

function onClose(e) {
    storeStartPage();
    storeUserDetails();
    e.preventDefault();
    e.returnValue = '';
}

function viewHomePage() {
    pages.forEach(page => {
        if(!page.classList.contains("hidden")){
            page.classList.add("hidden")
        }
    });
    sessionChooserPage.classList.remove('hidden');
}

function clearSession(e) {
    localStorage.clear();
    viewHomePage();
}

window.addEventListener("beforeunload", onClose, true);
roomSelectorForm.addEventListener('submit', roomEntered, true);
usernameForm.addEventListener('submit', userNameFormSubmit, true)
messageForm.addEventListener('submit', sendMessage, true)
main();