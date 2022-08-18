package iot.yom.finalproject.part2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import iot.yom.finalproject.R;

public class SaveDialog extends DialogFragment  {
    private EditText file_name_text;
    private Button save_button;

    public interface DialogSending{
        void setFileName_and_Save(String date);
    }
    RecordFragment pFragment;

    public void setRecordFragment(RecordFragment dpdfe){
        this.pFragment = dpdfe;
    }
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.save_dialog, container, false);
        save_button = view.findViewById(R.id.save_button_dialog);
        file_name_text = view.findViewById(R.id.file_name);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String file_name = file_name_text.getText().toString();
                pFragment.setFileName_and_Save(file_name);
                getDialog().dismiss();
            }

        });
        return view;
    }

}
