package si.uni_lj.fe.tnuv.wordle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//https://developer.android.com/training/search/search#java
public class DatabaseTable {
    private static final String TAG = "WordsDB";

    //The columns we'll include in the dictionary table
    public static final String COL_WORD = "WORD";

    private static final String DATABASE_NAME = "Words";
    private static final String ENG_FULL = "ENG_FULL";
    private static final String ENG_PARTIAL = "ENG_PARTIAL";
    private static final String SLO_FULL = "SLO_FULL";
    private static final String SLO_PARTIAL = "SLO_PARTIAL";

    private static final String[] TABLE_NAMES = {
            ENG_FULL,
            ENG_PARTIAL,
            SLO_FULL,
            SLO_PARTIAL
    };

    private static final String[] WORDS_SRC_LOCATION = {
            "engwords_full.txt",
            "engwords_partial.txt",
            "slowords_full.txt",
            "slowords_partial.txt"
    };


    private static final int DATABASE_VERSION = 1;

    private final DatabaseOpenHelper databaseOpenHelper;

    public DatabaseTable(Context context) {
        databaseOpenHelper = new DatabaseOpenHelper(context);
    }


    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context helperContext;
        private SQLiteDatabase mDatabase;

        //ustvarimo vse 4 tabele naenkrat v isti bazi
        private static final String[] CREATE_TABLE_STRINGS = {
                "CREATE VIRTUAL TABLE " + ENG_FULL +
                        " USING fts3 (" +
                        COL_WORD + ")",
                "CREATE VIRTUAL TABLE " + ENG_PARTIAL +
                        " USING fts3 (" +
                        COL_WORD + ")",
                "CREATE VIRTUAL TABLE " + SLO_FULL +
                        " USING fts3 (" +
                        COL_WORD + ")",
                "CREATE VIRTUAL TABLE " + SLO_PARTIAL +
                        " USING fts3 (" +
                        COL_WORD + ")"
        };


        public DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            helperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;

