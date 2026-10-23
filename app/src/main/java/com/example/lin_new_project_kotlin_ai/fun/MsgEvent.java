package com.example.lin_new_project_kotlin_ai.fun;

public class MsgEvent {
    private final int eventType;
    private final Object obj;
    public MsgEvent(int eventType, Object obj) {
        this.eventType = eventType;
        this.obj = obj;
    }


    public int getType(){
        return eventType;
    }

    public Object getObj() {
        return obj;
    }
}
