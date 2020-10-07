# solace-spring-sendAndReceive
Minimal application to reproduce a bottleneck identified with JmsTemplate.sendAndReceive().

Configure solace properties in application.properties. The number of client and listener threads can be configured here as well. 
When starting the Applciation class the client threads will perform sendAndReceive calls which will be replied to by the listener threads.
The client threads will quickly start to block each other, and the througput will not increase with more threads -  
starting more processes will though...
