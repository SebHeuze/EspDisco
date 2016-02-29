package espdisco.hodor.ninja.espdisco.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import espdisco.hodor.ninja.espdisco.model.Esp8266;

public class EspBDD {

    private static final int VERSION_BDD = 1;
    private static final String NOM_BDD = "esp.db";


    private static final String TABLE_ESP = "table_esp";
    private static final String COL_IP = "IP";
    private static final int NUM_COL_IP = 0;
    private static final String COL_CHECKED = "CHECKED";
    private static final int NUM_COL_CHECKED = 1;

    private SQLiteDatabase bdd;

    private MaBaseSQLite maBaseSQLite;

    public EspBDD(Context context){
        //On crée la BDD et sa table
        maBaseSQLite = new MaBaseSQLite(context, NOM_BDD, null, VERSION_BDD);
    }

    public void open(){
        //on ouvre la BDD en écriture
        bdd = maBaseSQLite.getWritableDatabase();
    }

    public void close(){
        //on ferme l'accès à la BDD
        bdd.close();
    }

    public SQLiteDatabase getBDD(){
        return bdd;
    }

    public long insertEsp(Esp8266 esp){
        //Création d'un ContentValues (fonctionne comme une HashMap)
        ContentValues values = new ContentValues();
        //on lui ajoute une valeur associée à une clé (qui est le nom de la colonne dans laquelle on veut mettre la valeur)
        values.put(COL_IP, esp.getIpAdress());
        values.put(COL_CHECKED, esp.isSelected()?1:0);
        //on insère l'objet dans la BDD via le ContentValues
        return bdd.insert(TABLE_ESP, null, values);
    }

    public int setSelected(Esp8266 esp){
        //La mise à jour d'un livre dans la BDD fonctionne plus ou moins comme une insertion
        //il faut simplement préciser quel livre on doit mettre à jour grâce à l'ID
        ContentValues values = new ContentValues();
        values.put(COL_CHECKED, esp.isSelected()?1:0);
        return bdd.update(TABLE_ESP, values, COL_IP + " = '" + esp.getIpAdress() + "'", null);
    }

    public void truncate(){
        //Suppression d'un livre de la BDD grâce à l'ID
        bdd.execSQL("DELETE FROM " + TABLE_ESP);
    }

    public List<Esp8266> getAllEsp(){
        //Récupère dans un Cursor les valeurs correspondant à un livre contenu dans la BDD (ici on sélectionne le livre grâce à son titre)
        Cursor cursor = bdd.rawQuery("select * from " + TABLE_ESP, null);
        List<Esp8266> esp8266List = new ArrayList<>();
        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                String ip = cursor.getString(NUM_COL_IP);
                int checked = cursor.getInt(NUM_COL_CHECKED);

                esp8266List.add(new Esp8266(ip, checked==1?true:false));
                cursor.moveToNext();
            }
        }
        return esp8266List;
    }

    public List<Esp8266> getCheckedEsp(){
        //Récupère dans un Cursor les valeurs correspondant à un livre contenu dans la BDD (ici on sélectionne le livre grâce à son titre)
        Cursor cursor = bdd.rawQuery("select * from " + TABLE_ESP + " where "+COL_CHECKED+" = 1", null);
        List<Esp8266> esp8266List = new ArrayList<>();
        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                String ip = cursor.getString(NUM_COL_IP);
                int checked = cursor.getInt(NUM_COL_CHECKED);

                esp8266List.add(new Esp8266(ip, checked==1?true:false));
                cursor.moveToNext();
            }
        }
        return esp8266List;
    }
}