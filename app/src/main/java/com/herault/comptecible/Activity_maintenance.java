package com.herault.comptecible;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.herault.comptecible.utils.MyHandlerThread;
import com.herault.comptecible.utils.Stockage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Activity_maintenance extends AppCompatActivity  {

    protected Activity context;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private ProgressBar progressBarExport;
    private Stockage stock = null;
    private Spinner archer = null;
    private Spinner round = null;
    private ArrayAdapter adapter;
    private ArrayAdapter adapterRound;

    private EditText pointageOffset = null ;
    long archer_id;
    private Activity_maintenance localActivity;
    private List<Resultat_archer> lresultat;
    private MyHandlerThread handlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localActivity = this;
        setContentView(R.layout.activity_maintenance);
        progressBarExport = findViewById(R.id.am_progressBar);
        this.configureHandlerThread();
        archer = findViewById(R.id.am_sArcher);
        stock = new Stockage();             // init de la classe interface de stockage
        stock.onCreate(this);

        List<String> lArcher = stock.getArchers(false);
        adapter = new ArrayAdapter(
                this,
                R.layout.spinner_generale);

        for (int i = 0; i < lArcher.size(); i++) {
            adapter.add(lArcher.get(i));
        }
        adapter.setDropDownViewResource(R.layout.spinner_generale);
        archer.setAdapter(adapter);

// Suppress Archer

        Button bSuppresArcher = findViewById(R.id.am_bSuppresArcher);
        bSuppresArcher.setOnClickListener(v -> {
            if (archer.getCount() != 0) {
                String name = archer.getSelectedItem().toString();

                AlertDialog.Builder popupValidation = new AlertDialog.Builder(localActivity);
                popupValidation.setMessage(getResources().getString(R.string.archerClean)+name);
                popupValidation.setTitle(getResources().getString(R.string.archerClean));
                popupValidation.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.remove(archer.getSelectedItem());
                        stock.dropArcher(name);

                        if (archer.getCount() == 0) {
                            Intent j = new Intent(Activity_maintenance.this, Activity_config_round.class);
                            startActivity(j);
                            Activity_maintenance.this.finish();
                        }
                    }
                });
                popupValidation.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                popupValidation.show();


            }

        });

// Clear Database
        Button bCleaDataBase = findViewById(R.id.am_bCleaDataBase);
        bCleaDataBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder popupValidation = new AlertDialog.Builder(localActivity);
                popupValidation.setMessage(getResources().getString(R.string.baseAlertClean)+"database");
                popupValidation.setTitle(getResources().getString(R.string.baseClean));
                popupValidation.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stock.dropArchers(false);
                        Intent j = new Intent(Activity_maintenance.this, Activity_config_round.class);
                        startActivity(j);
                        Activity_maintenance.this.finish();
                    }
                });
                popupValidation.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                popupValidation.show();

            }
        });

// Set pointing Offset
        // Get Offset of Pointer
        pointageOffset = findViewById(R.id.pointageOffset);
        String sPointageOffset = stock.getValue("pointageOffset");
        if(sPointageOffset.isEmpty())
        {
            sPointageOffset = "2" ;
        }
        pointageOffset.setText(sPointageOffset);
        pointageOffset.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String sPointageOffset = pointageOffset.getText().toString().trim();
                if (!sPointageOffset.isEmpty()) {
                    switch (sPointageOffset)
                    {
                        case "0":
                        case "1":
                        case "2":
                        case "3": break ;
                        default :   sPointageOffset = "2" ;
                    }
                    stock.updateValue("pointageOffset", sPointageOffset);
                }           // you can call or do what you want with your EditText here
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


// export in file Round for all archers
        Button bExportRoundArchers = findViewById(R.id.am_bexport_round_archers);
        bExportRoundArchers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // test Archer
                if (round.getCount() != 0 && round.getSelectedItemId() >= 0) {
                    // 3 - We create and start our AsyncTask
                    String name = new String(round.getSelectedItem().toString());
                    lresultat = stock.getResultatAll(name);
                    startHandlerThread(lresultat,name);


                }
            }
        });

