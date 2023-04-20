# JobOffers 
## Web application to help find jobs for developers 
-A web application that gives the opportunity to search offert jobs for developers.
-The main function of the application is to download data from a remote server  and saving to mongodb database.
The user first needs to register and will receive an authorisation token. The Jwt token will enable the user to use the functionalities of the application.
The user can get all job offers, search for offers by id and add a new job offer to the database.
Endpoint GET is cacheabled using Redis to improve the speed of requests
The application is connected to a remote server from which it retrieves job offers; a request to the remote server 
is made every 30 minutes in order to always have new and actual job offers.


## Rest-API Endpoints
Application provides five endpoints:

|     ENDPOINT   | METHOD |         REQUEST          | RESPONSE |             FUNCTION             |
|:--------------:|:------:|:------------------------:|:--------:|:--------------------------------:|
|/register       |  POST  |   JSON BODY              |   JSON   | User registration                |
|/token          |  POST  |   JSON BODY              |   JSON   | Getting a token                  |
|/offers         |  GET   |                          |   JSON   | Get all offers                   |
|/offers         |  POST  |   JSON BODY              |   JSON   | Create a new offer               |
|/offers/id      |  GET   |                          |   JSON   | Get offer by id                  |
