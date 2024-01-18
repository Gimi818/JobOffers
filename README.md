# JobOffers 
## Web application for easier browsing of job offers for programmers.
The application that gives the opportunity to search offers jobs for junior developers.
The main function of the application is to retrieve data from a remote server and save it with a function to search and add jobs.

This project uses modular monolithic application architecture with elements of hexagonal.
I used a NoSQL database because MongoDB works best for fast writing and reading of simple data.

The user first needs to register and will receive an authorization token. The Jwt token will enable the user to use the functionalities of the application.
The registered user can browse job offers with information about the company name, job position, salary and a link to the offer. 
In addition, users can create new personalized job offers, which will be stored in the database. 

Endpoint GET is cacheable using Redis to improve the speed of requests.
The application is connected to a remote server from which it gets job offers a request to the remote server 
is made every hour using a spring scheduler to always have new and current job offers.

The application is tested using unit tests.


## Application is developed using the following technologies:
 Core:
<p align="left"><a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://ultimateqa.com/wp-content/uploads/2020/12/Java-logo-icon-1.png" alt="java" width="80" height="50"/>  </a> <a href="https://spring.io/" target="_blank" rel="noreferrer"> <img src="https://e4developer.com/wp-content/uploads/2018/01/spring-boot.png" alt="spring" width="90" height="50"/> <a href="https://www.mongodb.com/" target="_blank" rel="noreferrer"> <a href="https://www.docker.com/" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/docker/docker-original-wordmark.svg" alt="docker" width="50" height="50"/> </a> </a> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/mongodb/mongodb-original-wordmark.svg" alt="mongodb" width="50" height="50"/> </a> </a> <a href="https://redis.io" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/redis/redis-original-wordmark.svg" alt="redis" width="50" height="50""/><a href="https://git-scm.com/" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/git-scm/git-scm-icon.svg" alt="git" width="50" height="50"/> <a href="https://www.docker.com/" target="_blank" rel="noreferrer"> <img src="https://mapstruct.org/images/mapstruct.png" alt="docker" width="80" height="50"/></a> </p>


 Testing:
<p align="left"> <a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://junit.org/junit4/images/junit5-banner.png" alt="java" width="80" height="40"/>
<a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://d33wubrfki0l68.cloudfront.net/6b06015a22f71ab9571943df763c6e827ae18f89/a3195/logo.png" alt="java" width="80" height="40"/>
<a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://javadoc.io/static/org.mockito/mockito-core/1.9.5/org/mockito/logo.jpg" alt="java" width="80" height="40"/></a> </p>
 
 
 
 ## To run the application, follow these steps:
- Install Mongodb and docker on your computer. 
- Clone the repository in IntelliJ IDEA using the link https://github.com/Gimi818/JobOffers.git
- Enter "docker-compose up" in thermal.
- Run the applications in IntelliJ IDEA.
- Check the available endpoints at the link localhost:8080/swagger-ui/index.html#/
- Try the applications in Postaman, the steps on how to do it are below.

 ## How to use the application in Postman:
 
    Step 1 :
    POST localhost:8080/register 
    Enter your username and password. 
  
  <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/1.PNG" width="500" heigt="700"/>
 
    Step 2 :
    POST localhost:8080/token
    Enter your username and password to get the token.
    
  <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/2.PNG" width="500" heigt="700"/>
 
    Step 3.1 : 
    Select the authorization field and select the Bearer token option 
    and paste the token you received into the all next steps.
    
  <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/3.PNG" width="500" heigt="700"/>
 
    Step 3.2 : 
    GET localhost:8080/offers
    Get all the latest job offers. 
    
    
  <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/4.PNG" width="500" heigt="700"/>
 
    Step 4 : 
    GET localhost:8080/offers
    Add a new job offer.
    
    
  <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/5.PNG" width="500" heigt="700"/>
 
    Step 5: 
    GET localhost:8080/offers/{id}
    Find a job offer by id.
    
    
  <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/6.PNG" width="500" heigt="700"/>
 
 ## Tests:
 <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/tests1.1.PNG" width="500" heigt="700"/>
 <img src="https://github.com/Gimi818/JobOffers/blob/master/steps/tests2.PNG" width="500" heigt="700"/>
 
 
