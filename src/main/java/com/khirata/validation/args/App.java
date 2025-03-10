package com.khirata.validation.args;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class App {

    public static void main(String[] args) {
        var myClass = new UserService();

        // null example
        try {
            new Validator().validate(myClass, "createUser", null, "hello@email.com");
        } catch (Exception e) {
            System.out.println("Validation failed: " + e.getMessage());
        }

        // empty example
        try {
            new Validator().validate(myClass, "createUser", "Jane", "");
        } catch (Exception e) {
            System.out.println("Validation failed: " + e.getMessage());
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface Required {
}

class Validator {

    public void validate(Object object, String methodName, Object... args) throws InvocationTargetException, IllegalAccessException {
        Method method = null;
        for (Method m : object.getClass().getDeclaredMethods()) {
            if(m.getName().equals(methodName)) {
                method= m;
                break;
            }
        }

        if (method == null) {
            throw new IllegalArgumentException("Method not found: " + methodName);
        }

        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {

            Object value = args[i];
            Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(Required.class) && value == null) {
                throw new IllegalArgumentException("Parameter \"" + parameter.getName() + "\" cannot be null.");
            }

            if (parameter.isAnnotationPresent(Required.class) && value != null && value instanceof String && ((String) value).isBlank()) {
                throw new IllegalArgumentException("Parameter \"" + parameter.getName() + "\" cannot be empty.");
            }
        }

        method.invoke(object, args);
    }
}

class UserService {

    public void createUser(@Required String name, @Required String email) {
        System.out.println("Executing create user logic..");
    }
}