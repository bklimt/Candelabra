package com.bklimt.candelabra;

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
}
