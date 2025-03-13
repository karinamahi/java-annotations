# Java Annotation

This is a study project focused on Java annotations.

## Basic Implementation

Basic annotation without any frameworks. Instead, we will use reflections.
In this example, I implemented a Java annotation that prints a message before executing the annotated method.

First, I defined `METHOD` as the target:
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyAnnotation {
}
```

And, I created the processor, which identifies the annotated method and prints the message before executing it.
```java
class Processor {
    public void process(Object object) throws InvocationTargetException, IllegalAccessException {
        var methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(MyAnnotation.class)) {
                System.out.println("Executing MyAnnotation before executing the method " + method.getName()); //it will be executed before the annotated method
            }
            method.invoke(object);
        }
    }
}
```
Here, I'm using the annotation I created on the first method:
```java
class MyClass {
    @MyAnnotation
    public void annotatedMethod() {
        System.out.println("Executing annotatedMethod..");
    }

    public void nonAnnotatedMethod() {
        System.out.println("Executing nonAnnotatedMethod..");
    }
}
```
Then, when I execute the code:
```java
public static void main( String[] args ) throws InvocationTargetException, IllegalAccessException {
    var object = new MyClass();
    new Processor().process(object);
}
```
The output is:
```text
Executing MyAnnotation before executing the method annotatedMethod
Executing annotatedMethod..
Executing nonAnnotatedMethod..
```

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

```text
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

### Using custom parameter name

Depending on the situation, we may not want to compile with the `-parameters` option, so let's try another strategy.

First, remove the compile configuration `-parameters` from pom.xml and run the following command:
```shell
mvn clean compile
```

If we run the code, as expected, it will not print the parameter names as it did before:
```text
Validation failed: Parameter "arg0" cannot be null.
Validation failed: Parameter "arg1" cannot be empty.
```

Then, I added the `paramName` which will allow us to specify a custom parameter name if needed.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface Required {

    String paramName() default "";
}
```

Now we need to handle the custom or default parameter name:
```java
for (int i = 0; i < parameters.length; i++) {

    Object value = args[i];
    Parameter parameter = parameters[i];
    
    if (parameter.isAnnotationPresent(Required.class) && value == null) {
    String paramName = parameter.getDeclaredAnnotation(Required.class).paramName();
    throw new IllegalArgumentException("Parameter \"" + ( paramName.isBlank() ? parameter.getName() : paramName ) + "\" cannot be null.");
    }
    
    if (parameter.isAnnotationPresent(Required.class) && value instanceof String str && str.isBlank()) {
    String paramName = parameter.getDeclaredAnnotation(Required.class).paramName();
    throw new IllegalArgumentException("Parameter \"" + ( paramName.isBlank() ? parameter.getName() : paramName ) + "\" cannot be empty.");
    }
}
```

Usage:
```java
public void createUser(@Required(paramName = "username") String name, @Required String email) {
    System.out.println("Executing create user logic..");
}
```

Output:
```text
Validation failed: Parameter "username" cannot be null.  # custom
Validation failed: Parameter "arg1" cannot be empty.     # default
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

        if (value instanceof String str && str.isBlank()) {
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

```shell
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

## Common Use Cases for Annotations

- ✅ Validation (@NotBlank, @Required) → Validate input before processing.
- ✅ Logging & Performance (@LogExecutionTime) → Measure execution time.
- ✅ Security (@RoleRequired) → Restrict access to admin-only actions.
- ✅ Auditing (@AuditAction) → Log important user actions.
- ✅ Feature Flags (@FeatureToggle) → Enable/disable features dynamically.