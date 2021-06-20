package com.example.myapplication2;
//package net.kcundercover.jdsp.math;
//import net.kcundercover.jdsp.math.Vector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import biz.source_code.dsp.math.Complex;
import biz.source_code.dsp.transform.Dft;


public class MainActivity extends AppCompatActivity implements SensorEventListener,View.OnClickListener {

    private SensorManager mSensorMgr;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    // for printing wifi info
    private TextView wifi_info;
    // for printing acc info
    private TextView acc_info;
    // for printing training info
    private TextView training_overview;
    // for printing wifi feature
    private TextView wifi_feature;
    // for printing wifi feature
    private TextView acc_feature;
    // for printing final result
    private TextView result;

    // for entering cell number
    private EditText cellNo;
    // for entering action
    private EditText movementNo;
    private EditText experimentNo;

    public String TAG="my_app";
    public String Rawdata;
    int index;
    // result
    int Cell1_Value, Cell2_Value, Cell3_Value, Cell4_Value;
    int still_value, move_value, rasputin_value;

    int WINDOWS_SIZE = 40;
    int Train_MAX = 800;
    int Test_MAX = 120;
    int test_count;
    float Test_Round = 2;
    int Test_still = 0, Test_move = 0, Test_rasputin = 0;
    int Test_cell1 = 0, Test_cell2 = 0, Test_cell3 = 0, Test_cell4 = 0;
    boolean states;

    public class Testing {
        double[] testing_acc_x = new double[Test_MAX-40];
        double[] testing_acc_y = new double[Test_MAX-40];
        double[] testing_acc_z = new double[Test_MAX-40];
        double[] testing_wifi1 = new double[Test_MAX-40];
        double[] testing_wifi2 = new double[Test_MAX-40];

        double p1 = 0;
        double p2 = 0;

        double max_z1, max_z2, max_z3;
        int maxIndex_z1, maxIndex_z2, maxIndex_z3;
    }
    Testing test_data = new Testing();

    String w1 = "star_platinum", w2 = "Heaven's_Door";

    WifiManager wifia;

    // for files
    File RootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS1");
    File RawRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS1/raw");  // see if the required folders already exist. >>
    File FeatureRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS1/feature");
    File TestingRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS1/test");

    Map<Integer, Integer> roomMap = new HashMap<>();
    Map<Integer, Double> distanceMap = new HashMap<>();
    Map<Integer, Integer> numberMap = new HashMap<>();
    Map<Integer, String> actionMap = new HashMap<>();
    Map<Integer, Double> distanceMap_cell = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt=findViewById(R.id.bt_start);
        bt.setOnClickListener(this);

        Button bt_stop=findViewById(R.id.bt_stop);
        bt_stop.setOnClickListener(this);

        Button bt_feature=findViewById(R.id.bt_feature);
        bt_feature.setOnClickListener(this);

        Button bt_testing=findViewById(R.id.bt_testing);
        bt_testing.setOnClickListener(this);

        wifi_info = findViewById(R.id.wifi_info);
        acc_info = findViewById(R.id.acc_info);
        training_overview = findViewById(R.id.training_overview);
        wifi_feature = findViewById(R.id.wifi_feature);
        acc_feature = findViewById(R.id.acc_feature);

        cellNo=findViewById(R.id.cell);
        experimentNo = findViewById(R.id.experimentNo);
        movementNo=findViewById(R.id.movement);

        result = findViewById(R.id.result);
        result.setMovementMethod(ScrollingMovementMethod.getInstance());


