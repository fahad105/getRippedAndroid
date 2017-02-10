package com.androidlearningproject.getripped;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidlearningproject.getripped.API.APIHandler;
import com.androidlearningproject.getripped.API.APIHandlerInterface;
import com.androidlearningproject.getripped.API.ResponseEntities.WeightEntry;
import com.androidlearningproject.getripped.Adapters.WeightAdapter;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

import static android.view.View.inflate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private WeightAdapter adapter;
    private ArrayList<WeightEntry> entries;
    private Calendar now;
    private APIHandlerInterface apiService;

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

        now = Calendar.getInstance();

        APIHandler api = new APIHandler();
        apiService = api.getClient().create(APIHandlerInterface.class);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        registerForContextMenu(listView);

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
                for (WeightEntry entry : retrievedEntries) {
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
                        .setView(inflate(view.getContext(), R.layout.create_weight_entry_dialog, null))
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

                EditText et = (EditText) dialog.findViewById(R.id.date_value);
                String date = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH);
                et.setText(date);

                EditText weightEt = (EditText) dialog.findViewById(R.id.weight_value);
                weightEt.requestFocus();
                ImageButton btn = (ImageButton) dialog.findViewById(R.id.btn_date_dialog);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

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

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            WeightEntry selectedEntry = entries.get(info.position);
            Log.d("ListView", "Selected entry: " + selectedEntry.id);
            String[] menuItems = getResources().getStringArray(R.array.context_menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    public void callDeleteWeightEntry(WeightEntry entry, final int index) { // TODO: Make these call methods for Create/Update/Get as well
        Call<ResponseBody> call = apiService.deleteWeightEntry(entry.id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int statusCode = response.code();
                Log.d("API", statusCode + "");
                if (statusCode == 204) {
                    Toast.makeText(MainActivity.this, "Succesfully deleted entry", Toast.LENGTH_SHORT).show();
                    entries.remove(index); // this could be a problem with concurrency, probably not an issue if user accounts are used.
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Invalid input!", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                Log.d("API", "ERROR");

            }
        });
    }

    public void callEditWeightEntry(final WeightEntry entry, final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit entry")
                .setView(R.layout.create_weight_entry_dialog)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("EDIT", "CLICKED OK");

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d("EDIT", "CLICKED CANCEL");

                    }
                });


        final AlertDialog dialog = builder.create();
        dialog.show();

        TextInputEditText date = (TextInputEditText) dialog.findViewById(R.id.date_value);
        date.setText(entry.timestamp.split("T")[0]);

        TextInputEditText weight = (TextInputEditText) dialog.findViewById(R.id.weight_value);
        weight.setText(entry.value + "");

        TextInputEditText remark = (TextInputEditText) dialog.findViewById(R.id.remark_value);
        remark.setText(entry.remark);

        ImageButton btn = (ImageButton) dialog.findViewById(R.id.btn_date_dialog);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MainActivity.this,
                        Integer.parseInt(entry.timestamp.split("T")[0].split("-")[0]), //year
                        Integer.parseInt(entry.timestamp.split("T")[0].split("-")[1]) - 1, //month
                        Integer.parseInt(entry.timestamp.split("T")[0].split("-")[2]) //day of month
                );

                dpd.show(getFragmentManager(), "Datepickerdialog");
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

        Button editButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update entry in adapter
                TextView date = (TextView) dialog.findViewById(R.id.date_value);
                entry.timestamp = date.getText().toString();

                TextView tv = (TextView) dialog.findViewById(R.id.weight_value);
                entry.value = Double.parseDouble(tv.getText().toString());

                TextView remark = (TextView) dialog.findViewById(R.id.remark_value);
                entry.remark = remark.getText().toString();

                Call<ResponseBody> call = apiService.editWeightEntry(entry, entry.id);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        int statusCode = response.code();
                        Log.d("API", statusCode + "");

                        if (statusCode == 204) {
                            Toast.makeText(MainActivity.this, "Succesfully edited entry", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                        }

                        dialog.dismiss();

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                        Log.d("API", "ERROR");

                        //at this point entry was changed in adapter but not in database bacause the API call failed. reset adapter?
                        adapter.clear();
                        Call<WeightEntry[]> callGetAll = apiService.getWeightEntries();
                        callGetAll.enqueue(new Callback<WeightEntry[]>() {
                            @Override
                            public void onResponse(Call<WeightEntry[]> call, Response<WeightEntry[]> response) {

                                int statusCode = response.code();
                                Log.d("API", statusCode + "");

                                WeightEntry[] retrievedEntries = response.body();
                                for (WeightEntry entry : retrievedEntries) {
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
                    }
                });

                dialog.dismiss();
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        WeightEntry selectedEntry = entries.get(info.position);


        switch (menuItemIndex) {
            case 0://Edit
                Log.d("ListView", "Edit clicked");
                callEditWeightEntry(selectedEntry, info.position);
                break;
            case 1://Delete
                Log.d("ListView", "Delete clicked");
                callDeleteWeightEntry(selectedEntry, info.position);
                break;
        }

        return true;
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



    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();


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
