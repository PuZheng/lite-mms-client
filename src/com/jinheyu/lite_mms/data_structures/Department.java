package com.jinheyu.lite_mms.data_structures;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: yangminghua
 * Date: 13-8-28
 * Time: 上午10:31
 */
public class Department implements Parcelable {
    public static final Creator<Department> CREATOR = new Creator<Department>() {
        @Override
        public Department createFromParcel(Parcel source) {
            return new Department(source);
        }

        @Override
        public Department[] newArray(int size) {
            return new Department[0];
        }
    };
    private final static SparseArray<Department> DEPARTMENT_COLLECTION = new SparseArray<Department>();
    private int id;
    private String name;
    private List<User> leader_list;
    private List<Team> team_list;


    public Department(Parcel parcel) {
        id = parcel.readInt();
        name = parcel.readString();
        parcel.readTypedList(leader_list, User.CREATOR);
        parcel.readTypedList(team_list, Team.CREATOR);
    }

    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static void initDepartmentCollection(List<Department> departmentList) {
        for (Department department : departmentList) {
            DEPARTMENT_COLLECTION.put(department.getId(), department);
        }
    }

    public static Department getDepartmentById(int id) {
        return DEPARTMENT_COLLECTION.get(id);
    }

    public int getId() {
        return id;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
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
        dest.writeTypedList(team_list);
    }

    public List<User> getLeaderList() {
        return leader_list;
    }

    public List<Team> getTeamList() {
        return team_list;
    }

    public String[] getTeamNames() {
        int size = team_list.size();
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = team_list.get(i).getName();
        }
        return names;
    }

    public void setTeamList(int[] teamIds) {
        team_list = new ArrayList<Team>();
        for (int teamId : teamIds) {
            Team team = Team.getTeamById(teamId);
            team_list.add(team);
        }
    }


    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "车间 " + name;
    }
}
