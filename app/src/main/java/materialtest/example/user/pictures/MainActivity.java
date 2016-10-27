package materialtest.example.user.pictures;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * MainActivity of App, download info from Flickr using its API
 * Import EndlessRecyclerViewScrollListener Class for endless scrolling
 * Created by Nigel
 */

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private ArrayList<Image> imageArrayList;
    private AsyncTaskRunner runner;
    private RecyclerViewAdapter recyclerViewAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private String search = "hello";
    private int currentPage = 1;
    private boolean internet = true;
    private boolean swipe = false;
    private int selected = -1;
    private int mColumnCount = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ProgressBar
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        initializeView();

        //swipe to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipe = true;
                imageArrayList.clear();
                currentPage = 1;
                recyclerViewAdapter.notifyDataSetChanged();
                runner = new AsyncTaskRunner();
                runner.execute(search);
                scrollListener.resetState();
                swipe = false;
            }
        });

        //Floating Action Button
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selected = -1;

                final CharSequence[] items = {"1", "2", "3", "4"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select Grid Column Size");
                builder.setSingleChoiceItems(items, selected,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                selected = Integer.parseInt((String) items[i]);
                            }
                            // indexSelected contains the index of item (of which checkbox checked)

                        })
                        // Set the action buttons
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //overriding this to prevent dialog from closing if no filter is selected
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                final AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (selected == -1) {
                            Toast.makeText(MainActivity.this, "Please select filer(s)", Toast.LENGTH_SHORT).show();

                        } else {
                            mColumnCount = selected;
                            initializeView();
                            dialog.dismiss();
                        }
                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });
            }
        });
    }

    //Enable search function on the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (runner.getStatus().equals(AsyncTask.Status.RUNNING)) {
            runner.cancel(true);
        }

        search = query;
        imageArrayList.clear();
        recyclerViewAdapter.notifyDataSetChanged();
        runner = new AsyncTaskRunner();
        runner.execute(search);
        scrollListener.resetState();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    //METHODS

    //Initialize the view and set how it display the pictures
    private void initializeView() {
        //Resizing Image
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int mImageWidth = displayMetrics.widthPixels / mColumnCount;
        int mImageHeight = displayMetrics.widthPixels / mColumnCount;
        //int mImageHeight = mImageWidth * 4 / 3;

        //Layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, mColumnCount);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);

        //Adapter and Array List
        imageArrayList = new ArrayList<>();
        recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, imageArrayList, mImageHeight, mImageWidth);
        recyclerView.setAdapter(recyclerViewAdapter);

        //Scroll listener
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                loadNextDataFromApi(page);
            }
        };

        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);
        scrollListener.resetState();

        //start download
        currentPage = 1;
        runner = new AsyncTaskRunner();
        runner.execute(search);

    }

    //get JSON
    public ArrayList<Image> getJSON(String string) {

        //Fetch 10 JSON data per page. Page number increase when scrolled at the end
        String result = null;
        String flickrQuery_url = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
        String flickrQuery_per_page = "&per_page=10";//set per page to 10
        String flickrQuery_page = "&page=";
        String flickrQuery_nojsoncallback = "&nojsoncallback=1";
        String flickrQuery_format = "&format=json";
        String flickrQuery_tag = "&tags=";
        String flickrQuery_key = "&api_key=";
        String flickrApiKey = "2155e9406043b7494453105eec99ae37";
        String URL = flickrQuery_url + flickrQuery_per_page + flickrQuery_page + currentPage + flickrQuery_nojsoncallback + flickrQuery_format + flickrQuery_tag + string + flickrQuery_key + flickrApiKey;

        try {
            URL url = new URL(URL);//url path
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            mProgressBar.setProgress(50);
            try {
                InputStream inputStream = urlConnection.getInputStream();//open stream to read from the database
                Reader in = new InputStreamReader(inputStream);
                mProgressBar.setProgress(70);
                BufferedReader bufferedreader = new BufferedReader(in);
                StringBuilder stringBuilder = new StringBuilder();
                String stringReadLine;
                while ((stringReadLine = bufferedreader.readLine()) != null) {
                    stringBuilder.append(stringReadLine);
                }
                result = stringBuilder.toString();
                mProgressBar.setProgress(90);
                urlConnection.disconnect();
                internet = true;
                imageArrayList = getImageURL(result);

            } catch (UnknownHostException e) {
                internet = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageArrayList;
    }

    //get picture url
    public ArrayList<Image> getImageURL(String JSON) {

        mProgressBar.setProgress(100);

        String imageURL;

        try {

            JSONObject JsonObject = new JSONObject(JSON);
            JSONObject Json_photos = JsonObject.getJSONObject("photos");
            JSONArray JsonArray_photo = Json_photos.getJSONArray("photo");

            //Iterate the jsonArray and print the info of JSONObjects
            for (int i = 0; i < JsonArray_photo.length(); i++) {
                JSONObject jsonObject = JsonArray_photo.getJSONObject(i);

                String id = jsonObject.optString("id");
                String secret = jsonObject.optString("secret");
                String server = jsonObject.optString("server");
                String farm = jsonObject.optString("farm");

                imageURL = "http://farm" + farm + ".static.flickr.com/" + server + "/" + id + "_" + secret + ".jpg";

            /*Uses picasso to download instead
                //download picture
                try {
                    URL url = new URL(imageURL);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            */
                Image image = new Image();
                image.setURL(imageURL);
                imageArrayList.add(image);
            }

        } catch (JSONException e) {
            e.printStackTrace();

        }
        return imageArrayList;
    }

    //Infinite scrolling
    private void loadNextDataFromApi(int page) {
        currentPage = page;
        runner = new AsyncTaskRunner();
        runner.execute(search);
        //Toast.makeText(getApplicationContext(), "Page is " + page, Toast.LENGTH_LONG).show();
    }

    //accept the search string and try to get the json format file
    public class AsyncTaskRunner extends AsyncTask<String, Void, ArrayList<Image>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!swipe) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mProgressBar.setProgress(30);
        }

        @Override
        protected ArrayList<Image> doInBackground(String... strings) {

            //JSON
            getJSON(strings[0]);// get json
            //check if there is internet
            return imageArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Image> stringsArray) {
            recyclerViewAdapter.notifyDataSetChanged();
            if (!swipe) {
                mProgressBar.setVisibility(View.GONE);
            }
            if (!internet) {
                Toast.makeText(getApplicationContext(), "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}

