# Java Annotation

This is a study project focused on Java annotations.

## Basic Implementation

Basic annotation without any complexity.

## Annotation for Parameters

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