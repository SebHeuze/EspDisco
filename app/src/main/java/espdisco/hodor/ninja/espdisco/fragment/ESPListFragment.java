package espdisco.hodor.ninja.espdisco.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import espdisco.hodor.ninja.espdisco.EspArrayAdapter;
import espdisco.hodor.ninja.espdisco.LedView;
import espdisco.hodor.ninja.espdisco.MainActivity;
import espdisco.hodor.ninja.espdisco.R;
import espdisco.hodor.ninja.espdisco.dao.EspBDD;
import espdisco.hodor.ninja.espdisco.enums.LEDMode;
import espdisco.hodor.ninja.espdisco.model.Esp8266;
import espdisco.hodor.ninja.espdisco.thread.SocketRequest;
import espdisco.hodor.ninja.espdisco.utils.EspManager;

public class ESPListFragment extends Fragment{

    private static final String LOG_TAG = "ESPListFragment";

    private EspBDD bdd;
    private Button espScanButton;
    private ProgressBar progressBar;

    private  ArrayAdapter<Esp8266> adapter;

    private List<Esp8266> loadedList;

    public ESPListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_esp_list, container, false);

        //now you must initialize your list view
        ListView listview =(ListView)view.findViewById(R.id.listView);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Esp8266 item = adapter.getItem(position);
                Log.d(LOG_TAG, "Item clicked " + item.getIpAdress());

                ColorPickerDialogBuilder
                        .with(getActivity())
                        .setTitle("Choose color")
                        .initialColor(Color.RED)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                int blue =  selectedColor & 255;
                                int green = (selectedColor >> 8) & 255;
                                int red =   (selectedColor >> 16) & 255;
                                EspManager.getInstance().changeColorOfEsp(item, red, green, blue );
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();

            }
        });

        this.reloadESPList();
        adapter =  new EspArrayAdapter(getActivity(), loadedList);

        listview.setAdapter(adapter);

        return view;
    }
    @Override
    public void onViewCreated (View view, Bundle savedInstanceState){
        espScanButton = (Button) getView().findViewById(R.id.scanEsp);
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);

        espScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scanForEsp();
            }
        });

    }

    private void reloadESPList() {
        loadedList = ((MainActivity)getActivity()).getBdd().getAllEsp();
    }

    private void setListItems(List<String> foundEspList){
        Log.d(LOG_TAG, "Nombre d'ESP trouvés " + foundEspList.size());
        ((MainActivity)getActivity()).getBdd().truncate();
        for(String EspHost : foundEspList){
            Esp8266 esp = new Esp8266(EspHost, true);
            ((MainActivity)getActivity()).getBdd().insertEsp(esp);
        }
        this.reloadESPList();

        adapter.clear();
        adapter.addAll(loadedList);
        adapter.notifyDataSetChanged();
    }

    private void scanForEsp(){
        new EspScanner().execute();

    }


    /**
     * Created by SEB on 13/02/2016.
     */
    private class EspScanner extends AsyncTask<Void, Integer, List<String>> {

        private static final String SUB_NETWORK = "192.168.1";
        private static final int ESP_PORT = 8266;
        private static final int TIMEOUT = 2000;
        private static final int MIN_RANGE = 1;
        private static final int MAX_RANGE = 50;

        protected List<String> doInBackground(Void... params) {
            List<String> hostList = new ArrayList<>();
            List<SocketRequest> requests = new ArrayList<>();
            for (int i=MIN_RANGE;i<=MAX_RANGE;i++){
                String host=SUB_NETWORK + "." + i;
                Log.d(LOG_TAG, "Création thread pour ip " + host);
                SocketRequest request = new SocketRequest(host, ESP_PORT, TIMEOUT);
                Thread thread = new Thread(request);
                thread.start();
                requests.add(request);
            }

            int progress = MIN_RANGE;

            for (SocketRequest request : requests) {
                Log.d(LOG_TAG, "Récupération requete " + request.getIp());
                while(!request.isCompleted()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(!request.isFailed()){
                    hostList.add(request.getIp());
                }

                publishProgress((int) ((progress / (float) MAX_RANGE) * 100));
                progress++;
            }

            return hostList;
        }

        protected void onProgressUpdate(Integer... progress) {
            Log.d(LOG_TAG, "Progression : " + progress[0]);
            progressBar.setProgress(progress[0]);
        }

        protected void onPostExecute(List<String> result) {
            setListItems(result);
        }

    }
}