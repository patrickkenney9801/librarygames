package com.nfehs.librarygames.net;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

public class ServerToken extends CallCredentials {
  private final Metadata.Key<String> USERNAME_METADATA_KEY =
      Metadata.Key.of("username", Metadata.ASCII_STRING_MARSHALLER);
  private final Metadata.Key<String> AUTHORIZATION_METADATA_KEY =
      Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

  private String username;
  private String password;
  private String token;
  private GameClient client;
  private ReentrantLock lock;

  public ServerToken(GameClient client) {
    this.lock = new ReentrantLock();
    this.client = client;
    this.username = null;
    this.password = null;
    this.token = null;
    new Thread(
            new Runnable() {
              public void run() {
                while (true) {
                  lock.lock();
                  try {
                    if (username != null) {
                      token = client.loginSync(username, password);
                    }
                  } finally {
                    lock.unlock();
                  }
                  try {
                    // sleep for 3 minutes between refreshes
                    Thread.sleep(3 * 60 * 1000);
                  } catch (InterruptedException e) {
                  }
                }
              }
            })
        .start();
  }

  public void SetToken(String username, String password, String token) {
    lock.lock();
    try {
      this.username = username;
      this.password = password;
      this.token = token;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
    executor.execute(
        () -> {
          lock.lock();
          try {
            if (username == null) {
              metadataApplier.fail(Status.UNAUTHENTICATED);
            } else {
              Metadata headers = new Metadata();
              headers.put(USERNAME_METADATA_KEY, username);
              headers.put(AUTHORIZATION_METADATA_KEY, token);
              metadataApplier.apply(headers);
            }
          } catch (Throwable e) {
            metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
          } finally {
            lock.unlock();
          }
        });
  }

  @Override
  public void thisUsesUnstableApi() {
    // noop
  }
}