        //
        mSensorMgr=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifia = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        index = 0;
        test_count = 0;
        test_data.max_z1 = 0; test_data.max_z2 = 0; test_data.max_z3 = 0;
        test_data.maxIndex_z1 = 0; test_data.maxIndex_z2 = 0; test_data.maxIndex_z3 = 0;
        test_data.p1 = 0; test_data.p2 = 0;
    }

    protected void onPause()
    {
        super.onPause();
        mSensorMgr.unregisterListener(this);
    }

    protected void onResume()
    {
        super.onResume();
    }
    protected void onStop()
    {
        super.onStop();
        mSensorMgr.unregisterListener(this);

    }
    public void onSensorChanged(SensorEvent event)
    {
        String wifi, wifi1 = ", ", wifi2 = ", ";
        String acc;
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
        {
            Rawdata= " ";

            float[] values=event.values;
            wifia.startScan();

            List<ScanResult> scanResult = wifia.getScanResults();
            wifi_feature.setText(scanResult.get(0).SSID);
            for (int i = 0; i < scanResult.size(); i++) {
                if (scanResult.get(i).SSID.equals(w1)) {

                    wifi1 = scanResult.get(i).SSID + ", " + scanResult.get(i).level;
                }
                if (scanResult.get(i).SSID.equals(w2)) {
                    wifi2 = scanResult.get(i).SSID + ", " + scanResult.get(i).level;
                }
            }

            wifi = wifi1 + ", " + wifi2;
            acc = Float.toString(values[0]) +", "+ Float.toString(values[1]) +", "+ Float.toString(values[2]);
            Rawdata = Float.toString(index) +", " + acc + ", " + wifi + ", " + Long.toString(System.currentTimeMillis());
            wifi_info.setText(wifi);
            acc_info.setText(acc);
            int N = Train_MAX;
            if (!states){
                N = Test_MAX;
            }
            String filename;
            File RootF;
            if (states)
            {
                String cell = cellNo.getText().toString();
                String movement = movementNo.getText().toString();
                String experiment = experimentNo.getText().toString();
                filename = cell + "_" + movement + "_" + experiment + "_"+ "CollectedData.txt";
                RootF = RawRootFile;
                if (index < N)
                    {
                        addToFile(Rawdata, RootF, filename, true);
                    }
                    else
                    {
                        mSensorMgr.unregisterListener(this);
                        training_overview.setText(Integer.toString(index) + " data stored for " + filename);
                        index = 0;
                        return;
                    }
            }
            else
            {
                if (index < N && index >= 80) {
                    String[] w = wifi.split(", ");
                    test_data.testing_acc_x[index-80] = values[0];
                    test_data.testing_acc_y[index-80] = values[1];
                    test_data.testing_acc_z[index-80] = values[2];
//                    result.append("1" + Double.toString(test_data.p1) + ", " + Double.toString(test_data.p2) + "\n");
                    test_data.p1 += Math.pow(10,0.2*Float.parseFloat(w[1]));
                    test_data.p2 += Math.pow(10,0.2*Float.parseFloat(w[3]));
//                    if (! wifi1.split(", ")[1].equals(""))
//                    {
//                        test_data.testing_wifi1[index] = Double.parseDouble(wifi1.split(", ")[1]);
//                    }
//                    else
//                    {
//                        test_data.testing_wifi1[index] = -1000;
//                    }
//                    if (! wifi2.split(", ")[1].equals(""))
//                    {
//                        test_data.testing_wifi2[index] = Double.parseDouble(wifi2.split(", ")[1]);
//                    }
//                    else
//                    {
//                        test_data.testing_wifi2[index] = -1000;
//                    }
                    Log.i(TAG,"Testing adding");
                }
                else if (index < 80) { }
                else
                {
                    mSensorMgr.unregisterListener(this);
                    training_overview.setText(Integer.toString(index-40) + " data stored for testing");
                    Log.i(TAG,"Testing added"); index = 0;
                    check();
                    test_count++;
                    test_data.p1 = 0; test_data.p2 = 0;
                    if(test_count < Test_Round){
                        // restart a test
                        mSensorMgr.unregisterListener(this,mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                        mSensorMgr.registerListener(this,
                                mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_UI);
                    }
                    else
                    {
                        mSensorMgr.unregisterListener(this);
                        result.append("Accuracy: " + "\n");
                        result.append("ACC: " + Float.toString(Test_still/Test_Round) + " " + Float.toString(Test_move/Test_Round) + " " + Float.toString(Test_rasputin/Test_Round) + "\n");
                        result.append("WiFi: " + Float.toString(Test_cell1/Test_Round) + " " + Float.toString(Test_cell2/Test_Round) + " " + Float.toString(Test_cell3/Test_Round) + " " + Float.toString(Test_cell4/Test_Round) + "\n");

                        test_count = 0; Test_still = 0; Test_move = 0; Test_rasputin = 0;
                        Test_cell4 = 0; Test_cell3 = 0; Test_cell2 = 0; Test_cell1 = 0;
                        return;
                    }
                }
            }
            index = index + 1;
        }
    }

    public void onAccuracyChanged(Sensor sensor,int accuracy)
    {
        return;
    }

    //添加数据到文件夹中

    public void addToFile(String raw_data, File RootFile, String filename, Boolean flag){
        if (!RootFile.exists()) {
            if(!RootFile.mkdirs()) {
                Log.i(TAG, RootFile.toString());
            }
        }
//        filename = cell + "_" + movement + "_" + "CollectedData.txt";
        File myFile = new File(RootFile, filename);
        try {
            myFile.createNewFile();
            Log.i(TAG,myFile + " added");
            FileWriter fw = new FileWriter(myFile,flag);
            fw.write( raw_data + "\n");
            fw.close();
        }
        catch (IOException ioe) {
            Log.i(TAG,"ERROR ADDING");
            Log.i(TAG,ioe.toString());
        }
    }

    public void extract_feature()
    {
        FileReader reader;
        BufferedReader inReader;
        File[] files = RawRootFile.listFiles();
        String str;
        float result_x = 0;
        float result_y = 0;
        float result_z = 0;

        float p1 = 0;
        float p2 = 0;

        String content;//, content_z1 = "", content_z2, content_z3;
        String[] content_z1 = new String[3];
        String[] content_p = new String[2];

        int count = 0, num = 1;
        for (File file : files)
        {
            if (file.exists())
                try{
                    num = 1;
                    double[] valuex = new double[WINDOWS_SIZE];
                    double[] valuey = new double[WINDOWS_SIZE];
                    double[] valuez = new double[WINDOWS_SIZE];
                    double[] power  = new double[2];
                    Log.i(TAG,file.getName()+"found");
                    File target= new File(FeatureRootFile, file.getName());
                    if (target.length() != 0)
                    {
                        target.delete();
                        target.createNewFile();
                        Log.i(TAG,target + " cleared");
                    }
                    reader = new FileReader(file);    // open the file
                    inReader = new BufferedReader(reader);
                    while ((str = inReader.readLine()) != null) {
                        String[] s = str.split(", ");
                        if (Float.parseFloat(s[0]) > 80.0)
                        {
                            result_x = Float.parseFloat(s[1]);
                            result_y = Float.parseFloat(s[2]);
                            result_z = Float.parseFloat(s[3]);
                            p1 = Float.parseFloat(s[5]);
                            p2 = Float.parseFloat(s[7]);

                            if (count < WINDOWS_SIZE)
                            {
                                valuex[count] = result_x;
                                valuey[count] = result_y;
                                valuez[count] = result_z;
                                power[0] += Math.pow(10,0.2*p1);
                                power[1] += Math.pow(10,0.2*p2);
                                count += 1;
                            }
                            else
                            {
                                result_x = result_x/40;
//                            biz.source_code.dsp.math.Complex[] a = Dft.goertzelSpectrum(data);
                                Complex[] acc_x = Dft.goertzelSpectrum(valuex);
                                Complex[] acc_y = Dft.goertzelSpectrum(valuey);
                                Complex[] acc_z = Dft.goertzelSpectrum(valuez);
//                            double max_x = 0, max_y = 0, max_z = 0;
//                            int maxIndex_x = 0, maxIndex_y = 0, maxIndex_z = 0;
//                            for (int i = 0; i < acc_x.length; i++) {
//
//                                if (acc_x[i].abs() > max_x) {
//                                    max_x = acc_x[i].abs();
//                                    maxIndex_x = i;
//                                }
//                                if (acc_y[i].abs() > max_y) {
//                                    max_y = acc_y[i].abs();
//                                    maxIndex_y = i;
//                                }
//                                if (acc_z[i].abs() > max_z) {
//                                    max_z = acc_z[i].abs();
//                                    maxIndex_z = i;
//                                }
//                            }
                                double maxVal = 0; double largest_maxVal = 100; int chosen = 0;
                                for (int k = 0; k < 3; k++) {
                                    for (int i = 0; i < acc_x.length; i++) {
                                        if ((acc_z[i].abs() > maxVal) && (acc_z[i].abs() < largest_maxVal)) {
                                            maxVal = acc_z[i].abs();
                                            chosen = i;
                                        }
                                    }
                                    content_z1[k] = Double.toString(maxVal) + ", " + Integer.toString(chosen);
                                    largest_maxVal = maxVal;
                                    maxVal = 0;
                                }
//                            content_x = Double.toString(max_x) + ", " + Double.toString(maxIndex_x) + ", " + Double.toString(maxIndex_x*(0.5/0.08 / (acc_x.length - 1)));
//                            content_y = Double.toString(max_y) + ", " + Double.toString(maxIndex_y) + ", " + Double.toString(maxIndex_y*(0.5/0.08 / (acc_x.length - 1)));
//                            content_z = Double.toString(max_z) + ", " + Double.toString(maxIndex_z) + ", " + Double.toString(maxIndex_z*(0.5/0.08 / (acc_x.length - 1)));
//                            content_x = Double.toString(max_x) + ", " + Double.toString(maxIndex_x);
//                            content_y = Double.toString(max_y) + ", " + Double.toString(maxIndex_y);
//                            content_z = Double.toString(max_z) + ", " + Double.toString(maxIndex_z);
//                            content_z1 = Double.toString(max_x) + ", " + Double.toString(maxIndex_x);
//                            content_z2 = Double.toString(max_y) + ", " + Double.toString(maxIndex_y);
//                            content_z3 = Double.toString(max_z) + ", " + Double.toString(maxIndex_z);
                                content_p[0] = Double.toString(5*Math.log10(power[0])); content_p[1] = Double.toString(5*Math.log10(power[1]));
                                content = Float.toString(num) + ", " + content_z1[0] + ", " + content_z1[1] + ", " + content_z1[2] + ", " + content_p[0] + ", " + content_p[1];
                                addToFile(content, FeatureRootFile, file.getName(), true);
//                            wifi_feature.setText(Float.toString(result));
                                wifi_feature.setText(content_z1[0] + ", " + content_z1[1] + ", " + content_z1[2]);
                                acc_feature.setText(content_p[0] + ", " + content_p[1]);
                                count = 0; num ++; //result = 0;
                            }
                        }

                    }
                }
                catch (IOException e) {
                Log.i(TAG, e.toString());
                }
        }

    }

    public void check()
    {
//        Complex[] acc_x = Dft.goertzelSpectrum(test_data.testing_acc_x);
//        Complex[] acc_y = Dft.goertzelSpectrum(test_data.testing_acc_y);
        Complex[] acc_z = Dft.goertzelSpectrum(test_data.testing_acc_z);

        double maxVal = 0; double largest_maxVal = 100; int chosen = 0;
        double[] content1 = new double[3];
        int[] content2 = new int[3];
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < acc_z.length; i++) {
                if ((acc_z[i].abs() > maxVal) && (acc_z[i].abs() < largest_maxVal)) {
                    maxVal = acc_z[i].abs();
                    chosen = i;
                }
            }
            content1[k] = maxVal;
            content2[k] = chosen;
            largest_maxVal = maxVal;
            maxVal = 0;
        }
        test_data.max_z1 = content1[0]; test_data.maxIndex_z1 = content2[0];
        test_data.max_z2 = content1[1]; test_data.maxIndex_z2 = content2[1];
        test_data.max_z3 = content1[2]; test_data.maxIndex_z3 = content2[2];
