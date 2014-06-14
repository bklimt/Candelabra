package com.bklimt.candelabra.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;

import com.bklimt.candelabra.util.SingleItemQueue;

public class Http {
  private static Http http = new Http();

  public static Http getInstance() {
    return http;
  }

  public static String readFully(InputStream stream) throws IOException {
    InputStream in = new BufferedInputStream(stream);
    byte[] buffer = new byte[1024];
    StringBuilder builder = new StringBuilder();
    int read = -1;
    while ((read = in.read(buffer)) >= 0) {
      builder.append(new String(buffer, 0, read));
    }
    return builder.toString();
  }

  public static JSONObject getNow(URL url) throws Exception {
    HttpURLConnection connection = null;
    String json = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(1000);
      json = readFully(connection.getInputStream());

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }

    JSONObject object = null;
    try {
      object = new JSONObject(json);
      if (object.has("error")) {
        JSONObject error = object.getJSONObject("error");
        if (error.optInt("type") == 101) {
          throw new RuntimeException(
              "Please press the link button on the router and then try again.");
        }
        throw new RuntimeException("Error " + error.optString("type") + ": "
            + error.optString("description"));
      }

    } catch (JSONException je) {
      throw je;
    }

    return object;
  }

  private static JSONArray requestNow(String method, URL url, JSONObject command) throws Exception {
    HttpURLConnection connection = null;
    String json = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(1000);
      connection.setDoOutput(true);
      if (method != null) {
        connection.setRequestMethod(method);
      }

      String commandString = command.toString();
      OutputStream out = new BufferedOutputStream(connection.getOutputStream());
      out.write(commandString.getBytes());
      out.close();

      json = readFully(connection.getInputStream());

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }

    JSONArray array = null;
    try {
      array = new JSONArray(json);
      if (array.length() == 1 && array.get(0) instanceof JSONObject
          && array.getJSONObject(0).has("error")) {
        JSONObject error = array.getJSONObject(0).getJSONObject("error");
        if (error.optInt("type") == 101) {
          throw new RuntimeException(
              "Please press the link button on the router and then try again.");
        }
        throw new RuntimeException("Error " + error.optString("type") + ": "
            + error.optString("description"));
      }

    } catch (JSONException je) {
      throw je;
    }

    return array;
  }

  private SingleItemQueue queue = new SingleItemQueue("HTTP Queue");

  public Task<JSONObject> get(String key, final URL url) {
    final Task<JSONObject>.TaskCompletionSource tcs = Task.create();
    queue.enqueue(key, new Runnable() {
      @Override
      public void run() {
        try {
          tcs.setResult(getNow(url));
        } catch (Exception e) {
          tcs.setError(e);
        }
      }
    });
    return tcs.getTask();
  }

  private Task<JSONArray> request(String key, final String method, final URL url,
      final JSONObject command) {
    final Task<JSONArray>.TaskCompletionSource tcs = Task.create();
    queue.enqueue(key, new Runnable() {
      @Override
      public void run() {
        try {
          tcs.setResult(requestNow(method, url, command));
        } catch (Exception e) {
          tcs.setError(e);
        }
      }
    });
    return tcs.getTask();
  }

  public Task<JSONArray> put(String key, URL url, JSONObject command) {
    return request(key, "PUT", url, command);
  }

  public Task<JSONArray> post(String key, URL url, JSONObject command) {
    return request(key, null, url, command);
  }
}
