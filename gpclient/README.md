# gpClient

The generic purpose client is meant to be used to build applications that 
are not specific to a particular deployment environment or to a particular use
case. It provides a system of queuing and caching that is appropriate
in most instances of multi-threaded applications. It isolates each reader
and writer and therefore protects different parts of the applications.
As such, it is not suitable for all purposes.


You **WILL** want to use the gpclient if:
	
You want data access and do not care about protocol details  
You want something thread-safe without having to care about locks and race conditions  
You want to mix data from multiple sources (i.e. other protocols, databases, files, etc.)  
You are developing a user interface  
You are developing an extensible application where different user will
want data access  

  
You may **NOT** want to use the gpclient if:

You need low level access to the EPICS communication protocol  
You need to implement an application specific real-time engine  
You want to lock the protocol until you have processed the data  
All your reads/write are tightly coupled to each other  


The generic purpose client provides the following functionality:


* A client API that is always thread-safe  
the ability to specify on which thread or thread pool the notification
should happen  
* A pluggable way to connect to different publish/subscribe sources of
data (i.e. {@link DataSource})  
* The ability to create your own data channel implementations, even if
they do not follow the common patterns  
