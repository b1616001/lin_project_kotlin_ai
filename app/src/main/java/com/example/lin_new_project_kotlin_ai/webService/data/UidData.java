package com.example.lin_new_project_kotlin_ai.webService.data;

import java.util.List;

public class UidData {


    private List<DATABean> DATA;

    public List<DATABean> getDATA() {
        return DATA;
    }

    public void setDATA(List<DATABean> DATA) {
        this.DATA = DATA;
    }

    public static class DATABean {
        /**
         * UID : 56670
         */

        private String UID;

        public String getUID() {
            return UID;
        }

        public void setUID(String UID) {
            this.UID = UID;
        }
    }
}
