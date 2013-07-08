package com.bklimt.candelabra.util;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleItemQueue {
  private HashMap<String, QueueEntry> enqueued = new HashMap<String, QueueEntry>();
  private ArrayDeque<QueueEntry> queue = new ArrayDeque<QueueEntry>();
  private Logger log = Logger.getLogger(getClass().getName());
  private Object lock = new Object();
  
  private static class QueueEntry {
    public String key;
    public Runnable runnable;
  }
  
  public SingleItemQueue(final String name) {
    new Thread(new Runnable() {
      public void run() {
        while (true) {
          QueueEntry entry = null;
          synchronized (lock) {
            if (queue.isEmpty()) {
              try {
                lock.wait();
              } catch (InterruptedException e) {
                log.severe("Thread for SingleItemQueue " + name + " was interrupted.");
                return;
              }
            }
            entry = queue.pop();
            if (entry.key != null) {
              enqueued.remove(entry.key);
            }
          }
          try {
            entry.runnable.run();
          } catch (Exception e) {
            log.log(Level.SEVERE, "SingleItemQueue " + name + " exception.", e);
          }
        }
      }
    }, name).start();
  }
  
  public void enqueue(String key, Runnable runnable) {
    synchronized (lock) {
      QueueEntry existing = null;
      if (key != null) {
        existing = enqueued.get(key);
        if (existing != null) {
          enqueued.remove(key);
          queue.remove(existing);
        }
      }

      QueueEntry newEntry = (existing != null ? existing : new QueueEntry());
      newEntry.key = key;
      newEntry.runnable = runnable;
      if (key != null) {
        enqueued.put(key, newEntry);
      }
      queue.addLast(newEntry);
      lock.notify();
    }
  }
}
