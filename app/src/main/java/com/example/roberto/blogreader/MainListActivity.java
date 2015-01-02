package com.example.roberto.blogreader;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainListActivity extends ListActivity {

    public static final int NUMBER_OF_POSTS = 3;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected  JSONObject mblogData;
    protected ProgressBar mProgressBar;
    private final String KEY_TITLE = "title";
    private final String KEY_AUTHOR = "author";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvaiable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            GetBlogPostTask getBlogPostTask = new GetBlogPostTask();
            getBlogPostTask.execute();
        }
        else {
            Toast.makeText(this, "Network is not avaiable!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_list, menu);
        return true;
    }

    private class GetBlogPostTask extends AsyncTask<Object, Void, JSONObject> {



        @Override
        protected JSONObject doInBackground(Object[] params) {
            return doInBack2();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mblogData = result;
            handleBlogResponse();

        }
    }

    private JSONObject doInBack1() {
        int responseCode = -1;
        String url = "http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS;
        JSONObject jsonResponse = null;

        try {
            URL blogFeedUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
            connection.connect();

            responseCode = connection.getResponseCode();
            Reader reader;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                reader = new InputStreamReader(inputStream);

                  /*
                int contentLength = connection.getContentLength();//get the number of characters to read in
                char [] charArray = new char[contentLength];//create the char array to store the the data
                reader.read(charArray); //read and store the data array into the char array
                String responseData = new String(charArray);//create a new string and convert and store from char to string
 //--> comment block                */

                // /*
                int nextCharacter; // read() returns an int, we cast it to char later
                String responseData = "";
                int count = 0;
                while(true){ // Infinite loop, can only be stopped by a "break" statement
                    nextCharacter = reader.read(); // read() without parameters returns one character
                    count++;
                    if(nextCharacter == -1) // A return value of -1 means that we reached the end
                        break;
                    responseData += (char) nextCharacter; // The += operator appends the character to the end of the string
                }
 //--> comment block              // */

                jsonResponse = new JSONObject(responseData);


            }
            else {
                Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "Exception caught: ", e);
        }
        catch (IOException e) {
            Log.e(TAG, "Exception caught: ", e);
        }
        catch (Exception e) {
            Log.e(TAG, "Exception caught: ", e);
        }
        return jsonResponse;
    }

    private JSONObject doInBack2() {
        int responseCode = -1;
        JSONObject jsonResponse = null;
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);

        try {
            HttpResponse response = client.execute(httpget);
            StatusLine statusLine = response.getStatusLine();
            responseCode = statusLine.getStatusCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                jsonResponse = new JSONObject(builder.toString());

                JSONArray jsonPosts = jsonResponse.getJSONArray("posts");
                for (int i = 0; i < jsonPosts.length(); i++) {
                    JSONObject jsonPost = jsonPosts.getJSONObject(i);
                    String title = jsonPost.getString("title");
                    Log.v(TAG, "Post " + i + ": " + title);
                }

                //int contentLength = connection.getContentLength();

                Log.v(TAG, "Length: " + jsonPosts.length());
                Log.v(TAG, "jsonPosts: " + jsonPosts.toString());
                Log.v(TAG, "builder: " + builder.toString());
                //Log.v(TAG, "Length2: " + responseData.length());
                //Log.v(TAG, "Length3: " + contentLength);
                //Log.v(TAG, "Count: " + count);
                Log.v(TAG, "Coincidencia???");

            } else {
                Log.i(TAG, String.format("Unsuccessful HTTP response code: %d", responseCode));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Exception Caught", e);
        } catch (Exception e) {
            Log.e(TAG, "Exception Caught", e);
        }

        return jsonResponse;
    }

    private JSONObject doInBack3() {
        return null;
    }

    private boolean isNetworkAvaiable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvaiable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvaiable = true;
        }
        return isAvaiable;
    }

    private void handleBlogResponse() {
        mProgressBar.setVisibility(View.INVISIBLE);
        
        if (mblogData == null) {
            updateDisplayForErrors();
        }
        else {
            try {
                JSONArray jsonPosts = mblogData.getJSONArray("posts");
                ArrayList<HashMap<String, String>> blogPosts = new ArrayList<>();
                for (int i = 0; i < jsonPosts.length(); i++) {
                    JSONObject post = jsonPosts.getJSONObject(i);
                    String title = post.getString(KEY_TITLE);
                    title = Html.fromHtml(title).toString();
                    String author = post.getString(KEY_AUTHOR);
                    author = Html.fromHtml(author).toString();

                    HashMap<String, String> blogPost = new HashMap<>();
                    blogPost.put(KEY_TITLE, title);
                    blogPost.put(KEY_AUTHOR, author);

                    blogPosts.add(blogPost);
                }

                String[] keys = {KEY_TITLE, KEY_AUTHOR};
                int[] ids = {android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, blogPosts, android.R.layout.simple_list_item_2, keys, ids);
                setListAdapter(adapter);
            } catch (JSONException e) {
                Log.e(TAG, "Exception caught!", e);
            }
        }
    }

    private void updateDisplayForErrors() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.empty));
    }

}
