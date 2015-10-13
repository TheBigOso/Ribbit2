package com.fartyou.thedirtyappstore.ribbit2;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

//        // Enable Local Datastore.
//        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, "VS45bzUkV7gE9aY9RMewdlRnK8ec3HCZHhPTWBfJ", "HizqyPnuFh9nCwzYv6TwJ6nMznguIXfYWmo51REU");
        //test
        //    ParseObject testObject = new ParseObject("TestObject");
        //    testObject.put("foo", "bar");
        //    testObject.saveInBackground();

//        ParseUser.enableAutomaticUser();
//        ParseACL defaultACL = new ParseACL();
//        // Optionally enable public read access.
//        // defaultACL.setPublicReadAccess(true);
//        ParseACL.setDefaultACL(defaultACL, true);
    }
}
