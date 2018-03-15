package uark.csce4623.rblowry.todolist;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Russ on 10/6/17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Sets an ID for the notification
        int mNotificationId = 001;

        Notification notification = new Notification.Builder(context)
                .setContentTitle("ToDoList")
                .setContentText("You have a new task ready to complete!")
                .setSmallIcon(R.drawable.notification_icon)
                .setChannelId(Integer.toString(mNotificationId))
                .build();

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notification);
    }
}
