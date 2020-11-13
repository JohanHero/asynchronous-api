package com.jayway.spring.asynchronous.demo.service;

import com.jayway.spring.asynchronous.demo.entity.User;
import com.jayway.spring.asynchronous.demo.repository.UserRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

  @Autowired
  private UserRepository repository;

  @Autowired
  private Executor executor;

  Object target;
  Logger logger = LoggerFactory.getLogger(UserService.class);

  @Async
  public CompletableFuture<List<User>> saveUsers(MultipartFile file) throws Exception {
    long start = System.currentTimeMillis();
    List<User> users = parseCSVFile(file);
    logger.info("saving list of users of size {}", users.size() + "" + Thread.currentThread().getName());
    users = repository.saveAll(users);
    long end = System.currentTimeMillis();
    logger.info("Total time {}", (end - start));
    return CompletableFuture.completedFuture(users);
  }

//  @Async
//  public CompletableFuture<List<User>> findAllUsers(){
//    logger.info("Get list of users {}" +Thread.currentThread().getName());
//
//    List<User> users = repository.findAll();
//    return CompletableFuture.completedFuture(users);
//  }

  @Async
  public Future<String> ping() {
    System.out.println("Execute method asynchronously - "  + Thread.currentThread().getName());
    try {
      Thread.sleep(5000);
      return new AsyncResult<String>("" + "pong !!!!");
    } catch (InterruptedException e) {

    }

    return null;
  }

  public CompletableFuture<List<User>> findAllUsers() throws InterruptedException {

    CompletableFuture<List<User>> u = new CompletableFuture<>();
    executor.execute( () -> {
      try {
        getUsers(u);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    logger.info("Chillin while another guy does all the work // {} ", "" +  Thread.currentThread().getName());
    return u;
  }

  @Async
  public void getUsers(CompletableFuture <List<User>> u) throws InterruptedException {

    Thread.sleep(20000);
    List<User> users = repository.findAll();
//    List<User> users1 = repository.findAll();
//    List<User> users2 = repository.findAll();

    logger.info("Finally done gathers all the data  // {}","" + Thread.currentThread().getName());


    //Kika efter complete.exception.. eller liknande
    u.complete(users);

  }

  private List<User> parseCSVFile(final MultipartFile file) throws Exception {
    final List<User> users = new ArrayList<>();
    try {
      try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
        String line;
        String headerLine = br.readLine();
        while ((line = br.readLine()) != null) {
          final String[] data = line.split(",");
          final User user = new User();
          user.setName(data[0]);
          user.setEmail(data[1]);
          user.setGender(data[2]);
          users.add(user);
        }
        return users;
      }
    } catch (final IOException e) {
      logger.error("Failed to parse CSV file {}", e);
      throw new Exception("Failed to parse CSV file {}", e);
    }
  }
}
