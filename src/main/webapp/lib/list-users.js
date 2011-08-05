$(document).ready(function(){
	var chatBar = new ChatBar({
		id:1,
		username:"joe"
	});

	var chatBar = $(".chat-bar");

	var client = new ChatClient({
		url: "http://localhost:8081",
		groupId: 329,
		invisible: true,
		user: {
			id:1,
			username:"joe"
		}
	});

	client.connect();

	client.getUsers(function(users){
		users.forEach(function(user){
			var user = $("<div>")
				.html(user.username)
				.click(function(){
					var container = $("<div>", {"class": "chat-box"});
					chatBar.append(container)
					new Chat({
						container: container,
						chatClient: client,
						users: [{
							id: user.id,
							username: user.username
						}]
					})
				})
			$(".chat-user-list").append(user)
		})
	})
}) 
