package com.udacity.gradle.builditbigger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.opensource.worldwide.showjokes.DisplayJokes;
import com.udacity.gradle.builditbigger.backend.myApi.MyApi;

import java.io.IOException;

import timber.log.Timber;

class EndpointsAsyncTask extends AsyncTask<Void, Void, String> {

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private ProgressBar mProgressBar;

    public EndpointsAsyncTask(Context context, ProgressBar progressBar) {
        mContext = context;
        mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(Void... voids) {
        MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                .setApplicationName("backend")
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        abstractGoogleClientRequest.setDisableGZipContent(true);
                    }
                });

        MyApi myApiService = builder.build();

        String joke = null;

        try {
            joke = myApiService.getJoke().execute().getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return joke;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mProgressBar != null)
        mProgressBar.setVisibility(View.INVISIBLE);
        startDisplayJokes(s);
    }

    private void startDisplayJokes(String joke) {
        Intent sendJoke = new Intent(mContext, DisplayJokes.class);
        sendJoke.putExtra(mContext.getString(R.string.joke_key), joke);
        sendJoke.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(sendJoke);
    }
}