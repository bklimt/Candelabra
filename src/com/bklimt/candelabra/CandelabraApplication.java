package com.bklimt.candelabra;

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

import com.parse.Parse;
import com.parse.ParseACL;

import android.app.Application;

public class CandelabraApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    Parse.initialize(this, "iU3LILeFjqfumdNV1JXKOhTHpD2t2tUv3W31kOgR",
        "qoBbEJ169RDL0iZUh7jcnRwA8VJpmRlYnOFIx5wE");

    // ParseUser.enableAutomaticUser();

    ParseACL defaultACL = new ParseACL();
    ParseACL.setDefaultACL(defaultACL, true);
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
  
  public static JSONObject get(URL url) throws Exception {
    HttpURLConnection connection = null;
    String json = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
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
  
  public static JSONArray put(URL url, JSONObject command) throws Exception {
    return request("PUT", url, command);
  }

  public static JSONArray post(URL url, JSONObject command) throws Exception {
    return request(null, url, command);
  }

  private static JSONArray request(String method, URL url, JSONObject command) throws Exception {
    HttpURLConnection connection = null;
    String json = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
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
}
