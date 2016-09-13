package com.example.isseiomizu.weight.utils;

import com.example.isseiomizu.weight.models.IDayItem;

import java.util.Calendar;

/**
 * Events emitted by the bus provider.
 */
public class Events {

    public static class DayClickedEvent {

        public Calendar mCalendar;
        public IDayItem mDayItem;

        public DayClickedEvent(IDayItem dayItem) {
            this.mCalendar = Calendar.getInstance();
            this.mCalendar.setTime(dayItem.getDate());
            this.mDayItem = dayItem;
        }

        public Calendar getCalendar() {
            return mCalendar;
        }

        public IDayItem getDay() {
            return mDayItem;
        }
    }

    public static class CalendarScrolledEvent {
    }

    public static class AgendaListViewTouchedEvent {
    }

    public static class EventsFetched {
    }

    public static class EventsNext {
    }

    public static class EventsPrevious {
    }

    public static class ForecastFetched {
    }
}
