package com.github.irinabotea.webui.web.form;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, Object> {

  private String field = "newPassword";
  private String confirmField = "confirmPassword";

  @Override
  public void initialize(PasswordsMatch ann) {
    this.field = ann.field();
    this.confirmField = ann.confirmField();
  }

  @Override
  public boolean isValid(@Nullable Object target, ConstraintValidatorContext ctx) {
    if (target == null) {
      return true;
    }
    BeanWrapperImpl wrapper = new BeanWrapperImpl(target);
    Object a = wrapper.getPropertyValue(field);
    Object b = wrapper.getPropertyValue(confirmField);
    boolean match = a == null ? b == null : a.equals(b);
    if (!match) {
      ctx.disableDefaultConstraintViolation();
      ctx
        .buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
        .addPropertyNode(confirmField)
        .addConstraintViolation();
    }
    return match;
  }
}
