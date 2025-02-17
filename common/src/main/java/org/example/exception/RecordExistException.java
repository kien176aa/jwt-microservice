package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecordExistException extends RuntimeException {
    public RecordExistException(String message){
        super(message + " already existed!");
    }
}
