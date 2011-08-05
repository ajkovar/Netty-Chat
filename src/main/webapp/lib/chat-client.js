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
				self.connect()
			}
		})
	}
	ChatClient.prototype = {
		connect: function(callback){
			var self = this;
			request.call(self, "/connect", {}, function(response){
				self.connect();
				self.listeners.forEach(function(listener){
					if("chat-"+listener.type===response.type){
						listener.callback(response.data)
					}
				})
				callback()
			})
		},
		sendMessage: function(messageDetails){
			request.call(this, "/message", messageDetails, function(){})
		},
		getUsers: function(callback){
			var self = this;
			request.call(self, "/users", {messageType: "list-users"}, function(users){
				console.log(users)
				callback(users)
			})
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
				userId: self.config.user.id,
				username: self.config.user.username
			}),
			callbackParameter: "callback",
			success: success
		})
	};

}())
