package mn.today;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class AppDataManager implements Parcelable {


    private static final String mapFileName = "appdata.json";
    private static File appDataFile;

    /* MUST BE STATIC & GLOBAL Creates file directory for the data files please don't delete again :))) */
    private static HashMap<String,Flow> dataMap;
    private static Context mContext;

    /** Reads JSON data from file and builds it's flowList
     *  th the return data
     */
    public AppDataManager(Context context){
        mContext = context;
        appDataFile = new File(mContext.getFilesDir(), mapFileName);
        dataMap = buildMap(mContext);

    } // End of Constructor

    /**
     * Adds the given FlowObject to the HashMap under the keyToSave key.
     *
     * Then saves the new Map to Storage
     *
     * @param keyToSave key being saved
     * @param objectToSave Flow Object being saved
     */
    public void save(String keyToSave, Flow objectToSave) {
        dataMap.put(keyToSave,objectToSave);
        saveAppFile();
    }

    /**
     * returns the FlowObject value associated with the keyToLoad key in the HashMap
     * Then saves the new Map to Storage
     *
     * @param keyToLoad key paired with value to load
     * @return
     */
    public Flow load(String keyToLoad) {
        return dataMap.get(keyToLoad);
    }

    /**
     * Overwrites the value already present in keyToWrite with a new FlowObject value
     * Then saves the new Map to Storage
     *
     * @param keyToOverwrite key paired with value to overwrite
     * @param valueToWrite new value to overwrite with
     */
    public void overwrite(String keyToOverwrite, Flow valueToWrite) {
        dataMap.put(keyToOverwrite, valueToWrite);
        saveAppFile();
    }

    /**
     * Removes the value present at keyToDelete from the Hashmap.
     * Then saves the new Map to Storage
     * @param keyToDelete
     */
    public void delete(String keyToDelete) {
        dataMap.remove(keyToDelete);
        saveAppFile();
    }

    /**
     * Removes all key:value pairs in the hashmap and then completely erases all data
     * present in the internal storage file
     */
    public void deleteAllData() {

        dataMap.clear();
        eraseData();

    }

    /**
     * Builds an arraylist by iterating through the HashMap and populating the AL with the values
     * present
     * @return
     */
    public ArrayList<Flow> generateArrayList(){
        return new ArrayList<>(dataMap.values());
    }

    /**
     * Flag to determine there is any data in the Manager
     * @return
     */
    public boolean hasData() {
        return !dataMap.isEmpty();
    }

    /**
     * Converts the HashMap to JSON Format and then writes the JSON String to the specified file name in
     * internal storage
     *
     */
    private void saveAppFile() {

        Gson gson = new Gson();

        String jsonData = gson.toJson(dataMap);

        FileOutputStream outputStream;

        try {
            outputStream= mContext.openFileOutput(mapFileName, Context.MODE_PRIVATE);
            outputStream.write(jsonData.getBytes());
            outputStream.close();

        } catch (Exception e) {
            Log.e("ERROR:", "\n " +e.getMessage());

            Toast.makeText(mContext, "Could not save file", Toast.LENGTH_LONG).show();
        }


    }


    /**
     * Reads and builds a String Response from the data present in the specified internal
     * storage file.
     *
     * @return a json string representation of the data in file.
     */
    private String loadAppData() {
        try {

            FileInputStream fis = mContext.openFileInput(mapFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            String loadedData = sb.toString();
            fis.close();
            isr.close();
            bufferedReader.close();
            return loadedData;
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
            return "";
            // Returns blank if no flow file exists
        }
    }




    /**
     * Overwrites all data present in file with a blank file.
     *
     */
    private void eraseData() {
        try {
            FileOutputStream os = mContext.openFileOutput(mapFileName, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(os);
            writer.print("");
            writer.close();

        } catch (Exception e) {
            Toast.makeText(mContext, "Flows could not be deleted!", Toast.LENGTH_LONG);
        }
    }

    /**
     * Reads data from file as a json string, then builds a HashMap with the JSON obtained
     *
     * @param context the context in which the method is being called
     */
    private HashMap<String,Flow> buildMap(Context context) {
        Type type = new TypeToken<HashMap<String, Flow>>() {
        }.getType();

        try {
            Gson gson = new Gson();

            String json = loadAppData();

            if (json.equals("")) {
                // if return data is blank, no file was found (exists)
                return new HashMap<>();
            }

            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(true);
            //GSON throws that particular error when there's extra characters after the end of the object
            // that aren't whitespace, and it defines whitespace very narrowly

            //Expected BEGIN_OBJECT but was BEGIN_ARRAY

            return  gson.fromJson(reader, type);
            // Returns flows to use for Map Format
        } catch (Exception e) {
            // Flow loadAppData not regenerated that sucks..
            return  new HashMap<>();
        }

    }

    @Override
    public String toString() {

        return "\n~~~ APP DATA MAP: ~~~ " + this.loadAppData();
    }
    // Parcel Implementation to pass data from the Stream to the Sandbox about
    // the current flow object.
    // Still need to make method to calculate the total time for the flow based on elements
    // The order of READING and WRITING is important (Read and write in same order)


    /*~~~~~~ Implements Parcelable ~~~~~~*/

    public AppDataManager(Parcel in) {
        dataMap = in.readHashMap(getClass().getClassLoader());

    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(dataMap);
    }


    public static final Parcelable.Creator<AppDataManager> CREATOR =
            new Parcelable.Creator<AppDataManager>() {

                @Override
                public AppDataManager createFromParcel(Parcel source) {
                    return new AppDataManager(source);
                    //Using Parcelable constructor
                }

                @Override
                public AppDataManager[] newArray(int size) {
                    return new AppDataManager[size];
                }
            };

}
