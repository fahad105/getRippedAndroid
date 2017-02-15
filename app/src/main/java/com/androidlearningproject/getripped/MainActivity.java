package com.androidlearningproject.getripped;

import android.app.FragmentManager;
import android.content.DialogInterface;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private WeightAdapter adapter;
    private ArrayList<WeightEntry> entries;
    private Calendar now;
    private APIHandlerInterface apiService;
    private FragmentManager fragmentManager;

    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        fragmentManager = getFragmentManager();
        setSupportActionBar(toolbar);
        this.setTitle(R.string.toolbar_dashboard);
        now = Calendar.getInstance();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        APIHandler api = new APIHandler();
        apiService = api.getClient().create(APIHandlerInterface.class);

        registerForContextMenu(listView);
        entries = new ArrayList<WeightEntry>();
        adapter = new WeightAdapter(MainActivity.super.getApplicationContext(), entries);
        listView.setAdapter(adapter);

        callGetAllWeightEntries();
    }

    @OnClick(R.id.fab)
    public void openCreateEntryDialog(View view) {
        View dialogView = View.inflate(view.getContext(), R.layout.create_weight_entry_dialog, null);
        final AlertDialog dialog = openCreateOrEditDialog("New weight entry", "Create", dialogView);
        ButterKnife.bind(dialogView);
        dialog.show();

        final EditText dateEt = ButterKnife.findById(dialogView, R.id.date_value);
        String date = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH);
        dateEt.setText(date);

        final EditText weightEt = ButterKnife.findById(dialogView, R.id.weight_value);
        weightEt.requestFocus();

        final EditText remarkEt = ButterKnife.findById(dialogView, R.id.remark_value);

        ImageButton dateDialogButton = ButterKnife.findById(dialog, R.id.btn_date_dialog);
        dateDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MainActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );

                dpd.show(fragmentManager, "Datepickerdialog");
                dpd.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                        dateEt.setText(date);
                    }
                });
            }
        });

        Button createButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeightEntry entry = new WeightEntry();

                entry.timestamp = dateEt.getText().toString();
                entry.value = Double.parseDouble(weightEt.getText().toString());
                entry.remark = remarkEt.getText().toString();

                callCreateWeightEntry(entry);
                dialog.dismiss();
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

    public void callCreateWeightEntry(WeightEntry entry) {
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
            }

            @Override
            public void onFailure(Call<WeightEntry> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Invalid input!", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                Log.d("API", "ERROR");

            }
        });

    }

    public void callGetAllWeightEntries() {
        adapter.clear();
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
        View dialogView = View.inflate(MainActivity.this, R.layout.create_weight_entry_dialog, null);
        final AlertDialog dialog = openCreateOrEditDialog("Edit weight entry", "Save", dialogView);
        ButterKnife.bind(dialogView);
        dialog.show();

        final TextInputEditText dateInput = ButterKnife.findById(dialogView, R.id.date_value);
        dateInput.setText(entry.timestamp.split("T")[0]);

        final TextInputEditText weightInput = ButterKnife.findById(dialogView, R.id.weight_value);
        weightInput.setText(entry.value + "");

        final TextInputEditText remarkInput = ButterKnife.findById(dialogView, R.id.remark_value);
        remarkInput.setText(entry.remark);

        ImageButton btn = ButterKnife.findById(dialogView, R.id.btn_date_dialog);
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
                        dateInput.setText(date);
                    }
                });
            }
        });

        Button editButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update entry in adapter
                entry.timestamp = dateInput.getText().toString();
                entry.value = Double.parseDouble(weightInput.getText().toString());
                entry.remark = remarkInput.getText().toString();

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

                        callGetAllWeightEntries();
                    }
                });
                dialog.dismiss();
            }
        });
    }

    public AlertDialog openCreateOrEditDialog(String createOrEditTitleString, String createOrEditButtonString, View view){
        View dialogView = view;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(createOrEditTitleString)
                .setView(dialogView)
                .setPositiveButton(createOrEditButtonString, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        return dialog;
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.d("myTag", "settings clicked");
            //make settings Activity
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

        //TODO: nav drawer options for Weight, Exercises, Goals, Settings once those activities are implemented
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
    }
}
