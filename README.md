# Internet Connection
An Android library That **Simplify** Connecting to the Internet and **Download** files.

# features
* send **POST** request
* sent **FILE** with request
* get result as a string
* downloader with perfect dialog
* sequence downloader

# Usage 
In the first stage, you need to include these permissions in your `AndroidManifest.xml` file

``` xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
After that, import **com.zhillamo.internet** package in your packages folder. So now everything is ready to start.

# Samples
**post request sample:**
``` java
HttpRequest request = new HttpRequest();
request.url("http://www.zhillamo.com/");
request.cacheExpireTime(120000);
request.cacheDir(DIR_APP);
request.enableCache(true);
request.addPostField("key1", "test value");
request.addPostField("key2", intValue);
request.addFileField("key3", new File(DIR_APP, "file_name.ex"));
request.listener(new HttpConnector.webListener() {
                    @Override
                    public void onSuccess(String result) {
                        Log.i(result);
                    }
        
                    @Override
                    public void onError(String errorMessage) {
                        Log.e(errorMessage);
                    }
                });
request.build();

HttpConnector connector = new HttpConnector(request);
connector.connect();
```

**downloader sample:**
``` java 
    //if yours files more than one so create list of FileStruct
    List<FileStruct> soundLinks = new ArrayList<>();
    
    Downloader downloader = new Downloader.Builder(activityContext)
        .title("DIALOG TITLE")
        .font(TYPEFACE)
        .showSize(true) //show file size during download ex: 1.23MB of 5.2MB
        .showDialog(true) //show progress dilaog of download
        .addUrls(soundLinks) //list of files
        .addUrl("FILE_URL", "FILE_PATH") //single file
        .listener(new Downloader.Listener() {
           @Override
           public void onPreDownload() {
              //do somethings before start download
           }

           @Override
           public void onProgressUpdate(long totalFiles, long fileIndex, long percent) {
              //progress update
           }

           @Override
           public void onComplete() {
              //complete download
           }

           @Override
           public void onError(int totalFilesCount, List<FileStruct> errorFiles) {
              //do something when have error
           }

           @Override
           public void onCancel() {
                //do somethings when user cancelled download.
           }
        })
        .build();
    downloader.start();
```