(function(){
	window.ChatClient = function(config){
		var self = this;
		self.config = config;
		self.connected=false;
		self.listeners = [];
		if(config.onMessage){
			self.listeners.push({
				type: "message",
				callback: config.onMessage
			})
		}

		this.listeners.push({
			type: "session-established",
			callback: function(data) {
				self.sessionId = data.sessionId
			}
		})
	}
	ChatClient.prototype = {
		connect: function(callback){
			var self = this;
			request.call(self, "/connect", {}, function(messages){
				messages.forEach(function(message){
					self.listeners.forEach(function(listener){
						if(listener.type===message.type){
							listener.callback(message.data)
						}
					})
				});
				self.connect();
				(callback && callback())
			})
		},
		sendMessage: function(messageDetails){
			request.call(this, "/message", $.extend({messageType: "message"}, messageDetails), function(){})
		},
		getUsers: function(callback){
			var self = this;
			request.call(self, "/message", {messageType: "list-users"}, function(users){})
			var listener = {
				type: "user-list",
				callback: function(response){
					var users = response.users;
					console.log(users);
					self.listeners.splice(self.listeners.indexOf(listener), 1);
					callback(users)
				}
			}

			self.listeners.push(listener)
		},
		pushListener: function(listener){
			this.listeners.push(listener)
		}
	}

	var request = function(path, data, success){
		var self = this;
		$.jsonp({
			url: self.sessionId ? (self.config.url + path + "/" + self.sessionId) : (self.config.url + path),
			data: $.extend({}, data, {
				groupId: self.config.groupId,
				id: self.config.user.id,
				username: self.config.user.username
			}),
			callbackParameter: "callback",
			success: success
		})
	};

}())
