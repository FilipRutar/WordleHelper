package si.uni_lj.fe.tnuv.wordle;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EngMain extends AppCompatActivity {
    //array za crke[0] in njihov status/barvo[1]
    char[][][] letterArray = new char [6][5][2];
    int activeRow = 0;
    int activeColumn = 0;
    DatabaseTable db = new DatabaseTable(this);
    static String LANG = "eng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eng_main);
        //da se db ustvari vnaprej in ne cakamo
        db.initiateDB();

    }

    public void engEnterLetter(View view) {
        //flow: vnesemo vse crke, pritisnemo enter (preveri ali je beseda veljavna),
        //pokaze se nova prazna vrstica in gumbi za razultate
        //ko vnesemo novo crko se gumb za rezultate spet skrije, ce zbrisemo vse crke se pokaze
        //ce zbrisemo prazno vrstico se gumb in vrstica skrijejo in vrnemo se v prejsnjo
        Button b = (Button)view;
        String btnText = b.getText().toString();

        if (btnText.equals("ENTER")){ //enter
            if (activeColumn == 5 && activeRow < 6 && validateRow()) {
                activeRow += 1;
                activeColumn = 0;

                //pokazi gumba
                Button getResults = findViewById(R.id.getResults);
                getResults.setEnabled(true);
                Button getGuesses = findViewById(R.id.getBestGuesses);
                getGuesses.setEnabled(true);
                if (activeRow < 6) {
                    //pokazi naslednjo vrstico samo ce se nismo v zadnji
                    String rowID = "GuessRow" + activeRow;
                    LinearLayout guessRow = findViewById(getResources().getIdentifier(rowID, "id", getPackageName()));
                    guessRow.setVisibility(View.VISIBLE);
                }
            }
        }else if (btnText.equals("\u232B")){ //backspace
            if (activeColumn > 0) {
                if (activeColumn == 1 && activeRow > 0){
                    //ce zbrisemo vse crke v vrstici se gumbi spet pokazejo
                    Button getResults = findViewById(R.id.getResults);
                    getResults.setEnabled(true);
                    Button getGuesses = findViewById(R.id.getBestGuesses);
                    getGuesses.setEnabled(true);
                }
                activeColumn -= 1;
                String btnID = "guess" + activeRow + activeColumn;
                TextView letterBox = findViewById(getResources().getIdentifier(btnID, "id", getPackageName()));
                letterBox.setText("");
                letterBox.setBackground(getDrawable(R.drawable.box_empty));
                letterBox.setTextColor(getResources().getColor(R.color.txtColor));

                letterArray[activeRow][activeColumn][0] = '\0';
                letterArray[activeRow][activeColumn][1] = '\0';
            } else if(activeColumn == 0 && activeRow > 0) {
                //vrnemo se v prejsjo vrstico, skirjemo trenutno vrstico in onemogocimo gumbe
                Button getResults = findViewById(R.id.getResults);
                getResults.setEnabled(false);
                Button getGuesses = findViewById(R.id.getBestGuesses);
                getGuesses.setEnabled(false);
                if (activeRow < 6) {
                    //skrij trenutno vrstico
                    String rowID = "GuessRow" + activeRow;
                    LinearLayout guessRow = findViewById(getResources().getIdentifier(rowID, "id", getPackageName()));
                    guessRow.setVisibility(View.INVISIBLE);
                }
                //postavi pozicijo na konec prejsnje vrstice
                activeColumn = 5;
                activeRow -= 1;
            }
        }else{ //ostale crke
            if (activeColumn < 5 && activeRow < 6) {
                if (activeColumn == 0 && activeRow > 0){
                    //ko vnesemo crko in dokler ne potrdimo vrstice/besede je gumb skrit
                    Button getResults = findViewById(R.id.getResults);
                    getResults.setEnabled(false);
                    Button getGuesses = findViewById(R.id.getBestGuesses);
                    getGuesses.setEnabled(false);
                }
                //prikazemo crko in jo shranimo
                String btnID = "guess" + activeRow + activeColumn;
                TextView letterBox = findViewById(getResources().getIdentifier(btnID, "id", getPackageName()));
                letterBox.setText(btnText);
                letterArray[activeRow][activeColumn][0] = btnText.charAt(0);
                activeColumn += 1;
            }
        }

        Log.d("current row/column", String.valueOf(activeRow) + activeColumn);
    }

    public void engSelectColor(View view) {
        TextView letterBox = (TextView)view;
        String letterBoxPosition = getResources().getResourceEntryName(letterBox.getId());
        int vrstica = Integer.parseInt(letterBoxPosition.substring(5, 6));
        int stolpec = Integer.parseInt(letterBoxPosition.substring(6, 7));
        if (letterArray[vrstica][stolpec][0] != '\0') {
            //krozimo po barvah in vpisemo trenutno v letterArray
            switch(letterArray[vrstica][stolpec][1]){ //[s]iva, [r]umena, [z]elena
                case 's':
                    letterBox.setBackground(getDrawable(R.drawable.box_yellow));
                    letterBox.setTextColor(Color.WHITE);
                    letterArray[vrstica][stolpec][1] = 'r';
                    break;
                case 'r':
                    letterBox.setBackground(getDrawable(R.drawable.box_green));
                    letterBox.setTextColor(Color.WHITE);
                    letterArray[vrstica][stolpec][1] = 'z';
                    break;
                case 'z':
                default:
                    letterBox.setBackground(getDrawable(R.drawable.box_grey));
                    letterBox.setTextColor(Color.WHITE);
                    letterArray[vrstica][stolpec][1] = 's';
            }
        }
    }


    private boolean checkAllLetterColor(){
        //preveri ali so vsi kvadratki pobarvani
        for (int row = 0; row < activeRow; row++){
            for (int col = 0; col < 5; col++){
                if (letterArray[row][col][1] == '\0'){
                    Log.d("warn", "kvadratek ni pobarvan");
                    Toast.makeText(this, "Color all letters", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }

    public void getResults(View view){
        // gumb je omogocen samo ko smo prejsnjo vrstico potrdili z enter
        // in se nismo zaceli pisati naslednje vrstice
        //preverimo ali so vsi kvadratki pobarvani
        if (checkAllLetterColor()){
            //preberemo shranjen difficulty, true=hard
            SharedPreferences pref = this.getSharedPreferences("MyPref", MODE_PRIVATE);
            boolean diff = pref.getBoolean("diff", true);
            Log.d("difficulty", "diff je " + diff);

            Cursor c = db.filterResults(letterArray, activeRow, diff, LANG);
            //process Cursor and display results
            TextView resultsView = findViewById(R.id.resultsView);
            resultsView.setText("");
            if (c != null)  {
                //preberemo prvo besedo, za vsako naslednjo najprej dodamo presledek
                c.moveToPosition(0);
                String str = c.getString(0);
                resultsView.append(str.toUpperCase());
                while(c.moveToNext()) {
                    str = c.getString(0);
                    resultsView.append(" " + str.toUpperCase());
                }
                c.close();

            }else{
                Log.d("warn", "cursor je prazen");
                resultsView.setText(R.string.warningNoResults);
            }
        }

    }


    public void getBestGuesses(View view){
        // gumb je omogocen samo ko smo prejsnjo vrstico potrdili z enter
        // in se nismo zaceli pisati naslednje vrstice
        // preverimo ali so vsi kvadratki pobarvani

        if (checkAllLetterColor()){

            Cursor c = db.filterGuesses(letterArray, activeRow, LANG);
            //process Cursor and display results
            TextView resultsView = findViewById(R.id.resultsView);
            resultsView.setText("");
            if (c != null)  {
                //preberemo prvo besedo, za vsako naslednjo najprej dodamo presledek
                c.moveToPosition(0);
                String str = c.getString(0);
                resultsView.append(str.toUpperCase());
                while(c.moveToNext()) {
                    str = c.getString(0);
                    resultsView.append(" " + str.toUpperCase());
                }
                c.close();

            }else{
                Log.i("warn", "cursor je prazen");
                resultsView.setText(R.string.warningNoGuess);
            }
        }

    }

    public boolean validateRow() {
        //prebere crke iz trenutne vrstice in preveri ali je beseda veljavna
        String word = "";
        for(int i = 0; i<5; i++) {
            word = word + letterArray[activeRow][i][0];
        }

        if(db.validateWord(word, LANG)){
            Log.d("buttonid", "word valid: " + word);
            return true;
        }
        Log.d("buttonid", "invalid word: " + word);
        Toast.makeText(this, "Invalid word!", Toast.LENGTH_SHORT).show();

        return false;
    }

    public void openSettings(View view) {
        popupSettings settingsPopup = new popupSettings(this);
        settingsPopup.showPopupWindow(view);
    }

    public void openHelp(View view) {
        popupHelp helpPopup = new popupHelp();
        helpPopup.showPopupWindow(view);
    }
}

