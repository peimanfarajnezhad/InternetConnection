package com.zhillamo.internet;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {

   String cacheDir;
   String connectionUrl;
   int readTimeOut = 10000;
   int connectTimeOut = 10000;
   long cacheExpireTime = 20 * 60 * 1000;
   List<HttpFileField> fileFields = new ArrayList<>();
   List<HttpHeaderField> headerFields = new ArrayList<>();
   List<HttpParameterField> postFields = new ArrayList<>();

   boolean enableCache = false;
   boolean forceReadFromWeb = true;
   public HttpConnector.webListener listener = null;

   public HttpRequest addPostField(String key, String value) {
      postFields.add(new HttpParameterField(key, value));
      return this;
   }

   public HttpRequest addPostField(String key, int value) {
      postFields.add(new HttpParameterField(key, value + ""));
      return this;
   }

   public HttpRequest addFileField(String name, File file) {
      this.fileFields.add(new HttpFileField(name, file));
      return this;
   }

   public HttpRequest addHeaderField(String key, String value) {
      headerFields.add(new HttpHeaderField(key, value));
      return this;
   }

   public HttpRequest url(String url) {
      if (url.length() > 0) {
         this.connectionUrl = url;
         return this;
      } else {
         Logger.e("url cannot be null.");
         return null;
      }
   }

   public HttpRequest connectTimeOut(int connectTimeOut) {
      this.connectTimeOut = connectTimeOut;
      return this;
   }

   public HttpRequest readTimeOut(int readTimeOut) {
      this.readTimeOut = readTimeOut;
      return this;
   }

   public HttpRequest cacheDir(String cacheDir) {
      this.cacheDir = cacheDir;
      return this;
   }

   public HttpRequest cacheExpireTime(long cacheExpireTime) {
      this.cacheExpireTime = cacheExpireTime;
      return this;
   }

   public HttpRequest enableCache(boolean cache) {
      //this.enableCache = cache;
      return this;
   }

   public HttpRequest forceReadFromWeb(boolean forceReadFromWeb) {
      //this.forceReadFromWeb = forceReadFromWeb;
      return this;
   }

   public HttpRequest listener(HttpConnector.webListener listener) {
      this.listener = listener;
      return this;
   }

   public HttpRequest build() {
      return this;
   }
}