//        result.append("3" + Double.toString(test_data.p1) + ", " + Double.toString(test_data.p2) + "\n");

        test_data.p1 = 5*Math.log10(test_data.p1);
        test_data.p2 = 5*Math.log10(test_data.p2);


        result.append("Testing data's feature: " + Double.toString(content1[0]) + ", " + Double.toString(content1[1]) + ", " + Double.toString(content1[2]) + ", " + Double.toString(test_data.p1) + ", " + Double.toString(test_data.p2) + "\n");
        Log.i(TAG, "for DFT over");

        // read the file and store all the calculation
        read_feature();
        // test by a KNN
        KNN_action();
        KNN_room();
        // compare the two
        if (still_value > move_value && still_value > rasputin_value) {
            result.append("still: " + Integer.toString(still_value) + " " + Integer.toString(move_value) + " " + Integer.toString(rasputin_value) + "\n");
            Log.i(TAG, "still");
            Test_still ++;
        }
        else if(move_value > still_value && move_value > rasputin_value) {
            result.append("move: " + Integer.toString(still_value) + " " + Integer.toString(move_value) + " " + Integer.toString(rasputin_value) + "\n");
            Log.i(TAG, "move");
            Test_move ++;
        }
        else if(rasputin_value > move_value && rasputin_value > still_value)
        {
            result.append("rasputin: " + Integer.toString(still_value) + " " + Integer.toString(move_value) + " " + Integer.toString(rasputin_value) + "\n");
            Log.i(TAG, "RR");
            Test_rasputin ++;
        }
        else {
            result.append("no classification");
            Log.i(TAG, "no");
            Test_move ++;
        }

        if (Cell1_Value > Cell2_Value && Cell1_Value > Cell3_Value && Cell1_Value > Cell4_Value) {
            result.append("cell1: " + Integer.toString(Cell1_Value) + " " + Integer.toString(Cell2_Value) + " " + Integer.toString(Cell3_Value) + " " + Integer.toString(Cell4_Value) + "\n");
            Log.i(TAG, "cell1");
            Test_cell1 ++;
        }
        else if(Cell2_Value > Cell1_Value && Cell2_Value > Cell3_Value && Cell2_Value > Cell4_Value) {
            result.append("cell2: " + Integer.toString(Cell1_Value) + " " + Integer.toString(Cell2_Value) + " " + Integer.toString(Cell3_Value) + " " + Integer.toString(Cell4_Value) + "\n");
            Log.i(TAG, "cell2");
            Test_cell2 ++;
        }
        else if(Cell3_Value > Cell2_Value && Cell3_Value > Cell1_Value && Cell3_Value > Cell4_Value)
        {
            result.append("cell3: " + Integer.toString(Cell1_Value) + " " + Integer.toString(Cell2_Value) + " " + Integer.toString(Cell3_Value) + " " + Integer.toString(Cell4_Value) + "\n");
            Log.i(TAG, "cell3");
            Test_cell3 ++;
        }
        else if(Cell4_Value > Cell2_Value && Cell4_Value > Cell1_Value && Cell3_Value < Cell4_Value)
        {
            result.append("cell4: " + Integer.toString(Cell1_Value) + " " + Integer.toString(Cell2_Value) + " " + Integer.toString(Cell3_Value) + " " + Integer.toString(Cell4_Value) + "\n");
            Log.i(TAG, "cell4");
            Test_cell4 ++;
        }
        else {
            result.append("no classification");
            Log.i(TAG, "no");
            Test_move ++;
        }


        // reset data
        distanceMap.clear();
        distanceMap_cell.clear();
        roomMap.clear();
        numberMap.clear();
        actionMap.clear();
        Cell1_Value = 0; Cell2_Value = 0; Cell3_Value = 0; Cell4_Value = 0; still_value = 0; move_value = 0; rasputin_value = 0;
    }

    // 读取收集的数据并且计算距离
    public void read_feature()
    {
        FileReader reader;
        BufferedReader inReader;
        File[] files = FeatureRootFile.listFiles();
        double distance = 0;
        double distance_cell = 0;
        int instanceCounter = 1;
        for (File file : files)
        {
            if (file.exists())
            {
                Log.i(TAG, "for read feature file found");
                try {
                    reader = new FileReader(file);    // open the file
                    inReader = new BufferedReader(reader);
                    String str;
                    while ((str = inReader.readLine()) != null) {
                        String[] s = str.split(", ");
                        // calculate the distance
//                        distance = Math.sqrt(Math.pow((Double.parseDouble((s[5])) - RSSI), 2));
                        distance = Math.sqrt(Math.pow((Double.parseDouble((s[1])) - test_data.max_z1), 2)
                                + Math.pow((Double.parseDouble((s[3])) - test_data.max_z2), 2)
                                + Math.pow((Double.parseDouble((s[5])) - test_data.max_z3), 2));
//                                + Math.pow((Double.parseDouble((s[2])) - test_data.maxIndex_z1), 2)
//                                + Math.pow((Double.parseDouble((s[4])) - test_data.maxIndex_z2), 2)
//                                + Math.pow((Double.parseDouble((s[6])) - test_data.maxIndex_z3), 2));
                        distance_cell = Math.sqrt(Math.pow((Double.parseDouble((s[7])) - test_data.p1), 2)
                                + Math.pow((Double.parseDouble((s[8])) - test_data.p2), 2));
                        // store the distance

                        if (!distanceMap.containsKey(instanceCounter)) {
                            distanceMap.put(instanceCounter, distance);
                            distanceMap_cell.put(instanceCounter, distance_cell);
                            roomMap.put(instanceCounter, Integer.parseInt(file.getName().split("_")[0]));
                            numberMap.put(instanceCounter, 1);
                            actionMap.put(instanceCounter, file.getName().split("_")[1]);
                        } else {
                            distance = distance + distanceMap.get(instanceCounter);
                            distance_cell = distance_cell + distanceMap.get(instanceCounter);
                            distanceMap_cell.put(instanceCounter, distance_cell);
                            distanceMap.put(instanceCounter, distance);
                            numberMap.put(instanceCounter, numberMap.get(instanceCounter) + 1);
                        }
//                        result.append(Double.toString(distance) + actionMap.get(instanceCounter) + "\n");
//                        result.append("room" + roomMap.get(instanceCounter) + "\n ");
//                        result.append("dist" + distanceMap_cell.get(instanceCounter) + "\n ");
                        instanceCounter++;
                    }
                }
                catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
                Log.i(TAG, "for read feature, over");
            }
        }
//        for (int cell = 1; cell <= cellNo; cell++) {
//            File RSSI_data = new File(RawRootFile, cell + "CollectedData.txt");
//            if (RSSI_data.exists()) {
//                Log.i(TAG, "file found");
//                try {
//                    reader = new FileReader(RSSI_data);    // open the file
//                    inReader = new BufferedReader(reader);
//                    String str;
//                    int offset = 0;
//                    while ((str = inReader.readLine()) != null) {
//                        String[] s = str.split(", ");
//                        offset = offset + 1;
//                        // calculate the distance
//                        distance = Math.sqrt(Math.pow((Double.parseDouble((s[5])) - RSSI), 2));
//                        // store the distance
//                        if (!distanceMap.containsKey(instanceCounter)) {
//                            distanceMap.put(instanceCounter, distance);
//                            roomMap.put(instanceCounter, cell);
//                            numberMap.put(instanceCounter, 1);
//                        } else {
//                            distance = distance + distanceMap.get(instanceCounter);
//                            distanceMap.put(instanceCounter, distance);
//                            numberMap.put(instanceCounter, numberMap.get(instanceCounter) + 1);
//                        }
//                        instanceCounter++;
//                    }
//
//                } catch (IOException e) {
//                    Log.i(TAG, e.toString());
//                }
//            }
//        }
//        data_reader.setText(Float.toString(instanceCounter) + '\n');
    }

    public void KNN_room()
    {
    int chosenOne = 0;
    Cell1_Value = 0; Cell2_Value = 0; Cell3_Value = 0; Cell4_Value = 0;
    double minVal = 99999;;
    double lowest_minVal = 0;
    for (int k = 0; k < 5; k++) {
        for (int j : distanceMap_cell.keySet()) {
            if ((distanceMap_cell.get(j) < minVal) && (distanceMap_cell.get(j) > lowest_minVal)) {
                minVal = distanceMap_cell.get(j);
                chosenOne = j;
            }
        }
//        result.append("chosenone" + roomMap.get(chosenOne) + "\n ");
        if (roomMap.get(chosenOne) == 1) {
            Cell1_Value++;
        } else if (roomMap.get(chosenOne) == 2) {
            Cell2_Value++;
        } else if (roomMap.get(chosenOne) == 3) {
            Cell3_Value++;
        } else if (roomMap.get(chosenOne) == 4) {
            Cell4_Value++;
        }
        lowest_minVal = minVal;
        minVal = 99999;
        }
    }
    public void KNN_action()
    {
        int chosenOne = 0;
        still_value = 0;
        move_value = 0;
        rasputin_value = 0;
        String move = "move";
        String still = "still";
        String rasputin = "Rasputin";

        double minVal = 99999;;
        double lowest_minVal = 0;
        for (int k = 0; k < 10; k++) {
            for (int j : distanceMap.keySet()) {
                if ((distanceMap.get(j) < minVal) && (distanceMap.get(j) > lowest_minVal)) {
                    minVal = distanceMap.get(j);
                    chosenOne = j;
                }
            }
//            result.append(move + " " + still + " " + rasputin + " " + actionMap.get(chosenOne) + "\n");
            if (actionMap.get(chosenOne).equals(still)) {
                still_value++;
            } else if (actionMap.get(chosenOne).equals(move)) {
                move_value++;
            }
            else
            {
                rasputin_value++;
            }
//            result.append(Integer.toString(still_value) + " " + Integer.toString(move_value) + " " + Integer.toString(rasputin_value) + "\n");
            lowest_minVal = minVal;
            minVal = 99999;
        }
        Log.i(TAG, "for KNN action");
    }
    public void onClick(View v)
    {
        // 开始收集
        if(v.getId()==R.id.bt_start)
        {
            states = true;
            wifiInfo = wifiManager.getConnectionInfo();
            mSensorMgr.unregisterListener(this,mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorMgr.registerListener(this,
                    mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
            return;
        }
        // 停止
        if(v.getId()==R.id.bt_stop)
        {
            mSensorMgr.unregisterListener(this);

            training_overview.setText(Integer.toString(index) + " data stored for " + cellNo.getText().toString() + ", " + movementNo.getText().toString());
            index = 0;
            return;
        }
        if(v.getId()==R.id.bt_feature)
        {
            extract_feature();
        }
        if(v.getId() == R.id.bt_testing)
        {
            states = false;
            result.setText("");
            // get the current wifi RSSI
            wifiInfo = wifiManager.getConnectionInfo();
            mSensorMgr.unregisterListener(this,mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorMgr.registerListener(this,
                    mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);

            return;
        }
    }
}