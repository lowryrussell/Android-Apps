package uark.csce4623.rblowry.todolist;

import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by Russ on 10/6/17.
 */

public class NoteSerializable implements java.io.Serializable {

    private String mTitle;
    private String mContent;
    private String mDueDate;
    private String mIsFinished;

    private static final long serialVersionUID = -8540820939918218779L;

    NoteSerializable(String title, String content, String dueDate, String isFinished) {

        mTitle = title;
        mContent = content;
        mDueDate = dueDate;
        mIsFinished = isFinished;
    }

    NoteSerializable() {

        mTitle = "";
        mContent = "";
        mDueDate = "";
        mIsFinished = "";
    }

    void setTitle(String title) {

        mTitle = title;
    }

    void setContent(String content) {

        mContent = content;
    }

    void setDueDate(String dueDate) {

        mDueDate = dueDate;
    }

    void setIsFinished(String isFinished) {

        mIsFinished = isFinished;
    }

    String getTitle() {

        return mTitle;
    }

    String getContent() {

        return mContent;
    }

    String getDueDate() {

        return mDueDate;
    }

    String getIsFinished() {

        return mIsFinished;
    }

    @Override
    public String toString() {

        return mTitle + ": " + mContent + ": " + mDueDate + ": " + mIsFinished;
    }
}
