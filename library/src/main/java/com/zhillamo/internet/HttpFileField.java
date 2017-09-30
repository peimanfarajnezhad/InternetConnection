package com.zhillamo.internet;


import java.io.File;

class HttpFileField {

   private String name;
   private File file;

   HttpFileField(String name, File file) {
      this.file = file;
      this.name = name;
   }

   public String getName() {
      return name;
   }

   public File getFile() {
      return file;
   }
}