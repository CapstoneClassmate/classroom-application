'use strict';

var usernamePage = document.querySelector('#username-page');
var roomSelectorPage = document.querySelector("#room-selector")
var chatPage = document.querySelector('#chat-page');
var sessionChooser = document.querySelector('#session-chooser');

var usernameForm = document.querySelector('#usernameForm');
var roomSelectorForm = document.querySelector('#roomSelectorForm');
var sessionForm = document.querySelector('#sessionForm');

var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;
var room = null;
var role = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function setRole(value) {
    if(value === "host") {
        role = "host";
        sessionChooser.classList.add('hidden');
        roomSelectorPage.classList.remove('hidden');
    } else if(value === "member") {
        role = "member";
        sessionChooser.classList.add('hidden');
        roomSelectorPage.classList.remove('hidden');
    }    
}

function roomEntered(event) {
    room = document.querySelector('#room').value.trim();
    if(room) {
        console.log("Room " + room);
        roomSelectorPage.classList.add('hidden');
        usernamePage.classList.remove('hidden');
    }
    event.preventDefault();
}

function connect(event) {
    console.log("hello");
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');
        document.getElementById("chat-header-text").innerText = room;

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
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
        // Subscribe to the individuals room
        stompClient.subscribe('/room/' + room + '/' + username, onMessageReceived);
    }
    // Send the join message for both.
    stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(chatMessage))
    connectingElement.classList.add('hidden');
}

// When user unloads page, notify room that user has left and remove from room
function disconnected() {
   var chatMessage = {
       sender: username,
       type: 'LEAVE',
       content: '',
       roomName: room,
       role: role
   };

   if (role === "host") {
       // Inform users that host left.
       stompClient.send('/app/chat.userLeft', {}, JSON.stringify(chatMessage));
       // Remove the host & users from the room, and terminate the room
       stompClient.send('/app/chat.removeAllUsers', {}, JSON.stringify(chatMessage));
       stompClient.send('/app/chat.terminateRoom', {}, JSON.stringify(chatMessage));
       // Navigate user to home page
       chatPage.classList.add('hidden');
       sessionChooser.classList.remove('hidden');
   } else if (role === "member") {
       // Remove user from room
       stompClient.send('/app/chat.removeUser', {}, JSON.stringify(chatMessage));
       // Send the leave message for both.
       stompClient.send('/app/chat.userLeft', {}, JSON.stringify(chatMessage));
       // Navigate user to home page
       chatPage.classList.add('hidden');
       sessionChooser.classList.remove('hidden');
   }

   connectingElement.classList.add('hidden');
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    console.log(event);
    var val = messageInput.value;
    console.log(val);
    var messageContent = messageInput.value.trim();

    if(messageContent && stompClient) {
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

roomSelectorForm.addEventListener('submit', roomEntered, true);
usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
// window.onbeforeunload = disconnected;
