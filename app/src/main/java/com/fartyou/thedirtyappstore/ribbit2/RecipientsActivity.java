package com.fartyou.thedirtyappstore.ribbit2;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import java.util.ArrayList;
import java.util.List;



//ListActivity
public class RecipientsActivity extends  AppCompatActivity {

    public static Boolean SENT = false;
    public static final String TAG = RecipientsActivity.class.getSimpleName();
    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    protected MenuItem mSendMenuItem;
    ListView mListView;
    protected Uri mMediaUri;
    protected String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);
//        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getStringExtra(ParseConstants.KEY_FILE_TYPE);

        mListView = (ListView) findViewById(R.id.lv);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);


//        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.addAscendingOrder(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {

                if (e == null) {
                    mFriends = friends;

                    String[] usernames = new String[mFriends.size()];
                    int i = 0;
                    for (ParseUser user : mFriends) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(RecipientsActivity.this
                            , android.R.layout.simple_list_item_checked
                            , usernames);

                    mListView.setAdapter(adapter);

                    if (friends.size() > 0) {
                        findViewById(R.id.khali).setVisibility(View.GONE);
                    }

//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                            getListView().getContext(),
//                            android.R.layout.simple_list_item_checked,
//                            usernames);
//                    setListAdapter(adapter);
                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListView.getCheckedItemCount() > 0) {
                    mSendMenuItem.setVisible(true);
                } else {
                    mSendMenuItem.setVisible(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                ParseObject message = createMessage();
                if (message == null) {
                  // error
                  AlertDialog.Builder builder = new AlertDialog.Builder(this);
                   builder.setMessage(R.string.error_selecting_file)
                           .setTitle(R.string.sorry_error)
                          .setPositiveButton(android.R.string.ok, null);

                   AlertDialog dialog = builder.create();
                   dialog.show();
                } else {
                  send(message);
                }
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENTS_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null)
        {
            return null;
        }
        else {
            if(mFileType.equals(ParseConstants.TYPE_IMAGE)){
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this,mMediaUri,mFileType);
            ParseFile file = new ParseFile(fileName,fileBytes);

            file.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    // Handle success or failure here ...
                    Log.d("hey","files away");
                }
            }, new ProgressCallback() {
                public void done(Integer percentDone) {
                    // Update your progress spinner here. percentDone will be between 0 and 100.
                    Log.d("hey", "files at " + percentDone);
                }
            });

            message.put(ParseConstants.KEY_FILE, file);
            return message;
        }
    }

    private ArrayList<String> getRecipientIds() {

        ArrayList<String> recipientIds = new ArrayList<>();
        for (int i = 0; i < mListView.getCount(); i++)
        {
            if (mListView.isItemChecked(i))
            {
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    void send(ParseObject message){

        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null)
                {
                    SENT = true;
                    Toast.makeText(RecipientsActivity.this,"Sent!", Toast.LENGTH_LONG).show();
                }
                else {
                    // error
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(R.string.error_sending_message)
                            .setTitle(R.string.sorry_error)
                            .setPositiveButton(android.R.string.ok,null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        });
    }
}

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        mSendMenuItem =menu.getItem(0);
//       return super.onCreateOptionsMenu(menu);
//   }
//
//  @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//
//        mSendMenuItem.setVisible(true);
//  }

