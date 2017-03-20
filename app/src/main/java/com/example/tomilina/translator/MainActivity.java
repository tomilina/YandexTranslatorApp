package com.example.tomilina.translator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** URL to query the translation of the text */
    private static final String YANDEX_TRANSLATOR_API_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    private static final String YANDEX_TRANSLATOR_API_KEY = "trnsl.1.1.20170320T032436Z.bfdee3577047a62c.91d782b5630a61b07011fa2f22131ef648c02081";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kick off an {@link AsyncTask} to perform the network request
        TranslatorAsyncTask task = new TranslatorAsyncTask();
        task.execute();
    }

    /**
     * Update the screen to display information
     */
    private void updateUi(String text) {
        // Display the earthquake title in the UI
        TextView titleTextView = (TextView) findViewById(R.id.text);
        titleTextView.setText(text);
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI
     */
    private class TranslatorAsyncTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl("https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170320T032436Z.bfdee3577047a62c.91d782b5630a61b07011fa2f22131ef648c02081&text=ПРИВЕТ!&lang=ru-fr");
//            URL url = createUrl(YANDEX_TRANSLATOR_API_URL
//                    +"key="+YANDEX_TRANSLATOR_API_KEY
//                    +"&text=" + "Приятного аппетита!"
//                    +"&lang=" + "fr");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            String text = extractFeatureFromJson(jsonResponse);

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return text;
        }

        /**
         * Update the screen with the given earthquake (which was the result of the

         */
        @Override
        protected void onPostExecute(String text) {
            if (text == null) {
                return;
            }

            updateUi(text);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

//            Если url пустая строка
            if(url == null){
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
//                Только если сервер ответил успехом делаем действия
                if ( urlConnection.getResponseCode()==200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }
                else{
                    Log.e("MainActivity","Current HTTP response code: "+urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e("MainActivity","IOException!");
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an object by parsing out information
         * about the first earthquake from the input earthquakeJSON string.
         */
        private String extractFeatureFromJson(String translatorJSON) {
            try {
                JSONObject baseJsonResponse = new JSONObject(translatorJSON);
                JSONArray text_array = baseJsonResponse.getJSONArray("text");
                String text = text_array.getString(0);

                return text;

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
            }
            return null;
        }
    }
}
