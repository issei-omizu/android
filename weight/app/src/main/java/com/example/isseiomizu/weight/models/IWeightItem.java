package com.example.isseiomizu.weight.models;

import java.util.Calendar;
import java.util.Date;

public interface IWeightItem {

    // region Getters/Setters

    Date getDate();

    void setDate(Date date);

    int getValue();

    void setValue(int value);

    boolean isToday();

    void setToday(boolean today);

    boolean isSelected();

    void setSelected(boolean selected);

    boolean isFirstDayOfTheMonth();

    void setFirstDayOfTheMonth(boolean firstDayOfTheMonth);

    String getMonth();

    void setMonth(String month);

    int getDayOftheWeek();

    void setDayOftheWeek(int mDayOftheWeek);

    // endregion

    void buildDayItemFromCal(Calendar calendar);

    String toString();

    IWeightItem copy();

}
