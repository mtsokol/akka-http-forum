# Akka HTTP forum

Forum template created with Akka HTTP and Slick.

See live template here:
[akka-http-forum.herokuapp.com](https://akka-http-forum.herokuapp.com/)

## Contents

Project contains REST web service for discussion forum 
(and also simple frontend created with Twirl and Bootstrap 4).

Features
* Creating new topic
* Answering for concrete topic
* Modifying and deleting answers/topics with given secret
* Simple pagination for answers and topics


## Usage

```sh
$ sbt run
```

Remember to set up your own db credentials (here with Postgres).


## References

* [github.com/ArchDev/akka-http-rest](https://github.com/ArchDev/akka-http-rest)
* [github.com/theiterators/akka-http-microservice](https://github.com/theiterators/akka-http-microservice)