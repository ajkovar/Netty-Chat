(function(){
	window.ChatBox = function(config){
		var self = this;

		self.config = config;

		self.titleBar = $("<div>", {"class": "chat-title-bar"})
		self.messageBox = $("<div>", {"class": "chat-messages"});
		self.inputBox = $("<input>", {"class": "chat-input"});

		var titleBarText = "Chat with " + $.map(config.users, function(user) {
			return user.username
		}).join(", ");

		self.titleBar.append(titleBarText);

		self.titleBar.append($("<div>", {"class": "chat-close"}).html("X"));
		self.titleBar.append($("<div>", {"class": "chat-minimize"}).html("_"));
							  
		config.container.append(self.titleBar).append(self.messageBox).append(self.inputBox);

		self.inputBox.keypress(function(e){
			if(e.keyCode===13){
				send.call(self)
			}
		})
	};

	ChatBox.prototype = {
		addMessage: function(sender, text){
			appendLine.call(this, sender + ": " + text);
		},
		joined: function(user) {
			appendLine.call(this, user.username + " joined the chat");
		},
		leave: function(user){
			appendLine.call(this, user.username + " left the chat");
		},
		focus: function(){
			this.inputBox.focus();
		}
	};

	var appendLine = function(line) {
		this.messageBox.append("<div>" + line + "</div>");
		var boxHeight = 0;
		this.messageBox.children().each(function(){
			boxHeight+=$(this).height();
		});
		this.messageBox.scrollTop(boxHeight);
	}

	var send = function(){
		var self = this;
		var message = self.inputBox.val();
		self.onSend(message);
		self.addMessage("me", message);
		self.inputBox.val("").focus();
	}
}())
