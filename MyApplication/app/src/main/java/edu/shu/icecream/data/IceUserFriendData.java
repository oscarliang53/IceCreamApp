package edu.shu.icecream.data;

import java.util.ArrayList;
import java.util.List;

public class IceUserFriendData {
    String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public List<FriendBean> getFriendBeans() {
        return friendBeans;
    }

    public void setFriendBeans(List<FriendBean> friendBeans) {
        this.friendBeans = friendBeans;
    }

    List<FriendBean> friendBeans;

    public static class FriendBean {
        String name;
        String friendID;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFriendID() {
            return friendID;
        }

        public void setFriendID(String friendID) {
            this.friendID = friendID;
        }
    }
}
