package com.example.shopmark2.user.presentation.advice;

import com.example.shopmark2.global.presentation.advice.GlobalError;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {

    private final GlobalError error;

    public UserException(UserError error) {
        super(error.getErrorMessage());
        this.error = error;
    }

}
