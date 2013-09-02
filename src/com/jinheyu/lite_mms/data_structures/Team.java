package com.jinheyu.lite_mms.data_structures;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import com.jinheyu.lite_mms.MyApp;
import com.jinheyu.lite_mms.netutils.BadRequest;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yangminghua
 * Date: 13-8-27
 * Time: 下午4:52
 */
public class Team implements Parcelable {

    public static final Creator<Team> CREATOR = new Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel source) {
            return new Team(source);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[0];
        }
    };
    private static final HashMap<Integer, Team> team_collection = new HashMap<Integer, Team>();
    private int id;
    private String name;
    private List<User> leader_list;

    public Team(Parcel parcel) {
        this.id = parcel.readInt();
        this.name = parcel.readString();
        parcel.readTypedList(leader_list, User.CREATOR);
    }


    public Team(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static boolean initTeamCollection(List<Team> teamList) {
        for (Team team : teamList) {
            team_collection.put(team.getId(), team);
        }
        return team_collection.isEmpty();
    }

    public static Team getTeamById(int teamId) {
        return team_collection.get(teamId);
    }

    public static List<Team> getTeamListByIdList(int[] teamIdList) {
        List<Team> result = new ArrayList<Team>();
        for (int i : teamIdList) {
            result.add(getTeamById(i));
        }
        return result;
    }

    public int getId() {
        return id;
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
        dest.writeString(name);
        dest.writeTypedList(leader_list);
    }

    public String getName() {
        return name;
    }

}
