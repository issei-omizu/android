package com.example.isseiomizu.weight;

import com.example.isseiomizu.weight.models.CalendarEvent;
import com.example.isseiomizu.weight.models.IDayItem;

import java.util.Calendar;

public interface CalendarPickerController {
    void onDaySelected(IDayItem dayItem);

    void onEventSelected(CalendarEvent event);

    void onScrollToDate(Calendar calendar);
}
