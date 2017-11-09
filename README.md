# Introduction
  
  This is a simple HTTP proxy server developed using Java SE. It is in fulfilment of homework assignment in course CSC 694 - Web Technologies.
  
  The simple HTTP Proxy Server supports caching. I implemented two cache replacement algorithms. You can also generate a simple report of the current session of the proxy server.  
  
  Simple HTTP Proxy Server Installation Instruction

  NB: Please ensure that Java (JDK) is installed on your computer.

  1. Copy the server's folder to your desired location on your computer.

  2. Navigate to the folder path_where_you_copied_it/ProxyCache/src

  3. Run the java compiler command from the command prompt or terminal
	$ javac *.java

  4. Finally run the java command to start the server passing the arguements as shown below
	$ java ProxyCache <<Port_Number>> <<Maximum cache size>> <<FIFO or LRU>>

<<Port_Number>> = 9999
<<Maximum cache size>> = Any none negative integer value
<<FIFO or LRU>> = FIFO or LRU
    
  5. Generate a report by running the command
  $ ~ java CacheReport

Sample Run

$ java ProxyCache 9999 50 FIFO <<Enter>>

  
  Please feel free to make any contribution, I am always willing to learn.
  You can contact me on nwachukwuc1@nku.edu or cnwachukwu5@gmail.com.