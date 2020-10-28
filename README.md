# solace-spring-sendAndReceive
Minimal application to reproduce a bottleneck identified with JmsTemplate.sendAndReceive() on Solace.

Configure solace credentials in application.properties as well as the number of client and listener threads. 
When starting the specified number of client threads will perform sendAndReceive calls which will be replied to by the 
listener threads. You will see that the client threads will block each other, and the throughput will not increase with
the number of threads - starting more processes will though...

You will need a machine with Java JDK 11 and maven. To compile and run the application execute,
from the project root folder execute:

> mvn spring-boot:run