            for (int i = 0; i < 4; i++) {
                mDatabase.execSQL(CREATE_TABLE_STRINGS[i]);
            }
            loadWordList();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            for (int i = 0; i < 4; i++) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMES[i]);
            }
            onCreate(db);
        }

        public void loadWordList() {
            //thread da ne blokiramo aplikacije, ampak ne dela??
            //ce je znotraj threada ne prebere do konca datoteke??
            for (int i = 0; i < 4; i++){
                try {
                    loadWordsAll(i);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


        }

        private void loadWordsAll(int i) throws IOException {

            InputStream inputStream = helperContext.getAssets().open(WORDS_SRC_LOCATION[i]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    addWordAll(line, i);
                }
            } finally {
                Log.d("thread", "finished reading " + i);

                reader.close();
            }
        }

        public long addWordAll(String word, int i) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_WORD, word);

            return mDatabase.insert(TABLE_NAMES[i], null, initialValues);
        }


    }

    public Cursor filterResults(char[][][] letterArray, int activeRow, boolean diff, String lang) {
        // vhod char array vseh crk in njihove barva in trenutna vrstica
        // sestavimo string where (where) WORD LIKE ? AND NOT LIKE ? ...
        // pogoji so v arrayu v formatu _x___ in jih query builder vstavi namesto ?
        // z for zanko gremo cez vse crke in ustvarimo ustrezen sql odsek
        StringBuilder selection = new StringBuilder("");
        List<String> argumentsList = new ArrayList<String>();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        //ce je difficulty true/hard izberemo vse besede, sicer samo mozne odgovore, tudi jezik

        if (diff && lang.matches("eng")) {
            builder.setTables(TABLE_NAMES[0]);
        }else if (!diff && lang.matches("eng")){
            builder.setTables(TABLE_NAMES[1]);
        } else if (diff && lang.matches("slo")){
            builder.setTables(TABLE_NAMES[2]);
        } else {
            builder.setTables(TABLE_NAMES[3]);
        }

        for (int row = 0; row < activeRow; row++){
            for (int col = 0; col < 5; col++){
                switch (letterArray[row][col][1]){
                    case 'z':
                        StringBuilder zelenaLikeString = new StringBuilder("_____");
                        zelenaLikeString.setCharAt(col, letterArray[row][col][0]);
                        selection.append(COL_WORD).append(" LIKE ? AND ");
                        argumentsList.add(String.valueOf(zelenaLikeString));
                        break;
                    case 'r':
                        String rumenaLike = "%" + letterArray[row][col][0] +"%";
                        StringBuilder rumenaNotLike = new StringBuilder("_____");
                        rumenaNotLike.setCharAt(col, letterArray[row][col][0]);
                        selection.append(COL_WORD).append(" LIKE ? AND ").append(COL_WORD).append(" NOT LIKE ? AND ");
                        //trenutno crko primerjamo z ostalimi crkami v vrstici
                        //za vsako isto ne sivo crko dodamo pogoj da je ena vec v besedi

                        for (int i = 0; i < 5; i++) {

                            if (letterArray[row][col][0] == letterArray[row][i][0] && letterArray[row][i][1] != 's' && i != col){
                                rumenaLike = rumenaLike + letterArray[row][col][0] +"%";

                            }
                        }
                        argumentsList.add(rumenaLike);
                        argumentsList.add(String.valueOf(rumenaNotLike));
                        break;
                    case 's':
                        // trenutno crko primerjamo z vsemi ostalimi v vrstici
                        // ce obstaja ista crka (ki ni siva) spremenimo iz not like %x%
                        // v not like __x__ (brisemo samo besede s crko na tem mestu)
                        boolean duplicateLetter = false;
                        for (int i = 0; i < 5; i++) {
                            // preverimo ali je ista crka (ne na istem mestu) in ni siva
                            if (letterArray[row][col][0] == letterArray[row][i][0] && letterArray[row][i][1] != 's' && i != col){
                                duplicateLetter = true;
                                break;
                            }
                        }

                        if (duplicateLetter) {
                            StringBuilder sivaNotLike = new StringBuilder("_____");
                            sivaNotLike.setCharAt(col, letterArray[row][col][0]);
                            selection.append(COL_WORD).append(" NOT LIKE ? AND ");
                            argumentsList.add(String.valueOf(sivaNotLike));
                        } else {
                            String sivaNotLike = "%" + letterArray[row][col][0] +"%";
                            selection.append(COL_WORD).append(" NOT LIKE ? AND ");
                            argumentsList.add(sivaNotLike);
                        }
                        break;
                }
            }
        }

        selection.delete(selection.length() - 5, selection.length()); //zbrisemo zadnji AND
        Log.d("sql string", String.valueOf(selection));
        Log.d("sql values", String.valueOf(argumentsList));
        String[] arrayString = argumentsList.toArray(new String[0]); //iz arraylist v array

        Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(),
                null, String.valueOf(selection), arrayString, null, null, COL_WORD + " ASC");

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;

    }


    public Cursor filterGuesses(char[][][] letterArray, int activeRow, String lang) {
        // vhod char array vseh crk in njihove barva in trenutna vrstica
        // sestavimo string where (where) WORD LIKE ? AND NOT LIKE ? ...
        // pogoji so v arrayu v formatu _x___ in jih query builder vstavi namesto ?
        // z for zanko gremo cez vse crke in ustvarimo ustrezen sql odsek
        StringBuilder selection = new StringBuilder("");
        List<String> argumentsList = new ArrayList<String>();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        if (lang.matches("eng")) {
            builder.setTables(TABLE_NAMES[0]);
        } else {
            builder.setTables(TABLE_NAMES[2]);
        }

        //sestavi sql query za vse crke
        //TODO ce ni zadetkov pametno sprosti zahteve (ne  upostevaj vseh pogojev)
        for (int row = 0; row < activeRow; row++){
            for (int col = 0; col < 5; col++){
                switch (letterArray[row][col][1]){
                    case 'z':
                        String zelenaNotLike = "%" + letterArray[row][col][0] +"%";
                        selection.append(COL_WORD).append(" NOT LIKE ? AND ");
                        argumentsList.add(zelenaNotLike);
                        break;
                    case 'r':
                        String rumenaNotLike = "%" + letterArray[row][col][0] +"%";
                        selection.append(COL_WORD).append(" NOT LIKE ? AND ");
                        argumentsList.add(rumenaNotLike);
                        break;
                    case 's':
                        String SivaNotLike = "%" + letterArray[row][col][0] +"%";
                        selection.append(COL_WORD).append(" NOT LIKE ? AND ");
                        argumentsList.add(SivaNotLike);
                        break;
                }
            }
        }

        selection.delete(selection.length() - 5, selection.length()); //zbrisemo zadnji AND
        Log.d("sql string", String.valueOf(selection));
        Log.d("sql values", String.valueOf(argumentsList));
        String[] arrayString = argumentsList.toArray(new String[0]); //iz arraylist v array

        //vrnemo max 20 nakljucnih besed ki ustrezajo
        Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(),
                null, String.valueOf(selection), arrayString, null, null, "RANDOM()", "20");

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;

    }

    public boolean validateWord(String word, String lang){
        //preveri ali je beseda na seznamu dovoljenih besed
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        Log.d("lang+word", lang + " " + word);

        if (lang.matches("eng")) {
            builder.setTables(TABLE_NAMES[0]);
        } else {
            builder.setTables(TABLE_NAMES[2]);
        }

        Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(),
                null, COL_WORD + " LIKE '" + word +"'", null, null, null, null);

        if (cursor == null) {
            return false;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
        }

    public void initiateDB() {
        //klic getReadableDatabase() poklice onCreate in ustvari db ob prvem zagonu
        //tako lahko uporabimo thread za branje txt v db in ne ustavi main threada
        //brez tega se db kreira sele ob filterResults, kjer pa ne moremo naloziti db v ozadju
        //EDIT thread ne dela pravilno

        //databaseOpenHelper.getReadableDatabase();

        //ce pa tole pozenem v threadu pa dela okej????
        new Thread(new Runnable() {
            public void run() {
                databaseOpenHelper.getReadableDatabase();
            }
        }).start();
    }

}
