package iot.yom.finalproject.part2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import iot.yom.finalproject.MainPart2;
import iot.yom.finalproject.R;


public class RecordFragment extends Fragment implements ServiceConnection, SerialListener, SaveDialog.DialogSending {


    private TextView tv_timer;
    private Button btn_record;
    private Button btn_stop;
    private Button btn_save;
    private Button btn_reset;
    private Button btn_report;
    private String device_address;
    private SerialService service;
    private boolean initialStart = true;
    private String newline = TextUtil.newline_crlf;
    private String main_path = "/storage/self/primary/Terminal/";
    private CSVWriter csvWriter;
    private String file_name;
    private boolean recording = false;
    private int seconds = 0;


    public void setFileName_and_Save(String name) {
        file_name = name;
        File csv_file = new File(main_path + "last_recording.csv");
        File rename = new File(main_path + file_name + ".csv");
        boolean succ = csv_file.renameTo(rename);
        if (succ) {
            Toast.makeText(getActivity(), "Recording " + file_name + " Saved", Toast.LENGTH_SHORT).show();
            seconds = 0;
        }
    }

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // set views
        tv_timer = view.findViewById(R.id.tv_timer);
        btn_record = view.findViewById(R.id.btn_record);
        btn_stop = view.findViewById(R.id.btn_stop);
        btn_save = view.findViewById(R.id.btn_save);
        btn_reset = view.findViewById(R.id.btn_reset);
        btn_report = view.findViewById(R.id.btn_goToReport);

        // buttons listeners
        ;

        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String device_selected = ((MainPart2) getActivity()).device_address;
                if (device_selected == null) {
                    Toast.makeText(getActivity(), "No Device Selected", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!device_selected.equals(device_address)) {
                    device_address = device_selected;
                }
                try {
                    csvWriter = new CSVWriter(new FileWriter(main_path + "tmp.csv", true));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connect();
                    }
                });
                recording = true;
                Toast.makeText(getActivity(), "Recording Starting", Toast.LENGTH_SHORT).show();
            }

        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
                recording = false;
                csvWriter = null;

                File csv_file = new File(main_path + "tmp.csv");
                File rename = new File(main_path + "last_recording.csv");
                boolean succ = csv_file.renameTo(rename);
                if (succ)
                    Toast.makeText(getActivity(), "Recording Stopping", Toast.LENGTH_SHORT).show();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveDialog dialog = new SaveDialog();
                dialog.setRecordFragment(RecordFragment.this);
                dialog.show(getParentFragmentManager(), "Dialog");
            }
        });
        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seconds = 0;
                File tmp = new File(main_path + "tmp.csv");
                if (tmp.exists())
                    if (tmp.delete()) {
                        Toast.makeText(getActivity(), "In session recording deleted", Toast.LENGTH_SHORT).show();
                        try {
                            csvWriter = new CSVWriter(new FileWriter(main_path + "tmp.csv", true));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                File to_be_named = new File(main_path + "last_recording.csv");
                if (to_be_named.exists())
                    if (to_be_named.delete()) {
                        Toast.makeText(getActivity(), "Unsaved recording deleted", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        runTimer();

        return view;
    }

    private void runTimer() {

        final Handler handler
                = new Handler();

        handler.post(new Runnable() {
            @Override

            public void run() {
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time
                        = String
                        .format(Locale.getDefault(),
                                "%02d:%02d",
                                minutes, secs);

                tv_timer.setText(time);


                if (recording) {
                    seconds++;
                }

                handler.postDelayed(this, 1000);
            }
        });
    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(device_address);
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        service.disconnect();
    }

    private void receive(byte[] message) {
        String msg = new String(message);
        if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0 && recording) {
            String msg_to_save = msg;
            msg_to_save = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
            if (msg_to_save.length() > 1) {
                String[] parts = msg_to_save.split(",");
                parts = clean_str(parts);
                String row[] = new String[]{parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9]};
                csvWriter.writeNext(row);
                try {
                    csvWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    public void onSerialConnect() {
    }

    @Override
    public void onSerialConnectError(Exception e) {
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        disconnect();
    }

    private String[] clean_str(String[] stringsArr) {
        for (int i = 0; i < stringsArr.length; i++) {
            stringsArr[i] = stringsArr[i].replaceAll(" ", "");
        }


        return stringsArr;
    }
}