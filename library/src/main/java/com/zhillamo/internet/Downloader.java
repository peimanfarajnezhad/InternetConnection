package com.zhillamo.internet;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Downloader {

   private static AppCompatDialog dialog;
   private static ProgressBar prgDownload;
   private static AppCompatTextView txtPercent;
   private static AppCompatTextView txtCounter;
   private static AppCompatTextView txtDownloadedLength;
   private static AppCompatTextView txtTotalLength;
   private Builder builder;
   private DownloadTask downloader;
   private PowerManager.WakeLock mWakeLock;
   private FileStruct currentFile;
   private boolean isCompleted = false;
   private boolean isSuccess = false;

   private Downloader(Builder builder) {
      this.builder = builder;
   }

   public void start() {
      downloader = new DownloadTask();

      if (builder.showDialog) {
         initDialog();
      } else {
         dialog = null;
      }

      downloader.execute();
   }

   private void initDialog() {
      dialog = new AppCompatDialog(builder.context);
      dialog.setContentView(R.layout.view_progress_dialog);
      dialog.setCancelable(false);
      prgDownload = (ProgressBar) dialog.findViewById(R.id.prgDownload);
      AppCompatTextView txtTitle = (AppCompatTextView) dialog.findViewById(R.id.txtTitle);
      AppCompatTextView txtDivider = (AppCompatTextView) dialog.findViewById(R.id.txtDivider);
      txtPercent = (AppCompatTextView) dialog.findViewById(R.id.txtPercent);
      txtCounter = (AppCompatTextView) dialog.findViewById(R.id.txtCounter);
      LinearLayoutCompat lytSize = (LinearLayoutCompat) dialog.findViewById(R.id.lytSize);
      txtDownloadedLength = (AppCompatTextView) dialog.findViewById(R.id.txtDownloadedLength);
      txtTotalLength = (AppCompatTextView) dialog.findViewById(R.id.txtTotalLength);
      AppCompatButton btnCancel = (AppCompatButton) dialog.findViewById(R.id.btnCancel);

      if (builder.typeface != null) {
         txtPercent.setTypeface(builder.typeface);
         txtCounter.setTypeface(builder.typeface);
         txtDownloadedLength.setTypeface(builder.typeface);
         txtTotalLength.setTypeface(builder.typeface);
         assert txtTitle != null;
         txtTitle.setTypeface(builder.typeface);
         assert txtDivider != null;
         txtDivider.setTypeface(builder.typeface);
         assert btnCancel != null;
         btnCancel.setTypeface(builder.typeface);
      }

      if (builder.title != null) {
         assert txtTitle != null;
         txtTitle.setText(builder.title);
      }

      prgDownload.setMax(100);
      prgDownload.setIndeterminate(false);

      assert btnCancel != null;
      btnCancel.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            dialog.dismiss();
         }
      });

      dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialogInterface) {
            downloader.onCancelled();
         }
      });

      dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
         @Override
         public void onCancel(DialogInterface dialogInterface) {
            downloader.onCancelled();
         }
      });

      if (!builder.showSize) {
         assert lytSize != null;
         lytSize.setVisibility(View.GONE);
      } else {
         assert lytSize != null;
         lytSize.setVisibility(View.VISIBLE);
      }
   }

   private List<FileStruct> getErrorFiles() {
      List<FileStruct> errorFiles = new ArrayList<>();
      for (int i = 0; i < builder.urls.size(); i++) {
         if (builder.urls.get(i).errorMessage != null) {
            errorFiles.add(builder.urls.get(i));
         }
      }
      return errorFiles;
   }

   private String readableFileSize(long size) {
      if (size <= 0) return "0";
      final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
      int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
      return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
   }

   public interface Listener {
      void onPreDownload();

      void onProgressUpdate(long totalFiles, long fileIndex, long percent);

      void onComplete();

      void onError(int total, List<FileStruct> errorFiles);

      void onCancel();
   }

   public static class Builder {

      protected String title;
      protected Context context;
      protected Listener listener;
      private List<FileStruct> urls = new ArrayList<>();
      private Typeface typeface;
      private boolean showDialog = false;
      private boolean showSize = false;

      public Builder(Context context) {
         this.context = context;
      }

      public Builder title(String title) {
         this.title = title;
         return this;
      }

      public Builder listener(Listener listener) {
         this.listener = listener;
         return this;
      }

      public Builder showDialog(boolean showDialog) {
         this.showDialog = showDialog;
         return this;
      }

      public Builder addUrl(String url, String path) {
         urls.add(new FileStruct(url, path));
         return this;
      }

      public Builder addUrls(List<FileStruct> u) {
         urls.addAll(u);
         return this;
      }

      public Builder showSize(boolean show) {
         showSize = show;
         return this;
      }

      public Builder font(Typeface font) {
         this.typeface = font;
         return this;
      }

      public Downloader build() {
         return new Downloader(this);
      }
   }

   private class DownloadTask extends AsyncTask<String, Long, String> {
      @Override
      protected String doInBackground(String... params) {
         BufferedInputStream in = null;
         FileOutputStream fs;
         BufferedOutputStream out = null;
         HttpURLConnection connection = null;

         for (int i = 0; i < builder.urls.size(); i++) {
            try {
               currentFile = builder.urls.get(i);

               URL url = new URL(currentFile.url);
               connection = (HttpURLConnection) url.openConnection();
               connection.connect();

               if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                  currentFile.errorMessage = "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                  builder.urls.get(i).errorMessage = "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                  continue;
               }

               long fileLength = connection.getContentLength();

               in = new BufferedInputStream(connection.getInputStream());
               fs = new FileOutputStream(currentFile.path);
               out = new BufferedOutputStream(fs);
               byte[] buffer = new byte[16384];

               int len;
               long total = 0;
               while ((len = in.read(buffer, 0, 16384)) != -1) {
                  out.write(buffer, 0, len);

                  total += len;
                  publishProgress((long) builder.urls.size(), (long) (i + 1), total, fileLength);
               }

               currentFile.downloaded = true;
               builder.urls.get(i).downloaded = true;
            } catch (IOException e) {
               currentFile.errorMessage = e.getMessage();
               builder.urls.get(i).errorMessage = e.getMessage();
            } finally {
               try {
                  if (out != null) {
                     out.flush();
                     out.close();
                  }
                  if (in != null) {
                     in.close();
                  }
                  if (connection != null) {
                     connection.disconnect();
                  }
               } catch (IOException ignored) {
                  Log.e("LOG", ignored.getMessage());
               }
            }
         }
         return null;
      }

      @Override
      protected void onPreExecute() {
         super.onPreExecute();
         if (builder.listener != null) {
            builder.listener.onPreDownload();
         }

         PowerManager pm = (PowerManager) builder.context.getSystemService(Context.POWER_SERVICE);
         mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
         mWakeLock.acquire();

         if (dialog != null) {
            dialog.show();
         }
      }

      @Override
      protected void onProgressUpdate(Long... progress) {
         super.onProgressUpdate(progress);

         long downloadCount = progress[0];
         long currentDownload = progress[1];
         long downloadedLength = progress[2];
         long fileLength = progress[3];
         float intPercent = (downloadedLength * 100 / fileLength);
         String percent = String.format("%.2f", (float) downloadedLength * 100 / fileLength);

         isCompleted = false;

         if (builder.listener != null) {
            builder.listener.onProgressUpdate(progress[0], progress[1], (downloadedLength * 100 / fileLength));
         }

         // if we get here, length is known, now set indeterminate to false
         if (dialog != null) {
            prgDownload.setProgress((int) intPercent);
            txtPercent.setText(percent + "%");
            txtCounter.setText(currentDownload + "/" + downloadCount);

            if (builder.showSize) {
               txtDownloadedLength.setText(readableFileSize(downloadedLength));
               txtTotalLength.setText(readableFileSize(fileLength));
            }
         }
      }

      @Override
      protected void onPostExecute(String result) {
         mWakeLock.release();
         isCompleted = true;

         List<FileStruct> errorFiles = getErrorFiles();
         if (errorFiles.size() > 0) {
            if (builder.listener != null) {
               isSuccess = false;
               builder.listener.onError(builder.urls.size(), errorFiles);
            }
         } else {
            if (builder.listener != null) {
               isSuccess = true;
               builder.listener.onComplete();
            }
         }

         if (dialog != null) {
            dialog.dismiss();
         }
      }

      @Override
      protected void onCancelled() {

         if (builder.listener != null) {
            builder.listener.onCancel();
         }

         if (!isCompleted || !isSuccess) {
            if (currentFile != null) {
               File file = new File(currentFile.path);
               file.delete();
            }
         }

         super.onCancelled();
      }
   }
}