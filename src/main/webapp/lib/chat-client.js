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
			request.call(self, "/connect", {}, function(response){
				self.listeners.forEach(function(listener){
					if(listener.type===response.type){
						listener.callback(response.data)
					}
				})
				self.connect();
				(callback && callback())
			})
		},
		sendMessage: function(messageDetails){
			request.call(this, "/message", messageDetails, function(){})
		},
		getUsers: function(callback){
			var self = this;
			request.call(self, "/message", {messageType: "list-users"}, function(users){
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
				id: self.config.user.id,
				username: self.config.user.username
			}),
			callbackParameter: "callback",
			success: success
		})
	};

}())
