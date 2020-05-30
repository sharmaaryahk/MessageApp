package com.example.messageapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    int x = 0;
    String  pnr =  new String();
    String  tno = new String();
    String  ast = new String();
    String adt = new String();
    String  ati = new String();
    String  sno = new String();
    ArrayList<String> smsMessagesList = new ArrayList<>();
    ListView messages;
    ArrayAdapter arrayAdapter;
    EditText input;
    SmsManager smsManager = SmsManager.getDefault();
    private static MainActivity inst;

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void updateInbox(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messages = (ListView) findViewById(R.id.messages);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        messages.setAdapter(arrayAdapter);
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
        {
            getPermissionToReadSMS();
        }

        else {
            refreshSmsInbox();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadSMS()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
            {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                refreshSmsInbox();
            }
            else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public void refreshSmsInbox()
    {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        String sender = smsInboxCursor.getString(indexAddress);
        String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                "\n" + smsInboxCursor.getString(indexBody) + "\n";
        String str1 = smsInboxCursor.getString(indexBody);
        arrayAdapter.add(str);
        String[] words=str1.split("\\s");
        String  sen = words[1];


        if (sender.equals("IRCTC") || sen.equals("IRCTC"))
        {
            x=1;
            pnr = words[3];
            tno = words[5];
            ast = words[7];
            adt = words[9];
            ati = words[11];
            sno = words[13];

            //Toast.makeText(this, sen+pnr+tno+ast+adt+ati+sno, Toast.LENGTH_LONG).show();

            Toast.makeText(this, "IRCTC ticket found!!", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Inappropriate Message", Toast.LENGTH_LONG).show();
        }


//messages.setSelection(arrayAdapter.getCount() - 1);
    }

    public void onSendClick(View view)
    {
        if(x==1)
        {
            String info = pnr + "\n" + tno+ "\n" + ast + "\n" + adt + "\n" +ati + "\n" +sno;
            File file = new File(Environment.getExternalStorageDirectory(), "text");
            if (!file.exists()) {
                file.mkdir();
            }
            try {
                File gpxfile = new File(file, "sample");
                FileWriter writer = new FileWriter(gpxfile);
                writer.append(info);
                writer.flush();
                writer.close();
                Toast.makeText(MainActivity.this, "Saved Information", Toast.LENGTH_LONG).show();
            } catch (Exception e) { }
        }
    }
}