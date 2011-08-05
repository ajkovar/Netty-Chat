$(document).ready(function(){
	var chatBox = new ChatBox({
		container: $("#chatBox")
	});
	var client = new ChatClient({
		url: "http://localhost:8081",
		groupId: 329,
		invisible: true,
		user: {
			id:2,
			username:"joe"
		},
		onMessage: function(data){
			chatBox.addMessage("them", data.message)
		},
		onUserJoin: function(user) {
		},
		onMemberLeave: function(user){
		}
	});

	chatBox.onSend = function(message){
		client.sendMessage({
			to: 1,
			message: message 
		});
	}

	client.connect();
})
