package com.zhillamo.internet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

class HttpResponse implements java.io.Serializable {

   private long when;
   private String response;

   HttpResponse(long when, String response) {
      try {
         this.when = when;
         this.response = Encryption.encrypt(response);
      } catch (GeneralSecurityException | UnsupportedEncodingException e) {
         Logger.e(e.getMessage());
      }
   }

   long getWhen() {
      return this.when;
   }

   String getResponse() {
      try {
         return Encryption.decrypt(this.response);
      } catch (GeneralSecurityException | IOException e) {
         Logger.e(e.getMessage());
         return null;
      }
   }
}