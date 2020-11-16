package com.jayway.spring.asynchronous.demo.service;

import com.jayway.spring.asynchronous.demo.entity.User;
import com.jayway.spring.asynchronous.demo.repository.UserRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import javax.persistence.EntityNotFoundException;
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
  public Future<String> ping() {
    System.out.println("Execute method asynchronously - " + Thread.currentThread().getName());
    try {
      Thread.sleep(5000);
      return new AsyncResult<String>("" + "pong !!!!");
    } catch (InterruptedException e) {

    }
    return null;
  }

  private List<User> parseCSVFile(final MultipartFile file) throws Exception {
    final List<User> users = new ArrayList<>();
    try {
      try (final BufferedReader br = new BufferedReader(
          new InputStreamReader(file.getInputStream()))) {
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

  @Async
  public CompletableFuture<List<User>> saveUsers(MultipartFile file) throws Exception {
    long start = System.currentTimeMillis();
    List<User> users = parseCSVFile(file);
    logger.info("saving list of users of size {} ",
        users.size() + "" + Thread.currentThread().getName());
    users = repository.saveAll(users);
    long end = System.currentTimeMillis();
    logger.info("Total time {}", (end - start));
    return CompletableFuture.completedFuture(users);
  }


  @Async
  public CompletableFuture<List<User>> findAllUsers()
      throws InterruptedException, ExecutionException {

    CompletableFuture<List<User>> u = new CompletableFuture<>();
//    CompletableFuture<List<User>> u = CompletableFuture.supplyAsync(() -> {
//      try {
//        Thread.sleep(5000);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//      logger.info("about to find all the users {} ", Thread.currentThread().getName());
//      return repository.findAll();
//    }).exceptionally(exception -> {
//      System.err.println("exception: " + exception);
//      return null;
//    });

    executor.execute(() -> {
      try {
        getUsers(u);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    logger.info("Chilling while another guy does all the work // {} ", "" + Thread.currentThread().getName());
    return u;
  }

  private void getUsers(CompletableFuture<List<User>> u) throws InterruptedException {

    Thread.sleep(5000);
    List<User> a = repository.findAll();
    u.complete(a);
  }

  @Async
  public CompletableFuture<User> getUserdById(int id) throws InterruptedException {

    CompletableFuture<User> futureUser = CompletableFuture.supplyAsync(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Kollar först här ");
      return repository.findById(id).orElseThrow();
    })
        .exceptionally(exception -> {
          System.out.println("IDt finns inte och därför skickar den exception");
          System.err.println("" + exception);
          return null;
        });

    logger.info("last step before returning the User " + Thread.currentThread().getName());
    return futureUser;
  }

  @Async
  public void completeUserdById(CompletableFuture<User> completableFuture, int id)
      throws InterruptedException {

    Thread.sleep(5000);
    logger.info("Trying to gather the right information {} with thread: " + Thread.currentThread()
        .getName());
    User a = repository.findById(id).orElseThrow(() -> new EntityNotFoundException());
    try {
      completableFuture.complete(a);
    } catch (Exception e) {
      logger.error("failed to complete the findById", e);
      completableFuture.completeExceptionally(e);
    }
  }


}
