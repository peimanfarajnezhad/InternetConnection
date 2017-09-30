package com.zhillamo.internet;


import android.util.Log;

public class Logger {

   private static boolean DEBUG = BuildConfig.DEBUG;
   private static final String TAG = "zhillamo-log";

   static void i(String message) {
      if (DEBUG) {
         Log.i(TAG, buildLogMsg(message));
      }
   }

   static void e(String message) {
      if (DEBUG) {
         Log.e(TAG, buildLogMsg(message));
      }
   }

   private static String buildLogMsg(String message) {

      StackTraceElement ste = Thread.currentThread().getStackTrace()[4];

      return "[" + ste.getFileName().replace(".java", "") + "::" + ste.getMethodName() + "]" + message;

   }
}
