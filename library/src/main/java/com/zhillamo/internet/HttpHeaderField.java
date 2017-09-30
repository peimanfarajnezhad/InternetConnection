package com.zhillamo.internet;

class HttpHeaderField {
   private String key;
   private String value;

   public HttpHeaderField(String key, String value) {
      this.key = key;
      this.value = value;
   }

   public String getKey() {
      return key;
   }

   public String getValue() {
      return value;
   }
}