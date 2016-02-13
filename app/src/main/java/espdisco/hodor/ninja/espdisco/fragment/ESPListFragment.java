package espdisco.hodor.ninja.espdisco.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import espdisco.hodor.ninja.espdisco.EspArrayAdapter;
import espdisco.hodor.ninja.espdisco.LedView;
import espdisco.hodor.ninja.espdisco.R;
import espdisco.hodor.ninja.espdisco.enums.LEDMode;
import espdisco.hodor.ninja.espdisco.model.Esp8266;

public class ESPListFragment extends Fragment{

    private static final String LOG_TAG = "ESPListFragment";


    private Button espScanButton;
    private ProgressBar progressBar;

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

        ArrayAdapter<Esp8266> adapter =
                new EspArrayAdapter(getActivity(), getESPList());

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

    private List<Esp8266> getESPList() {
        List<Esp8266> list = new ArrayList<Esp8266>();
        list.add(new Esp8266("192.168.1.1", "Esp1"));
        list.add(new Esp8266("192.168.1.2", "Esp2"));
        list.add(new Esp8266("192.168.1.3", "Esp3"));
        list.get(1).setSelected(true);  // select one item by default
        return list;
    }

    private void setListItems(List<String> foundEspList){
        Log.d(LOG_TAG, "Nombre d'ESP trouvés " + foundEspList.size());
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
        private static final int TIMEOUT = 200;
        private static final int MIN_RANGE = 1;
        private static final int MAX_RANGE = 100;

        protected List<String> doInBackground(Void... params) {
            List<String> hostList = new ArrayList<>();
            for (int i=MIN_RANGE;i<=MAX_RANGE;i++){
                String host=SUB_NETWORK + "." + i;
                if (isPortOpen(host,ESP_PORT, TIMEOUT)){
                    hostList.add(host);
                }
                publishProgress((int) ((i / (float) MAX_RANGE) * 100));
            }
            return hostList;
        }

        protected void onProgressUpdate(Integer... progress) {
            Log.d(LOG_TAG, "Progression : "+progress[0]);
            progressBar.setProgress(progress[0]);
        }

        protected void onPostExecute(List<String> result) {
            setListItems(result);
        }


        private boolean isPortOpen(final String ip, final int port, final int timeout) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), timeout);
                socket.close();
                return true;
            }

            catch(ConnectException ce){
                return false;
            }

            catch (Exception ex) {
                return false;
            }
        }
    }
}