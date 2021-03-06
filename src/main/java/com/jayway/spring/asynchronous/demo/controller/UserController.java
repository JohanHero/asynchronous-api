package com.jayway.spring.asynchronous.demo.controller;

import com.jayway.spring.asynchronous.demo.entity.User;
import com.jayway.spring.asynchronous.demo.repository.UserRepository;
import com.jayway.spring.asynchronous.demo.service.UserService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.hibernate.annotations.NotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class UserController {

  @Autowired
  private UserService service;

  @Autowired
  private UserRepository repository;

  Logger logger = LoggerFactory.getLogger(UserController.class);

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity handleException(NoSuchElementException exception) {

    //exception.printStackTrace();
    return new ResponseEntity<>("Id not found, exception: " + exception, HttpStatus.NOT_FOUND);
  }

  @PostMapping(value = "/users", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
  public ResponseEntity SaveUsers(@RequestParam(value = "files") MultipartFile[] files) throws Exception {
    for (MultipartFile file : files) {
      service.saveUsers(file);
    }

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping(value = "/users", produces = "application/json")
  public CompletableFuture<ResponseEntity> findAllUsers() throws InterruptedException, ExecutionException {

    logger.info("Get list of users {}" + Thread.currentThread().getName());
    CompletableFuture<List<User>> u = service.findAllUsers();
    logger.info("the step right before we return the value... {}" + Thread.currentThread().getName());

    return u.thenApply(ResponseEntity::ok);
  }

  @GetMapping(value = "/users/{id}", produces = "application/json")
  public CompletableFuture<ResponseEntity<User>> findUserById(@PathVariable("id") int id) throws InterruptedException {

    logger.info("Get single user by id {} {}" + Thread.currentThread().getName(), id);

    CompletableFuture<User> u = service.getUserdById(id);

    logger.info("step right before returning the value — if there is a value {}" + Thread.currentThread()
        .getName());

    return u.thenApply(ResponseEntity::ok);
  }


  @GetMapping(value = ("/userThread"), produces = "application/json")
  public ResponseEntity getUsers() throws InterruptedException, ExecutionException {
    CompletableFuture<List<User>> users1 = service.findAllUsers();
    logger.info("Get list of users {}" + Thread.currentThread().getName());
    CompletableFuture<List<User>> users2 = service.findAllUsers();
    CompletableFuture<List<User>> users3 = service.findAllUsers();
    CompletableFuture.allOf(users1, users2, users3).join();

    return ResponseEntity.status(HttpStatus.OK).build();
  }


}
