(function(){
	window.Chat = function(config){
		var self = this,
		client = config.chatClient;

		self.config = config;

		self.chatBox = new ChatBox(config);

		client.pushListener({
			type: "chat-message",
			callback: function(data){
				var u = $.grep(self.config.users, function(u) {
					return u.id===data.fromId
				})
				if(u.length>0){
					self.chatBox.addMessage(u[0].username, data.body)
				}
			}
		})

		self.chatBox.onSend = function(message){
			config.users.forEach(function(u){
				client.sendMessage({
					toId: u.id,
					body: message 
				});
			})
		}
	};

	Chat.prototype = {
		addMessage: function(username, message){
			this.chatBox.addMessage(username, message);
		},
		focus: function(){
			this.chatBox.focus();
		}
	}
}())
