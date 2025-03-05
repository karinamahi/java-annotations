package com.khirata.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class App 
{
    public static void main( String[] args ) throws InvocationTargetException, IllegalAccessException {
        var object = new MyClass();
        new Processor().process(object);
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MyAnnotation {
}

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
class MyClass {
    @MyAnnotation
    public void annotatedMethod() {
        System.out.println("Executing annotatedMethod..");
    }

    public void nonAnnotatedMethod() {
        System.out.println("Executing nonAnnotatedMethod..");
    }
}