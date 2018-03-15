package uark.csce4623.rblowry.todolist;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.ContentHandler;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.widget.ArrayAdapter;

//Create HomeActivity and implement the OnClick listener
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ListView tasks;
    Button deleteButton;
    ArrayAdapter<String> adapter;
    ArrayList<String> contentList;
    ArrayList<String> titleList;
    ArrayList<String> dateList;
    ArrayList<String> finishedList;

    private String LOGTAG = "SAVE_NOTE";
    private String LOCAL_FILE_NAME = "taskList.srl";

    ArrayList<NoteSerializable> NS = new ArrayList<>();
    NoteSerializable myNote = null;

    private ToDoProvider.MainDatabaseHelper mOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeComponents();

        //initialize connectivity broadcast receiver
        MainActivity.ConnectivityBroadcastReceiver myConnBR = new ConnectivityBroadcastReceiver();
        IntentFilter myFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(myConnBR, myFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //fill the list table
        //check if connected
        //if connected, clear the database, and fill database with existing local data
        try {
            fillListTable();
            if (isConnected()) {
                mOpenHelper = new ToDoProvider.MainDatabaseHelper(this);
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                mOpenHelper.onUpgrade(db, 0, 1);
                fillDatabase();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    void createNote() throws IOException {

        //initialize the arraylists
        contentList = new ArrayList<>();
        titleList = new ArrayList<>();
        dateList = new ArrayList<>();
        finishedList = new ArrayList<>();

        //send default data through the intent to the NoteActivity
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra("contentString", "New Note Content");
        intent.putExtra("titleString", "New Note");
        intent.putExtra("dateString", "");
        intent.putExtra("finishedString", "0");
        intent.putExtra("rowId", NS.size()+1);
        startActivity(intent);
    }

    //fill the list table with all of the data in the local file
    void fillListTable() throws IOException, ClassNotFoundException {
        contentList = new ArrayList<>();
        titleList = new ArrayList<>();
        dateList = new ArrayList<>();
        finishedList = new ArrayList<>();

        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = this.openFileInput(LOCAL_FILE_NAME);
            is = new ObjectInputStream(fis);
            NS = (ArrayList<NoteSerializable>)is.readObject();

            //fill up the arraylists that belong to each column
            for (int x = 0; x < NS.size(); x++) {
                titleList.add(NS.get(x).getTitle());
                contentList.add(NS.get(x).getContent());
                dateList.add(NS.get(x).getDueDate());
                finishedList.add(NS.get(x).getIsFinished());
            }

            is.close();
            fis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(adapter==null){
            adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.row,R.id.task_title,titleList);
            tasks.setAdapter(adapter);
        }
        else{
            adapter.clear();
            adapter.addAll(titleList);
            adapter.notifyDataSetChanged();
        }

    }

    //This function is called to fill the database with all
    //of the local tasks
    void fillDatabase() {

        AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {

                while(cursor.moveToNext()){

                    int titleIndex = cursor.getColumnIndex("TITLE");
                    System.out.println(cursor.getString(titleIndex));
                }
            }
        };
        //for each task, insert each task into the database
        for (int x = 0; x < NS.size(); x++) {

            //Create a ContentValues object
            ContentValues myCV = new ContentValues();
            //Put key_value pairs based on the column names, and the values
            myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, NS.get(x).getTitle());
            myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, NS.get(x).getContent());
            myCV.put(ToDoProvider.TODO_TABLE_COL_DUE_DATE, NS.get(x).getDueDate());
            myCV.put(ToDoProvider.TODO_TABLE_COL_IS_FINISHED, NS.get(x).getIsFinished());

            //Perform the insert function using the ContentProvider
            handler.startInsert(-1, null, ToDoProvider.CONTENT_URI, myCV);
        }

        //Set the projection for the columns to be returned
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DUE_DATE,
                ToDoProvider.TODO_TABLE_COL_IS_FINISHED };

        handler.startQuery(-1, null, ToDoProvider.CONTENT_URI, projection, null, null, null);
    }

    //Set the OnClick Listener for buttons and listTable
    void initializeComponents() {
        tasks = (ListView) findViewById(R.id.listTable);
        tasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("contentString", contentList.get(i));
                intent.putExtra("titleString", titleList.get(i));
                intent.putExtra("dateString", dateList.get(i));
                intent.putExtra("finishedString", finishedList.get(i));
                intent.putExtra("rowId", i+1);
                startActivity(intent);
            }
        });
        findViewById(R.id.btnNewNote).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){

        switch (v.getId()){
            //If new Note, call createNewNote()
            case R.id.btnNewNote:
                try {
                    createNote();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.layout.row:
                break;
            default:
                break;
        }
    }

    boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    protected class ConnectivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(isConnected()) {
                Toast.makeText(getApplicationContext(), "Connected",Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Not Connected",Toast.LENGTH_LONG).show();
            }
        }
    }
}
