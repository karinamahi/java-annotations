package com.khirata.validation.parameters.spring;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

@Documented
@Constraint(validatedBy = Validator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface Required {
    String message() default "Field cannot be null or empty.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

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

@Service
@Validated
class UserService {

    public void createUser(@Required String name, @Required String email) {
        System.out.println("Executing create user logic..");
    }
}

@RestController
@RequestMapping("/users")
class UserController {

    private UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public void createUser(@RequestParam String name, @RequestParam String email) {
        service.createUser(name, email);
    }
}