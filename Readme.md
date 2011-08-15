# Netty Chat

Lets chat about netty!  Actually, this is very experimental group based chat server implementation written using the
popular non blocking java server [netty](http://www.jboss.org/netty).  

It is written so that it can be embedded in any web application.  Simply add the libraries in src/main/webapp/lib to your 
application and then add this snippet to create a chat bar:

<pre>
var chatBar = new ChatBar({
  url:"http://localhost:8081", // point to location of chat server
  id:1, // arbitrary number used to determine distinct users
  groupId:1, // arbitrary number to determine distinct rooms
  username:"username" // displayed in chat
});
</pre>

Also see examples in src/main/webapp/chat-bar-[1234].html.  Open them in different windows to simulate chatting back and forth.

Groups can be thought of as "rooms".  Users will be restricted to seeing a list of users in their group.

Uses apache buildr.  To install, first make sure you have [ruby gems](http://rubygems.org/) installed.
Then run:

    sudo gem install buildr

Finally, from the root of the project run:

    buildr run

To start the server.

More work to be done.  Right now it uses a probably very naive hand rolled jsonp polling implementation but
may be switched to use something based on socket.io, for example [socketio-java](http://code.google.com/p/socketio-java/)
or [socketet.io-netty](https://github.com/ibdknox/socket.io-netty) 
