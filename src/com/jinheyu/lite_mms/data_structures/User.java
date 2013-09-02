package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;
import com.jinheyu.lite_mms.LeaderMainActivity;
import com.jinheyu.lite_mms.LoaderMainActivity;
import com.jinheyu.lite_mms.LogInActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xc on 13-8-12.
 */
public class User implements Parcelable {
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[0];
        }
    };
    private static final int DEPARTMENT_LEADER = 1;
    private static final int TEAM_LEADER = 2;
    private static final int LOADER = 3;
    private String userName;
    private String token;
    private int groupId;
    private int id;
    private int[] teamIdList;
    private int[] departmentIdList;
    private List<Team> teamList;
    private List<Department> departmentList;

    public User(int id, String userName, String token, int groupId, int[] teamIdList, int[] departmentIdList) {
        this.id = id;
        this.userName = userName;
        this.token = token;
        this.groupId = groupId;
        this.teamIdList = teamIdList;
        this.departmentIdList = departmentIdList;
        this.teamList = Team.getTeamListByIdList(teamIdList);
    }

    public User(Parcel parcel) {
        this.id = parcel.readInt();
        this.userName = parcel.readString();
        parcel.readIntArray(this.departmentIdList);
        parcel.readIntArray(this.teamIdList);
        this.groupId = parcel.readInt();
        this.token = parcel.readString();
    }

    public String getUserName() {
        return userName;
    }

    public String getToken() {
        return token;
    }

    public Class getDefaultActivity() {
        switch (groupId) {
            case LOADER:
                return LoaderMainActivity.class;
            case TEAM_LEADER:
                return LeaderMainActivity.class;
            default:
                return LogInActivity.class;
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public int getId() {
        return id;
    }

    public int[] getTeamIdList() {
        return teamIdList;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     *         by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(groupId);
        dest.writeIntArray(teamIdList);
        dest.writeIntArray(departmentIdList);
        dest.writeString(userName);
        dest.writeString(token);
    }

    public Team getTeamByIndex(int mNum) {
        int team_id = teamIdList[mNum];
        for (Team team : teamList) {
            if (team.getId() == team_id) {
                return team;
            }
        }
        return null;
    }

    public int[] getDepartmentIdList() {
        return departmentIdList;
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    public void setTeamList(List<Team> teamList) {
        this.teamList = teamList;
    }
}