// Export resultat for one archer for a round

        Button bExportRoundArcher = findViewById(R.id.am_bexport_round_archer);
        bExportRoundArcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // test Archer
                if (round.getCount() != 0 && round.getSelectedItemId() >= 0 && archer.getCount() != 0 && archer.getSelectedItemId() >= 0) {

                 //   String[] argv = new String[]{archer.getSelectedItem().toString() + "_" + round.getSelectedItem().toString()};
                    lresultat = stock.getResultatArrows(archer.getSelectedItem().toString(), round.getSelectedItem().toString());
                  /*  task = new ExportAsyncTask(Activity_maintenance.this);
                    task.execute(argv);*/
                    String name = new String(archer.getSelectedItem().toString() + "_" + round.getSelectedItem().toString());
                    startHandlerThread(lresultat,name);

                }
            }
        });

// export in file Archer for all Rounds

        Button bExportArcherRounds = findViewById(R.id.am_bexport_archer_rounds);
        bExportArcherRounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // test Archer
                if (archer.getCount() != 0 && archer.getSelectedItemId() >= 0) {
                    // 3 - We create and start our AsyncTask
                    String name = new String(archer.getSelectedItem().toString());
                    lresultat = stock.getResultatAllRound(name);
                    name += "_all.csv";
                    startHandlerThread(lresultat,name);

                }
            }
        });

//-------------------------------------------------------------------------------------------------

        round = findViewById(R.id.sRound);
        List<String> lRound = stock.getRounds();
        adapterRound = new ArrayAdapter(
                this,
                R.layout.spinner_generale
        );

        for (int i = 0; i < lRound.size(); i++) {
            adapterRound.add(lRound.get(i));
        }
        adapterRound.setDropDownViewResource(R.layout.spinner_generale);
        round.setAdapter(adapterRound);

        Button bSuppressRound = findViewById(R.id.am_bSup_round);
        bSuppressRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (round.getCount() != 0 && round.getSelectedItemId() >= 0) {
                    String name = round.getSelectedItem().toString();
                    AlertDialog.Builder popupValidation = new AlertDialog.Builder(localActivity);
                    popupValidation.setMessage(getResources().getString(R.string.roundClean) + name);
                    popupValidation.setTitle(getResources().getString(R.string.roundClean));
                    popupValidation.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            adapterRound.remove(round.getSelectedItem());
                            stock.supRound(name);

                            if (round.getCount() == 0) {
                                Intent j = new Intent(Activity_maintenance.this, Activity_config_round.class);
                                startActivity(j);
                                Activity_maintenance.this.finish();
                            }
                        }
                    });
                    popupValidation.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    popupValidation.show();

                }
            }
        });

// request write protection external memory

   /*    if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
            } else {
                requestPermission(); // Code for permission
            }
        } else {
            // Code for Below 23 API Oriented Device
            // Do next code
        } */

    }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(Activity_maintenance.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(Activity_maintenance.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(Activity_maintenance.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void  onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

//---------------------------------------
    // -----------------
    // CONFIGURATION
    // -----------------

    // 2 - Configuring the HandlerThread
    private void configureHandlerThread(){
        handlerThread = new MyHandlerThread(this,"MyAwesomeHandlerThread", this.progressBarExport);
    }

    // -------------------------------------------
    // BACKGROUND TASK (HandlerThread & AsyncTask)
    // -------------------------------------------

    // 4 - EXECUTE HANDLER THREAD
    private void startHandlerThread(List <Resultat_archer>lresultat,String name){

        handlerThread.startHandler(lresultat,name);
    }

//--------------------------------------
    /*********************************************************************************/
    /** Managing LifeCycle and database open/close operations ************************/
    /*********************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        // Open stockage
        stock.openDB();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Close stockage
        stock.closeDB();
    }

    @Override
    protected void onDestroy() {
        // 3 - QUIT HANDLER THREAD (Free precious resources)
        handlerThread.quit();
        super.onDestroy();
    }


}
