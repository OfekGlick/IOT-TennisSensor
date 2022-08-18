package iot.yom.finalproject.part2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;

import iot.yom.finalproject.R;

public class BluetoothDialog extends DialogFragment {

    // Views
    private ListView lv_devices_list;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private final ArrayList<BluetoothDevice> listItems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;

    public interface OnInputListener {
        void sendInput(String input);
    }
    public OnInputListener mOnInputListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.bluetooth_dialog, container, false);

        // Views
        lv_devices_list = view.findViewById(R.id.lv_devicesList);

        // Initialize bluetooth adapter
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize list adapter
        listAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), 0, listItems) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                BluetoothDevice device = listItems.get(position);
                if (view == null)
                    view = inflater.inflate(R.layout.device_list_item, parent, false);
                TextView tv_device_name = view.findViewById(R.id.tv_deviceName);
                tv_device_name.setText(device.getName());
                return view;
            }
        };

        lv_devices_list.setAdapter(listAdapter);
        lv_devices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = listItems.get(i);
                mOnInputListener.sendInput(device.getAddress());
                getDialog().dismiss();
            }
        });

        return view;

    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            mOnInputListener
                    = (OnInputListener)getContext();
        }
        catch (ClassCastException e) {
            System.out.println("Could not attach");
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    void refresh() {
        listItems.clear();
        if (bluetoothAdapter != null) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices())
                if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE)
                    listItems.add(device);
        }
        Collections.sort(listItems, BluetoothDialog::compareTo);
        listAdapter.notifyDataSetChanged();
    }

    static int compareTo(BluetoothDevice a, BluetoothDevice b) {
        boolean aValid = a.getName() != null && !a.getName().isEmpty();
        boolean bValid = b.getName() != null && !b.getName().isEmpty();
        if (aValid && bValid) {
            int ret = a.getName().compareTo(b.getName());
            if (ret != 0) return ret;
            return a.getAddress().compareTo(b.getAddress());
        }
        if (aValid) return -1;
        if (bValid) return +1;
        return a.getAddress().compareTo(b.getAddress());
    }
}
