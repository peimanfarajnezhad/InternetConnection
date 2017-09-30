# Internet Connection
An Android library That **Simplify** Connecting to the Internet

**sample code:**
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