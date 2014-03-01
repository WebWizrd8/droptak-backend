package edu.purdue.maptak.admin.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MapTakDB extends SQLiteOpenHelper {

    /** Database name and version */
    private Context context;
    public static final String DB_NAME = "database_cached_taks";
    public static final int DB_VERSION = 1;

    /** Tables */
    public static final String TABLE_MAPS = "t_maps";
    public static final String TABLE_TAKS = "t_taks";

    /** Columns - TABLE_MAPS */
    public static final String MAP_ID = "_id";
    public static final String MAP_LABEL = "map_label";

    /** Columns - TABLE_TAKS */
    public static final String TAK_ID = "_id";
    public static final String TAK_MAP_ID = "map_id";
    public static final String TAK_LABEL = "tak_label";
    public static final String TAK_LAT = "tak_lat";
    public static final String TAK_LNG = "tak_lng";

    /** Default constructor */
    public MapTakDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    /** Called when the database is first created. */
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Strings which create tables

        String create_table_maps = "CREATE TABLE " + TABLE_MAPS + " (" +
                MAP_ID + " TEXT, " +
                MAP_LABEL + " TEXT );";

        String create_table_taks = "CREATE TABLE " + TABLE_TAKS + " ( " +
                TAK_ID + " TEXT, " +
                TAK_MAP_ID + " TEXT, " +
                TAK_LABEL + " TEXT, " +
                TAK_LAT + " DOUBLE, " +
                TAK_LNG + " DOUBLE );";

        // Create the tables from the strings provided

        sqLiteDatabase.execSQL(create_table_maps);
        sqLiteDatabase.execSQL(create_table_taks);

    }

    /** Called when the database is upgraded from one DB_VERSION to the next */
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    /** Destroys the local database and creates a new one. */
    public void destroy() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            db.execSQL("DROP TABLE " + TABLE_MAPS);
            db.execSQL("DROP TABLE " + TABLE_TAKS);
            onCreate(db);
        }
    }

    /** Refreshes the database with information from the server */
    public void refresh() {
        // Implement in sprint 2
    }

    /** Pushes a new map for the user to the remote database then refreshes the local cache */
    public void addMap(MapObject map) {

        // Add the map to the local database
        ContentValues values = new ContentValues();
        values.put(MAP_ID, -1);
        values.put(MAP_LABEL, map.getLabel());
        getWritableDatabase().insert(TABLE_MAPS, null, values);

        // Push the change to the server
        // Implement in sprint 2

    }

    /** Pushes a new tak for a given map to the remote database then refreshes the local cache */
    public void addTak(TakObject tak, MapID map) {

        // Add the tak to the local cache
        ContentValues values = new ContentValues();
        values.put(TAK_ID, -1);
        values.put(TAK_MAP_ID, map.getIDStr());
        values.put(TAK_LABEL, tak.getLabel());
        values.put(TAK_LAT, tak.getLatitude());
        values.put(TAK_LNG, tak.getLongitude());
        getWritableDatabase().insert(TABLE_TAKS, null, values);

        // Push the change to the server
        // Implement in sprint 2

    }

    /** Removes a tak from both the server and the local data cache */
    public void removeTak(TakID tak) {

        // Remove the tak from the local cache
        getWritableDatabase().delete(TABLE_TAKS, TAK_ID + "=\'" + tak.getIDStr() + "\'", null);

        // Push the change to the server

    }

    /** Returns a list of the maps the user is authorized to access. */
    public List<MapObject> getUsersMaps() {
        List<MapObject> results = new LinkedList<MapObject>();
        Cursor c = getReadableDatabase().query(TABLE_MAPS, null, null, null, null, null, null);
        c.moveToFirst();
        do {
            // Create the ID
            String id = c.getString(c.getColumnIndex(MAP_ID));
            MapID mapID = new MapID(id);

            // Create the label
            String label = c.getString(c.getColumnIndex(MAP_LABEL));

            // Get the taks for the map
            List<TakObject> taks = getTaks(mapID);

            // Create the map object
            MapObject map = new MapObject(context, label, mapID, taks);

            // Add to the list
            results.add(map);

        } while (c.moveToNext());

        return results;
    }

    /** Returns a specific map with a given UUID, or null if it doesn't exist in the cache */
    public MapObject getMap(MapID mapID) {
        List<MapObject> usersMaps = getUsersMaps();
        for (MapObject m : usersMaps) {
            if (m.getID().equals(mapID)) {
                return m;
            }
        }
        return null;
    }

    /** Returns a list of taks associated with a given mapid */
    public List<TakObject> getTaks(MapID mapID) {
        List<TakObject> results = new LinkedList<TakObject>();

        // TODO: Optimize this
        Cursor c = getReadableDatabase().query(TABLE_TAKS, null, null, null, null, null, null);
        c.moveToFirst();
        do {
            String iterMapID = c.getString(c.getColumnIndex(TAK_MAP_ID));
            if (mapID.equals(iterMapID.equals(mapID.getIDStr()))) {
                String takID = c.getString(c.getColumnIndex(TAK_ID));
                String takLabel = c.getString(c.getColumnIndex(TAK_LABEL));
                double takLat = c.getDouble(c.getColumnIndex(TAK_LAT));
                double takLng = c.getDouble(c.getColumnIndex(TAK_LNG));
                TakObject t = new TakObject(new TakID(takID), takLabel, takLat, takLng);
                results.add(t);
            }
        } while (c.moveToNext());

        return results;
    }

    /** Returns a specific tak with a given ID, or null if it doesn't exist in the cache
     *  Returns null if the object does not exist in the database. */
    public TakObject getTak(TakID takID) {

        Cursor c = getReadableDatabase().query(TABLE_TAKS, null, TAK_ID + "=\'" + takID.getIDStr() + "\'", null, null, null, null);

        if (c.moveToFirst()) {
            String takIDStr = c.getString(c.getColumnIndex(TAK_ID));
            String takLabel = c.getString(c.getColumnIndex(TAK_LABEL));
            double takLat = c.getDouble(c.getColumnIndex(TAK_LAT));
            double takLng = c.getDouble(c.getColumnIndex(TAK_LNG));
            TakObject obj = new TakObject(new TakID(takIDStr), takLabel, takLat, takLng);
            return obj;
        }

        return null;
    }



}
