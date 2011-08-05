$(document).ready(function(){

	var users = [
		{
			id: 1,
			username: "joe",
			container: "#chatBox1"
		},
		{
			id: 2,
			username: "bob",
			container: "#chatBox2"
		},
		{
			id: 3,
			username: "jane",
			container: "#chatBox3"
		},
		{
			id: 4,
			username: "bert",
			container: "#chatBox4"
		}
	]

	users.forEach(function(user){
		var chatBox = new ChatBox({
			container: $(user.container)
		});

		var client = new ChatClient({
			url: "http://localhost:8081",
			groupId: 329,
			invisible: true,
			user: user,
			onMessage: function(data){
				var u = $.grep(users, function(u) {
					return u.id===data.fromId
				})
				if(u.length>0){
					chatBox.addMessage(u[0].username, data.message)
				}
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

})
