package com.jinheyu.lite_mms;

import android.os.AsyncTask;

import com.jinheyu.lite_mms.data_structures.WorkCommand;
import com.jinheyu.lite_mms.netutils.BadRequest;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by xc on 13-10-7.
 */
public class GetWorkCommandAsyncTask extends AsyncTask<Integer, Void, WorkCommand> {

    private final UpdateWorkCommand updateWorkcommand;
    Exception ex;

    public GetWorkCommandAsyncTask(UpdateWorkCommand updateWorkCommand) {
        this.updateWorkcommand = updateWorkCommand;
    }

    @Override
    protected WorkCommand doInBackground(Integer... params) {
        try {
            return MyApp.getWebServieHandler().getWorkCommand(params[0]);
        } catch (BadRequest badRequest) {
            badRequest.printStackTrace();
            ex = badRequest;
        } catch (IOException e) {
            e.printStackTrace();
            ex = e;
        } catch (JSONException e) {
            e.printStackTrace();
            ex = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(WorkCommand workCommand) {
        if (ex != null) {
            this.updateWorkcommand.updateWorkCommandFailed(ex);
        } else {
            this.updateWorkcommand.updateWorkCommand(workCommand);
        }
   }
}
