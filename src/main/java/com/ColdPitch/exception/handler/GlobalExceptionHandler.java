package com.ColdPitch.exception.handler;

import com.ColdPitch.exception.AuthNotFoundException;
import com.ColdPitch.exception.CommentException;
import com.ColdPitch.exception.CustomException;
import com.ColdPitch.exception.DislikeAlreadySelectedException;
import com.ColdPitch.exception.LikeAlreadySelectedException;
import com.ColdPitch.exception.PostNotExistsException;
import com.ColdPitch.exception.UnauthorizedAccesException;
import com.ColdPitch.exception.UserNotFoundException;
import com.ColdPitch.exception.handler.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<?> handlerCustomException(CustomException e) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("errorCode", String.valueOf(e.getErrorCode().getStatus()));
        jsonObject.addProperty("errorMessage", e.getErrorCode().getMessage());
        String body = new Gson().toJson(jsonObject);

//        log.warn(e.getErrorCode().toString());

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(body);
    }

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<?> handlerNoSuchElementException(NoSuchElementException e) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", e.getMessage());
        String body = new Gson().toJson(jsonObject);

        return ResponseEntity.status(204).body(body);
    }

    @ExceptionHandler(AuthNotFoundException.class)
    protected ResponseEntity<?> AuthNotFoundException(AuthNotFoundException e) {
        log.error("Exception: " + e.getErrorCode().getMessage());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<> (response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(UnauthorizedAccesException.class)
    protected ResponseEntity<?> UnauthorizedAccesException(UnauthorizedAccesException e) {
        log.error("Exception: " + e.getErrorCode().getMessage());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<> (response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(PostNotExistsException.class)
    protected ResponseEntity<?> PostNotExistsException(PostNotExistsException e) {
        log.error("Exception: " + e.getErrorCode().getMessage());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<> (response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(LikeAlreadySelectedException.class)
    protected ResponseEntity<?> LikeAlreadySelectedException(LikeAlreadySelectedException e) {
        log.error("Exception: " + e.getErrorCode().getMessage());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<> (response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(DislikeAlreadySelectedException.class)
    protected ResponseEntity<?> DislikeAlreadySelectedException(DislikeAlreadySelectedException e) {
        log.error("Exception: " + e.getErrorCode().getMessage());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<> (response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<?> UserNotFoundException(UserNotFoundException e) {
        log.error("Exception: " + e.getErrorCode().getMessage());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(CommentException.class)
    protected ResponseEntity<?> CommentException(UserNotFoundException e) {
        log.error("Exception: " + e.getErrorCode().getMessage());
        ErrorResponse response = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(response, e.getErrorCode().getStatus());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handlerException(Exception e) {
        log.error("Exception : " + e.getMessage());
        return ResponseEntity.status(500).body("에러코드 정의해줘...");
    }
}
