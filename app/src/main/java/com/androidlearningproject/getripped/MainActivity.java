package com.androidlearningproject.getripped;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidlearningproject.getripped.API.APIHandler;
import com.androidlearningproject.getripped.API.APIHandlerInterface;
import com.androidlearningproject.getripped.API.ResponseEntities.WeightEntry;
import com.androidlearningproject.getripped.Adapters.WeightAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private WeightAdapter adapter;
    private ArrayList<WeightEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(R.string.toolbar_dashboard);

        final FragmentManager fragmentManager = getFragmentManager();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        APIHandler api = new APIHandler();
        final APIHandlerInterface apiService = api.getClient().create(APIHandlerInterface.class);

        final ListView listView = (ListView) findViewById(R.id.list_view);

        entries = new ArrayList<WeightEntry>();

        adapter = new WeightAdapter(MainActivity.super.getApplicationContext(), entries);
        listView.setAdapter(adapter);

        Call<WeightEntry[]> call = apiService.getWeightEntries();
        call.enqueue(new Callback<WeightEntry[]>() {
            @Override
            public void onResponse(Call<WeightEntry[]> call, Response<WeightEntry[]> response) {

                int statusCode = response.code();
                Log.d("API", statusCode + "");

                WeightEntry[] retrievedEntries = response.body();
                for( WeightEntry entry : retrievedEntries){
                    entries.add(entry);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<WeightEntry[]> call, Throwable t) {
                t.printStackTrace();
                Log.d("API", "ERROR");
                Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(R.string.new_title)
                        .setView(view.inflate(view.getContext(), R.layout.create_weight_entry_dialog, null))
                        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });


                final AlertDialog dialog = builder.create();

                dialog.show();

                Button btn = (Button) dialog.findViewById(R.id.btn_date_dialog);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Calendar now = Calendar.getInstance();
                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                MainActivity.this,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );

                        dpd.show(fragmentManager, "Datepickerdialog");
                        Log.d("DATE BTN", "clicked");
                        dpd.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                EditText et = (EditText) dialog.findViewById(R.id.date_value);
                                et.setText(date);
                            }
                        });
                    }


                });

                Button createButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WeightEntry entry = new WeightEntry();

                        TextView date = (TextView) dialog.findViewById(R.id.date_value);
                        entry.timestamp = date.getText().toString();


                        TextView tv = (TextView) dialog.findViewById(R.id.weight_value);
                        entry.value = Double.parseDouble(tv.getText().toString());

                        TextView remark = (TextView) dialog.findViewById(R.id.remark_value);
                        entry.remark = remark.getText().toString();

                        Call<WeightEntry> call = apiService.createWeightEntry(entry);
                        call.enqueue(new Callback<WeightEntry>() {
                            @Override
                            public void onResponse(Call<WeightEntry> call, Response<WeightEntry> response) {
                                int statusCode = response.code();
                                Log.d("API", statusCode + "");

                                WeightEntry createdEntry = response.body();
                                entries.add(createdEntry);
                                adapter.notifyDataSetChanged();

                                Toast.makeText(MainActivity.this, "Created entry", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<WeightEntry> call, Throwable t) {
                                Toast.makeText(MainActivity.this, "Invalid input!", Toast.LENGTH_SHORT).show();
                                t.printStackTrace();
                                Log.d("API", "ERROR");

                            }
                        });
                    }
                });


            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("myTag", "settings clicked");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            Log.d("myTag", "share clicked");
        }

        //nav drawer options for Weight, Exercises, Goals, Settings

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onResume() {
        Log.d("RESUME", "resumed");
        super.onResume();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
    }
}
