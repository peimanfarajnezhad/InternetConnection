package com.zhillamo.internet;


public class FileStruct {
   public String url;
   public String path;
   public String errorMessage;
   public boolean downloaded;

   public FileStruct(String url, String path) {
      this.url = url;
      this.path = path;
      downloaded = false;
   }
}
