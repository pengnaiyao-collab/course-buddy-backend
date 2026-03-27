<Project Structure>
- src
  - main
    - java
      - com
        - example
          - coursebuddybackend
            - CourseBuddyBackendApplication.java
            - config
              - SecurityConfig.java
              - DatabaseConfig.java
            - controller
              - CourseController.java
              - AuthController.java
              - CollaborationController.java
              - NotesController.java
              - QAController.java
            - model
              - Course.java
              - User.java
              - Note.java
              - QA.java
              - Collaboration.java
            - repository
              - CourseRepository.java
              - UserRepository.java
              - NoteRepository.java
              - QARepository.java
              - CollaborationRepository.java
            - service
              - CourseService.java
              - AuthService.java
              - CollaborationService.java
              - NotesService.java
              - QAService.java
    - resources
      - application.properties 
      - static
      - templates
- pom.xml

<!-- Maven Configuration -->
<properties>
    <java.version>11</java.version>
    <spring.version>2.5.4</spring.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
