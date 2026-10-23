package com.example.lin_new_project_kotlin_ai;

import androidx.annotation.IntDef;

public class General {


    //WINDOW Animation
    public static final int TRANSLATION_X = 0;
    public static final int TRANSLATION_Y = 1;
    public static final int FADE = 2;
    public static final int SCALE_X = 3;
    public static final int SCALE_Y = 4;
    public static final int NONE = 5;


    public static enum LongKEY {
        LevelAdd, LevelLess, InclineAdd, InclineLess
    }

    public static enum UserProfile_Enum {
        UserProfile_Home, UserProfile_Age, UserProfile_Height,UserProfile_Weight
    }


    @IntDef({TRANSLATION_Y, TRANSLATION_X, FADE})
    public @interface animationType {
    }
}
