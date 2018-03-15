package uark.csce4623.rblowry.todolist;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Russ on 10/2/17.
 */

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {

    String titleString;
    String contentString;
    String dateString;
    int rowId;

    TextView content;
    TextView title;
    TextView date;
    CheckBox finished;
    Button deleteButton;

    PendingIntent pi;
    AlarmManager am;

    private String LOGTAG = "SAVE_NOTE";
    private String LOCAL_FILE_NAME = "taskList.srl";

    ConnectivityBroadcastReceiver myConnBR;

    ArrayList<NoteSerializable> NS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        finished = (CheckBox) findViewById(R.id.doneCheck);
        content = (TextView) findViewById(R.id.etNoteContent);
        title = (TextView) findViewById(R.id.tvNoteTitle);
        date = (TextView) findViewById(R.id.etDatePicker);
        deleteButton = (Button) findViewById(R.id.dltBtn);

        Bundle extras = getIntent().getExtras();
        rowId = extras.getInt("rowId");
        System.out.println(rowId);

        try {
            initializeComponents();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setupBroadcast();

        try {
            loadTaskList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //initialize connectivity broadcast receiver
        myConnBR = new ConnectivityBroadcastReceiver();
        IntentFilter myFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(myConnBR, myFilter);
    }

    //set up all of the views
    void initializeComponents() throws ParseException {
        Bundle extras = getIntent().getExtras();

        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.dltBtn).setOnClickListener(this);

        if (extras.getString("titleString").equals("")) {
            titleString = "New Note";
        }
        else {
            titleString = extras.getString("titleString");
        }

        if (extras.getString("contentString").equals("")) {
            contentString = "New Note Content";
        }
        else {
            contentString = extras.getString("contentString");
        }

        if (extras.getString("dateString").equals("")) {
            dateString = "MM/dd/yyyy hh:mm pm/am";
        }
        else {
            dateString = extras.getString("dateString");
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
            Date date = new Date(Long.parseLong(dateString));
            dateString = format.format(date);
        }

        if (extras.getString("finishedString").equals("false")) {
            finished.setChecked(false);
        }
        else {
            finished.setChecked(true);
        }

        content.setText(contentString);
        title.setText(titleString);
        date.setText(dateString);
    }

    //check if connection isConnected
    boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    //setup broadcast receiver
    void setupBroadcast() {

        pi = PendingIntent.getBroadcast(this, 0, new Intent(getApplicationContext(), AlarmReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        am = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
    }

    void deleteNote() {

        //delete the note in index rowId - 1
        NS.remove(rowId-1);

        //overwrite the notes object again
        try {
            FileOutputStream fos = this.openFileOutput(LOCAL_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(NS);
            os.close();
            fos.close();
        }
        catch (java.io.FileNotFoundException e) {
            Log.e(LOGTAG, "File Not Found. FileNotFoundException: " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(NoteActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            //If updating note, call updateNewTask()
            case R.id.btnSave:
                try {
                    updateNewTask();
                } catch (ParseException e) {
                    Toast.makeText(getApplicationContext(), "Invalid Date!",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.dltBtn:
                deleteNote();
                break;
            default:
                break;
        }
    }

    void loadTaskList() throws IOException {

        //pull in the local tasks list
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = this.openFileInput(LOCAL_FILE_NAME);
            is = new ObjectInputStream(fis);
            NS = (ArrayList<NoteSerializable>)is.readObject();

            is.close();
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void updateNewTask() throws ParseException {

        //Format the date from date format the timestamp format
        String dateStr = date.getText().toString();
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        Date date = formatter.parse(dateStr);

        //This determined if this is a new task.
        //If new task, jump inside this if statement.
        //If editing existing task, jump inside the else statement.
        if (rowId > NS.size()) {

            NoteSerializable newNote = new NoteSerializable(title.getText().toString(), content.getText().toString(), Long.toString(date.getTime()), Boolean.toString(finished.isChecked()));
            NS.add(newNote);

            try {
                FileOutputStream fos = this.openFileOutput(LOCAL_FILE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(NS);
                os.close();
                fos.close();
            }
            catch (java.io.FileNotFoundException e) {
                Log.e(LOGTAG, "File Not Found. FileNotFoundException: " + e.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {

            NS.get(rowId-1).setTitle(title.getText().toString());
            NS.get(rowId-1).setContent(content.getText().toString());
            NS.get(rowId-1).setDueDate(Long.toString(date.getTime()));
            NS.get(rowId-1).setIsFinished(Boolean.toString(finished.isChecked()));

            try {
                FileOutputStream fos = this.openFileOutput(LOCAL_FILE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(NS);
                os.close();
                fos.close();
            }
            catch (java.io.FileNotFoundException e) {
                Log.e(LOGTAG, "File Not Found. FileNotFoundException: " + e.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        System.out.println(cal.getTimeInMillis());

        Time time = new Time(date.getTime());

        //Only set a timer if the current time is less than the specified date in the future
        if (System.currentTimeMillis() < cal.getTimeInMillis()) {

            am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        }
    }

    protected class ConnectivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(isConnected()) {
                Toast.makeText(getApplicationContext(), "Connected",Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Disconnected",Toast.LENGTH_LONG).show();
            }
        }
    }

    //unregister the broadcast receiver
    @Override
    protected void onStop() {
        unregisterReceiver(myConnBR);
        super.onStop();
    }

}
