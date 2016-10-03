/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stackleader.kubefx.ui.auth;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dcnorris
 */
public class BasicAuthCredentialTest {

    public BasicAuthCredentialTest() {
    }

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void usernameIsNull() {
        BasicAuthCredential authCredential = new BasicAuthCredential("", null, "test", "127.0.0.1");
        Set<ConstraintViolation<BasicAuthCredential>> constraintViolations = validator.validate(authCredential);
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void passwordIsNull() {
        BasicAuthCredential authCredential = new BasicAuthCredential("", "admin", null, "127.0.0.1");
        Set<ConstraintViolation<BasicAuthCredential>> constraintViolations = validator.validate(authCredential);
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void masterUrlIsNull() {
        BasicAuthCredential authCredential = new BasicAuthCredential("", "admin", "admin", null);
        Set<ConstraintViolation<BasicAuthCredential>> constraintViolations = validator.validate(authCredential);
        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

}
