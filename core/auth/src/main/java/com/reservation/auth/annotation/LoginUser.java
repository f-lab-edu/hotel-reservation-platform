package com.reservation.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Schema;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Schema(hidden = true)
public @interface LoginUser {
}
