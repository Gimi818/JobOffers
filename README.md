# JobOffers 
## Web application to help find first job
-A  application that gives the opportunity to search offert jobs for junior developers.
The main function of the application is to retrieve data from a remote server and save to a database with a job search function.
This project uses modular monolithic application architecture with elements of hexagonal.
I used a nosql database because mongodb works best for fast writing and reading of simple data.
The user first needs to register and will receive an authorisation token. The Jwt token will enable the user to use the functionalities of the application.
The user can get all job offers, search for offers by id and add a new job offer to the database.
Endpoint GET is cacheabled using Redis to improve the speed of requests.
The application is connected to a remote server from which it getting job offers a request to the remote server 
is made every 30 minutes using a spring scheduler to always have new and up-to-date job offers.

For a quick demonstration, the application has been deployed on an AWS server and is available at the link : 

## Application is developed using following technologies:
 Core:
<p align="left"><a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://ultimateqa.com/wp-content/uploads/2020/12/Java-logo-icon-1.png" alt="java" width="80" height="50"/>  </a> <a href="https://spring.io/" target="_blank" rel="noreferrer"> <img src="https://e4developer.com/wp-content/uploads/2018/01/spring-boot.png" alt="spring" width="90" height="50"/> <a href="https://www.mongodb.com/" target="_blank" rel="noreferrer"> <a href="https://www.docker.com/" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/docker/docker-original-wordmark.svg" alt="docker" width="50" height="50"/> </a> </a> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/mongodb/mongodb-original-wordmark.svg" alt="mongodb" width="50" height="50"/> </a> </a> <a href="https://redis.io" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/redis/redis-original-wordmark.svg" alt="redis" width="50" height="50""/><a href="https://git-scm.com/" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/git-scm/git-scm-icon.svg" alt="git" width="50" height="50"/> <a href="https://www.docker.com/" target="_blank" rel="noreferrer"> <img src="https://mapstruct.org/images/mapstruct.png" alt="docker" width="80" height="50"/></a> </p>


 Testing:
<p align="left"> <a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://junit.org/junit4/images/junit5-banner.png" alt="java" width="80" height="40"/>
<a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://d33wubrfki0l68.cloudfront.net/6b06015a22f71ab9571943df763c6e827ae18f89/a3195/logo.png" alt="java" width="80" height="40"/>
<a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://javadoc.io/static/org.mockito/mockito-core/1.9.5/org/mockito/logo.jpg" alt="java" width="80" height="40"/></a> </p>
 Deployed on:
<p align="left"> <a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://i.pcmag.com/imagery/reviews/0179dSC1AqaTy8DbcTJMDYE-7.fit_scale.size_760x427.v1569472044.jpg" alt="java" width="80" height="40"/></a> </p>


## Rest-API Endpoints
Application provides five endpoints:

|     ENDPOINT   | METHOD |         REQUEST          | RESPONSE |             FUNCTION             |
|:--------------:|:------:|:------------------------:|:--------:|:--------------------------------:|
|/register       |  POST  |   JSON BODY              |   JSON   | User registration                |
|/token          |  POST  |   JSON BODY              |   JSON   | Get a token                      |
|/offers         |  GET   |                          |   JSON   | Get all offers                   |
|/offers         |  POST  |   JSON BODY              |   JSON   | Create a new offer               |
|/offers/id      |  GET   |                          |   JSON   | Get offer by id                  |

