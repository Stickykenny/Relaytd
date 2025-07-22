<div align="center">

# Relaytd

(Back-end)  
<> See Front-End ~~and usage~~ at [Overlaytd](https://github.com/Stickykenny/Overlaytd) <>

On the theme of astral . . .  
Web-application to centralize my own interests

### Main Technical Stack

**Front-end** | **Back-end**    
Angular 19 | Java 21 -  
------- Typescript 5 | Spring Boot 3.4   
Bootstrap | Maven -  
. Ag Grid 32 | PostgreSQL




<details>
<summary><b>Others : </b></summary>

Spring Security  
Maven  
JPA : Hibernate ORM implementation

</details>
</div>

### Implementation feature

<div align="left">

#### Main

Back-end
> - CRUD Operation with PostgreSQL database
> - DTO (Data Transfer Object) Usage
> - Swagger integrated
> - Logging with Logback
> - Unit Test with Junit 5 and Mockito

Front-end

> - API call to Back-end
> - HttpInterceptorFn Used for JWT token
> - Add/Send new entries
> - Toast Notification with ngx-toastr

#### Security

- Spring Security Filter Chain : CORS and role-based access
- User Authentification with Database and In-Memory
- OAuth 2.0 with Github or Google
- JWT token generation and requirement
- CSRF is disabled and not needed but implementation is commented and still available to see

</div>

#### Deprecated

- Server UI with Thymeleaf => now moved to a proper Back-end
- CSRF interceptor => doesn't have a usage when using JWT on SPA

#### Comments

- I know JWT shouldn't be on queryParam, tried as a cookie while making formLogin work at
  the same time (the id was invalid on oauth side): current hint would be to learn more about the @Order annotation and
  make 2 WebSecurityConfigurerAdapter
- Looking into ways to introduce custom animations and procedural animations with layers into the web-app
- I am leaving the comments as they are a learning points in this project
- Changed Path Variables endpoints to Endpoint using Request Body, Path Variables are too restricted due to URI (no "/",
  no spaces)

#### Plan

*I am still thinking about if I want to make public my todolist/desire to implement and learn new techs*

### Others

*I understand that some of the technologies used might be overkill for a project like this, but it's a learning and
testing project where I'm exploring how to configure and work with various tools.*  
See Linked UI : https://github.com/Stickykenny/Overlaytd  
Originated from this side-project : https://github.com/Stickykenny/Playground/tree/main/relaytd 