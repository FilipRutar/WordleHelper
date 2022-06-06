package si.uni_lj.fe.tnuv.wordle;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class popupSettings {

    private Context context;
//save the context received via constructor in a local variable

    public popupSettings(Context context){
        this.context=context;
    }


    public void showPopupWindow(final View view) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_settings, null);


        //ustvarimo popup, focusable true pomeni da se zapre ce kliknemo mimo okna
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        //glede na trenutno sharanjno tezavnost nastavimo kateri gumb je oznacen
        final RadioGroup group = (RadioGroup) popupView.findViewById(R.id.difficultyToggle);
        SharedPreferences pref = context.getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if (pref.getBoolean("diff", true)) {
            ((RadioButton) group.getChildAt(1)).setChecked(true);
        }else{
            ((RadioButton) group.getChildAt(0)).setChecked(true);
        }

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
        @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = group.getCheckedRadioButtonId();
                //glede na to kateri gumb oznacimo shranimo to v shared pref, true je hard
                switch (id) {
                    case R.id.easy:
                        editor.putBoolean("diff", false);
                        editor.apply();
                        break;
                    case R.id.hard:
                        editor.putBoolean("diff", true);
                        editor.apply();
                        break;
                }
            }
        });

        //gumb za zapiranje popupa
        ImageButton popupButton = popupView.findViewById(R.id.closeSettingsPopup);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //As an example, display the message
                popupWindow.dismiss();
            }
        });

    }



}
