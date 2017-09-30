package com.zhillamo.internet;


import android.os.AsyncTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

/* HttpRequest request = new HttpRequest()
                .url("http://www.zhillamo.com/android/webservice.php")
                .cacheExpireTime(120000)
                .cacheDir(DIR_APP)
                .enableCache(true)
                .addPostField("test1", "test value")
                .addPostField("test2", "test value")
                .addPostField("test3", "test value")
                .addFileField("fileKey4", new File(DIR_APP, "php-awake.png"))
                .listener(new HttpConnector.webListener() {
                    @Override
                    public void onSuccess(String result) {
                        Logger.i("result:\n" + result);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Logger.e("error:\n" + errorMessage);
                    }
                })
                .build();
        HttpConnector connector = new HttpConnector(request);
        connector.connect();*/

public class HttpConnector {

   private String boundary;
   private PrintWriter writer;
   private String cacheFileName;
   private OutputStream outputStream;
   private HttpURLConnection connection;
   private final String LINE_FEED = "\r\n";
   private String errorMessage = null;

   private String cacheDir;
   private int readTimeOut;
   private int connectTimeOut;
   private boolean enableCache;
   private webListener listener;
   private long cacheExpireTime;
   private String connectionUrl;
   private boolean forceReadFromWeb;
   private List<HttpParameterField> postFields;
   private List<HttpFileField> fileFields;
   private List<HttpHeaderField> headerFields;

   public HttpConnector(HttpRequest request) {
      this.cacheDir = request.cacheDir;
      this.connectionUrl = request.connectionUrl;
      this.readTimeOut = request.readTimeOut;
      this.connectTimeOut = request.connectTimeOut;
      this.listener = request.listener;
      this.enableCache = request.enableCache;
      this.forceReadFromWeb = request.forceReadFromWeb;
      this.cacheExpireTime = request.cacheExpireTime;
      this.postFields = request.postFields;
      this.fileFields = request.fileFields;
      this.headerFields = request.headerFields;
   }

   //create or initialize http connection.
   private void createConnection() {
      try {
         // creates a unique boundary based on time stamp
         boundary = "===" + System.currentTimeMillis() + "===";

         //create url for connect.
         URL url = new URL(connectionUrl);

         //create connection.
         connection = (HttpURLConnection) url.openConnection();
         connection.setUseCaches(false);
         connection.setDoOutput(true);
         connection.setDoInput(true);
         connection.setReadTimeout(readTimeOut);
         connection.setConnectTimeout(connectTimeOut);
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
         connection.setRequestProperty("User-Agent", "CodeJava Agent");

         outputStream = connection.getOutputStream();
         writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
      } catch (IOException e) {
         Logger.i("createConnection -> IOException: " + e.getMessage());

         errorMessage = e.getMessage();
      }
   }

