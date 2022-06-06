package si.uni_lj.fe.tnuv.wordle;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class popupHelp {


    public void showPopupWindow(final View view) {

        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_help, null);


        //ustvarimo popup, focusable true pomeni da se zapre ce kliknemo mimo okna
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);


        //glede na trenutno sharanjno tezavnost nastavimo kateri gumb je oznacen


        //gumb za zapiranje popupa
        ImageButton popupButton = popupView.findViewById(R.id.closeHelpPopup);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //As an example, display the message
                popupWindow.dismiss();
            }
        });

    }
}
