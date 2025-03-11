# Java Annotation

This is a study project focused on Java annotations.

## Basic Implementation

Basic annotation without any complexity.

## Annotation for Parameters

### Initial Implementation

In this example, I created the annotation @Required, and it is used when we want to validate the parameter.

```java
    public void createUser(@Required String name, @Required String email) {
        System.out.println("Executing create user logic..");
    }
```

Behind the scenes, I implemented a validator to check if the value is null or empty.
It prints error messages when the value is not valid:

```shell
Parameter "name" cannot be null.
Parameter "email" cannot be empty.
```
**Note**:  it is possible to retrieve the real parameter names at runtime, but it requires compiling with the -parameters flag. If the flag is missing, Java will default to synthetic names like arg0, arg1, etc.

So, I added the following in pom.xml
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.1</version>
        <configuration>
          <compilerArgs>
            <arg>-parameters</arg>
          </compilerArgs>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

### Using Spring

First, I added the Spring dependencies.
```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <version>3.4.3</version>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
      <version>3.4.3</version>
    </dependency>
```
Spring uses Bean Validation (Jakarta Validation), which allows us to create custom validation annotations.

Now, my validation implements ConstraintValidator from Jakarta Validation.
```java
class Validator implements ConstraintValidator<Required, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        System.out.println("Executing custom validation...");
        if (value == null) {
            return false;
        }

        if (value != null && value instanceof String && ((String) value).isBlank()) {
            return false;
        }
        return true;
    }
}
```
The `@Constraint` annotation links to our custom validator. Also, our annotation must follow a default structure.

```java
@Documented
@Constraint(validatedBy = Validator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface Required {
    String message() default "Field cannot be null or empty.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```
The `@Validated` activates the validation.

```java
@Service
@Validated
class UserService {

    public void createUser(@Required String name, @Required String email) {
        System.out.println("Executing create user logic..");
    }
}
```
After that, I implemented the controller, which calls the service. Then, I tested the implementation using HTTPie.

```bash
# success
$ http POST ':8080/users?name=Marta&email=marta@email.com'
HTTP/1.1 200
Connection: keep-alive
Content-Length: 23
Content-Type: text/plain;charset=UTF-8
Date: Tue, 11 Mar 2025 16:57:20 GMT
Keep-Alive: timeout=60

Processed successfully.

# failure
$ http POST ':8080/users?name=Marta&email='
HTTP/1.1 500
Connection: close
Content-Length: 48
Content-Type: text/plain;charset=UTF-8
Date: Tue, 11 Mar 2025 16:57:23 GMT

createUser.email: Field cannot be null or empty.

```