   //connect to web server and read string response.
   private String readFromWeb() {
      Logger.i("readFromWeb -> url: " + connectionUrl);

      createConnection();

      if (connection == null || writer == null) {
         return null;
      }

      StringBuilder response = new StringBuilder();
      try {
         if (postFields.size() > 0) {
            addPostParamsToHeader();
         }
         if (fileFields.size() > 0) {
            addFileParamsToHeader();
         }
         if (headerFields.size() > 0) {
            addHeaderFieldsToHeader();
         }

         writer.append(LINE_FEED).flush();
         writer.append("--").append(boundary).append("--").append(LINE_FEED);

         writer.close();

         // checks server's status code first
         int status = connection.getResponseCode();

         if (status == HttpURLConnection.HTTP_OK) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
               response.append(line);
            }
            reader.close();
            connection.disconnect();
         } else {
            throw new IOException("Server returned non-OK status: " + status);
         }
      } catch (IOException e) {
         Logger.e("readFromWeb -> IOException: " + e.getMessage());

         connection.disconnect();
         errorMessage = e.getMessage();

         return null;
      }

      //if cache enabled so save to cache.
      if (enableCache) {
         saveToCache(response.toString());
      }

      Logger.i("reading from web result: " + response.toString());

      return response.toString();
   }

   //if request not expired read the last result from file.
   private String readFromCache() {
      Logger.i("readFromCache -> url: " + connectionUrl + "\ncache file name: " + cacheFileName);

      try {
         ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(cacheDir + "/" + cacheFileName));
         HttpResponse responseObject = (HttpResponse) inputStream.readObject();

         long now = System.currentTimeMillis();
         long when = responseObject.getWhen();

         if (now - when > cacheExpireTime) {
            if (new File(cacheDir + "/" + cacheFileName).delete()) {
               Logger.i("cache deleted: " + cacheDir + "/" + cacheFileName);
            }
            return null;
         }

         String output = responseObject.getResponse();
         inputStream.close();

         Logger.i("reading from cache result: " + output);

         return output;
      } catch (IOException e) {
         Logger.e("readFromCache -> IOException: " + e.getMessage());

         errorMessage = e.getMessage();

         return null;
      } catch (ClassNotFoundException e) {
         Logger.e("readFromCache -> ClassNotFoundException: " + e.getMessage());

         errorMessage = e.getMessage();

         return null;
      }
   }

   //to fast access the result save result if cache enabled.
   private void saveToCache(String response) {
      Logger.i("saveToCache -> url: " + connectionUrl + "\ncache file name: " + cacheFileName);

      try {
         ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(cacheDir + "/" + cacheFileName));
         HttpResponse responseObject = new HttpResponse(System.currentTimeMillis(), response);
         outputStream.writeObject(responseObject);

         //close output stream.
         outputStream.flush();
         outputStream.close();
      } catch (IOException e) {
         Logger.e("saveToCache -> IOException: " + e.getMessage());
      }
   }

   private void addPostParamsToHeader() {
      for (HttpParameterField param : postFields) {
         writer.append("--").append(boundary).append(LINE_FEED);
         writer.append("Content-Disposition: form-data; name=\"").append(param.getKey()).append("\"").append(LINE_FEED);
         writer.append("Content-Type: text/plain; charset=").append("UTF-8").append(LINE_FEED);
         writer.append(LINE_FEED);
         writer.append(param.getValue()).append(LINE_FEED);
         writer.flush();
      }
   }

   private void addFileParamsToHeader() throws IOException {
      for (HttpFileField param : fileFields) {
         String fileName = param.getFile().getName();
         writer.append("--").append(boundary).append(LINE_FEED);
         writer.append("Content-Disposition: form-data; name=\"").append(param.getName()).append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
         writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
         writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
         writer.append(LINE_FEED);
         writer.flush();

         FileInputStream inputStream = new FileInputStream(param.getFile());
         byte[] buffer = new byte[4096];
         int bytesRead;
         while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
         }
         outputStream.flush();
         inputStream.close();

         writer.append(LINE_FEED);
         writer.flush();
      }
   }

   private void addHeaderFieldsToHeader() {
      for (HttpHeaderField param : headerFields) {
         writer.append(param.getKey()).append(": ").append(param.getValue()).append(LINE_FEED);
         writer.flush();
      }
   }

   private String createCacheFileName() {
      try {
         String query = getQuery();
         String result = (query.length() > 0) ? (connectionUrl + ";" + query) : connectionUrl;

         return Common.sha1(result) + ".zh";
      } catch (UnsupportedEncodingException e) {
         Logger.e("createCacheFileName -> UnsupportedEncodingException: " + e.getMessage());

         return null;
      }
   }

   //convert postFields to post string postFields for put in header request.
   private String getQuery() throws UnsupportedEncodingException {
      StringBuilder result = new StringBuilder();
      boolean first = true;

      for (HttpParameterField param : postFields) {
         if (first) {
            first = false;
         } else {
            result.append("&");
         }

         result.append(URLEncoder.encode(param.getKey(), "UTF-8"));
         result.append("=");
         result.append(URLEncoder.encode(param.getValue(), "UTF-8"));
      }

      return result.toString();
   }

   private String getResult() {
      if (enableCache) {
         cacheFileName = createCacheFileName();
      }

      if (forceReadFromWeb) {
         return readFromWeb();
      }

      String result = null;

      if (enableCache) {
         result = readFromCache();
      }

      if (result == null) {
         result = readFromWeb();
      }

      return result;
   }

   private void handleResult(final String result) {
      if (listener != null) {
         if (result != null) {
            listener.onSuccess(result);
         } else {
            listener.onError((errorMessage == null) ? "unknown error" : errorMessage);
         }
      }
   }

   public void connect() {
      new InternetAsyncTask().execute();
   }

   public interface webListener {
      void onSuccess(String result);

      void onError(String errorMessage);

      void onPreConnect();
   }

   private class InternetAsyncTask extends AsyncTask<Void, Void, String> {

      @Override
      protected void onPreExecute() {
         super.onPreExecute();

         if (listener != null) {
            listener.onPreConnect();
         }
      }

      @Override
      protected String doInBackground(Void... voids) {
         return getResult();
      }

      @Override
      protected void onPostExecute(String s) {
         super.onPostExecute(s);

         handleResult(s);
      }
   }
}