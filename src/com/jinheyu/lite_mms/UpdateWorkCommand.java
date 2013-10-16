package com.jinheyu.lite_mms;

import com.jinheyu.lite_mms.data_structures.WorkCommand;

/**
 * Created by xc on 13-10-7.
 */
public interface UpdateWorkCommand {

    public void updateWorkCommand(WorkCommand workCommand);
    public void updateWorkCommandFailed(Exception ex);
    public void beforeUpdateWorkCommand();
}
