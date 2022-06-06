package si.uni_lj.fe.tnuv.wordle;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    char[][] letterArray = new char [2][6];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //za hec nastavimo nakljucne barve naslovnih kvadratkov :)
        for (int i = 0; i < 2; i++){
            for (int j = 0; j < 6; j++){
                int random = new Random().nextInt(4);
                String letterID = "Letter" + i + j;
                TextView letterBox = findViewById(getResources().getIdentifier(letterID, "id", getPackageName()));
                switch(random){
                    case 0:
                        letterBox.setBackground(getDrawable(R.drawable.box_empty));
                        break;
                    case 1:
                        letterBox.setBackground(getDrawable(R.drawable.box_grey));
                        letterBox.setTextColor(Color.WHITE);
                        letterArray[i][j] = 's';
                        break;
                    case 2:
                        letterBox.setBackground(getDrawable(R.drawable.box_yellow));
                        letterBox.setTextColor(Color.WHITE);
                        letterArray[i][j] = 'r';
                        break;
                    case 3:
                    default:
                        letterBox.setBackground(getDrawable(R.drawable.box_green));
                        letterBox.setTextColor(Color.WHITE);
                        letterArray[i][j] = 'z';
                        break;
                }
            }

        }
    }

    public void StartEng(View view) {
        Intent intent = new Intent(this, EngMain.class);
        startActivity(intent);
    }

    public void StartSlo(View view) {
        Intent intent = new Intent(this, SloMain.class);
        startActivity(intent);
    }

    public void openSettings(View view) {
        popupSettings settingsPopup = new popupSettings(this);
        settingsPopup.showPopupWindow(view);
    }

    public void openHelp(View view) {
        popupHelp helpPopup = new popupHelp();
        helpPopup.showPopupWindow(view);
    }

    public void titleSelectColor(View view) {
        TextView letterBox = (TextView)view;
        String letterBoxPosition = getResources().getResourceEntryName(letterBox.getId());
        int vrstica = Integer.parseInt(letterBoxPosition.substring(6, 7));
        int stolpec = Integer.parseInt(letterBoxPosition.substring(7, 8));
        switch(letterArray[vrstica][stolpec]){ //[s]iva, [r]umena, [z]elena
            case 's':
                letterBox.setBackground(getDrawable(R.drawable.box_yellow));
                letterBox.setTextColor(Color.WHITE);
                letterArray[vrstica][stolpec] = 'r';
                break;
            case 'r':
                letterBox.setBackground(getDrawable(R.drawable.box_green));
                letterBox.setTextColor(Color.WHITE);
                letterArray[vrstica][stolpec] = 'z';
                break;
            case 'z':
            default:
                letterBox.setBackground(getDrawable(R.drawable.box_grey));
                letterBox.setTextColor(Color.WHITE);
                letterArray[vrstica][stolpec] = 's';
        }
    }




}