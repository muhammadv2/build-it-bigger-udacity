package com.udacity.gradle.builditbigger;

import android.test.AndroidTestCase;

public class EndpointsAsyncTaskTest extends AndroidTestCase{

    public void test() {
        String joke = null;
        EndpointsAsyncTask asyncTask = new EndpointsAsyncTask(getContext());
        asyncTask.execute();
        try {
            joke = asyncTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(joke);
    }

}