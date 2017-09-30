package com.zhillamo.internet;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Common {

   public static boolean isOnline(Context context) {
      ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
   }

   public static boolean isLollipop() {
      return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
   }

   /* Checks if external storage is available for read and write */
   public static boolean isExternalStorageWritable() {
      String state = Environment.getExternalStorageState();
      return Environment.MEDIA_MOUNTED.equals(state);
   }

   /* Checks if external storage is available to at least read */
   public static boolean isExternalStorageReadable() {
      String state = Environment.getExternalStorageState();
      return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
   }

   public static boolean isDigit(String phoneNumber) {
      for (int i = 0; i < phoneNumber.length(); i++) {
         if (!Character.isDigit(phoneNumber.charAt(i)))
            return false;
      }
      return true;
   }

   //get file name without extension.
   public static String getBaseName(File file) {
      String name = file.getName();
      int pos = name.lastIndexOf(".");
      if (pos > 0) {
         name = name.substring(0, pos);
      }
      return name;
   }

   private final static char[] hexArray = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

   private static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];
      for (int j = 0; j < bytes.length; j++) {
         int v = bytes[j] & 0xFF;
         hexChars[j * 2] = hexArray[v >>> 4];
         hexChars[j * 2 + 1] = hexArray[v & 0x0F];
      }
      return new String(hexChars);
   }


   static String sha1(String toHash) {
      String hash = null;
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-1");
         byte[] bytes = toHash.getBytes("UTF-8");
         digest.update(bytes, 0, bytes.length);
         bytes = digest.digest();

         hash = bytesToHex(bytes);
      } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
         Logger.e(e.getMessage());
      }

      return hash;
   }

   public static String encryptPassword(String password) {
      try {
         Security security = new Security();
         return Security.bytesToHex(security.encrypt(password));
      } catch (Exception e) {
         Logger.e(e.getMessage());
         return null;
      }
   }

   public static String decryptPassword(String hashPassword) {
      try {
         Security security = new Security();
         return new String(security.decrypt(hashPassword));
      } catch (Exception e) {
         Logger.e(e.getMessage());
         return null;
      }
   }
}
