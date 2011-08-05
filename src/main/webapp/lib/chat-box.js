(function(){
	window.ChatBox = function(config){
		var self = this;

		self.config = config;

		self.messageBox = $("<div>", {"class": "chat-messages"});
		self.inputBox = $("<input>", {"class": "chat-input"});

		config.container.append(self.messageBox).append(self.inputBox);

		self.inputBox.keypress(function(e){
			if(e.keyCode===13){
				send.call(self)
			}
		})
	};

	ChatBox.prototype = {
		addMessage: function(sender, text){
			this.messageBox.append(sender + ": " + text + "<br />");
		},
		joined: function(user) {
			this.messageBox.append(user.name + " joined the chat");
		},
		leave: function(user){
			this.messageBox.append(user.name + " left the chat");
		},
		focus: function(){
			this.inputBox.focus();
		}
	};

	var send = function(){
		var self = this;
		var message = self.inputBox.val();
		self.onSend(message);
		self.addMessage("me", message);
		self.inputBox.val("").focus();
	}
}())
