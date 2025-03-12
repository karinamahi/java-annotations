package com.khirata.validation.parameters.named;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class App {

    public static void main(String[] args) {
        var myClass = new UserService();

        // null example, custom param name
        try {
            new Validator().validate(myClass, "createUser", null, "hello@email.com");
        } catch (Exception e) {
            System.out.println("Validation failed: " + e.getMessage());
        }

        // empty example, default param name
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

    String paramName() default "";
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
                String paramName = parameter.getDeclaredAnnotation(Required.class).paramName();
                throw new IllegalArgumentException("Parameter \"" + ( paramName.isBlank() ? parameter.getName() : paramName ) + "\" cannot be null.");
            }

            if (parameter.isAnnotationPresent(Required.class) && value instanceof String str && str.isBlank()) {
                String paramName = parameter.getDeclaredAnnotation(Required.class).paramName();
                throw new IllegalArgumentException("Parameter \"" + ( paramName.isBlank() ? parameter.getName() : paramName ) + "\" cannot be empty.");
            }
        }

        method.invoke(object, args);
    }
}

class UserService {

    public void createUser(@Required(paramName = "username") String name, @Required String email) {
        System.out.println("Executing create user logic..");
    }
}