package com.example.isseiomizu.weight;

import com.example.isseiomizu.weight.models.DayItem;
import com.example.isseiomizu.weight.models.IDayItem;
import com.example.isseiomizu.weight.models.IWeekItem;
import com.example.isseiomizu.weight.models.CalendarEvent;
import com.example.isseiomizu.weight.models.IWeightItem;
import com.example.isseiomizu.weight.models.WeekItem;
import com.example.isseiomizu.weight.models.WeightItem;
import com.example.isseiomizu.weight.utils.DateHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class manages information about the calendar. (Events, weather info...)
 * Holds reference to the days list of the calendar.
 * As the app is using several views, we want to keep everything in one place.
 */
public class CalendarManager {

    private static final String LOG_TAG = CalendarManager.class.getSimpleName();

    private static CalendarManager mInstance;

    private static SQLiteDatabase mDbWeight;

    private Context mContext;
    private Locale mLocale;
    private Calendar mMinCal;
    private Calendar mMaxCal;
    private Calendar mToday = Calendar.getInstance();
    private SimpleDateFormat mWeekdayFormatter;
    private SimpleDateFormat mMonthHalfNameFormat;

    /// instances of classes provided from outside
    private IDayItem mCleanDay;
    private IWeekItem mCleanWeek;

    /**
     * List of weights used by the calendar
     */
    private List<IWeightItem> mWeights = new ArrayList<>();
    /**
     * List of days used by the calendar
     */
    private List<IDayItem> mDays = new ArrayList<>();
    /**
     * List of weeks used by the calendar
     */
    private List<IWeekItem> mWeeks = new ArrayList<>();
    /**
     * List of events instances
     */
    private List<CalendarEvent> mEvents = new ArrayList<>();

    // region Constructors

    public CalendarManager(Context context) {
        this.mContext = context;
    }

    public static CalendarManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CalendarManager(context);
        }

        // db
        if (mDbWeight == null) {
            MyOpenHelper helper = new MyOpenHelper(context);
            mDbWeight = helper.getWritableDatabase();
        }

        return mInstance;
    }

    public static CalendarManager getInstance() {
        return mInstance;
    }

    // endregion

    // region Getters/Setters

    public Locale getLocale() {
        return mLocale;
    }

    public Context getContext() {
        return mContext;
    }

    public Calendar getToday() {
        return mToday;
    }

    public void setToday(Calendar today) {
        this.mToday = today;
    }

    public List<IWeightItem> getWeights() {
        return mWeights;
    }

    public List<IWeekItem> getWeeks() {
        return mWeeks;
    }

    public List<CalendarEvent> getEvents() {
        return mEvents;
    }

    public List<IDayItem> getDays() {
        return mDays;
    }

    public SimpleDateFormat getWeekdayFormatter() {
        return mWeekdayFormatter;
    }

    public SimpleDateFormat getMonthHalfNameFormat() {
        return mMonthHalfNameFormat;
    }

    // endregion

    // region Public methods

    public void buildCal(Calendar minDate, Calendar maxDate, Locale locale, IDayItem cleanDay, IWeekItem cleanWeek) {
        if (minDate == null || maxDate == null) {
            throw new IllegalArgumentException(
                    "minDate and maxDate must be non-null.");
        }
        if (minDate.after(maxDate)) {
            throw new IllegalArgumentException(
                    "minDate must be before maxDate.");
        }
        if (locale == null) {
            throw new IllegalArgumentException("Locale is null.");
        }

        setLocale(locale);

        mDays.clear();
        mWeeks.clear();
        mEvents.clear();

        mCleanDay = cleanDay;
        mCleanWeek = cleanWeek;

        mMinCal = Calendar.getInstance(mLocale);
        mMaxCal = Calendar.getInstance(mLocale);
        Calendar mWeekCounter = Calendar.getInstance(mLocale);

        mMinCal.setTime(minDate.getTime());
        mMaxCal.setTime(maxDate.getTime());

        // maxDate is exclusive, here we bump back to the previous day, as maxDate if December 1st, 2020,
        // we don't include that month in our list
        mMaxCal.add(Calendar.MINUTE, -1);

        // Now iterate we iterate between mMinCal and mMaxCal so we build our list of weeks
        mWeekCounter.setTime(mMinCal.getTime());
        int maxMonth = mMaxCal.get(Calendar.MONTH);
        int maxYear = mMaxCal.get(Calendar.YEAR);

        int currentMonth = mWeekCounter.get(Calendar.MONTH);
        int currentYear = mWeekCounter.get(Calendar.YEAR);

        // Loop through the weeks
        while ((currentMonth <= maxMonth + 1 // Up to, including the month.
                || currentYear < maxYear) // Up to the year.
                && currentYear < maxYear + 1) { // But not > next yr.

            Date date = mWeekCounter.getTime();
            // Build our week list
            int currentWeekOfYear = mWeekCounter.get(Calendar.WEEK_OF_YEAR);

            IWeekItem weekItem = cleanWeek.copy();
            weekItem.setWeekInYear(currentWeekOfYear);
            weekItem.setYear(currentYear);
            weekItem.setDate(date);
            weekItem.setMonth(currentMonth);
            weekItem.setLabel(mMonthHalfNameFormat.format(date));
            List<IDayItem> dayItems = getDayCells(mWeekCounter); // gather days for the built week
            weekItem.setDayItems(dayItems);
            mWeeks.add(weekItem);

            Log.d(LOG_TAG, String.format("Adding week: %s", weekItem));

            mWeekCounter.add(Calendar.WEEK_OF_YEAR, 1);

            currentMonth = mWeekCounter.get(Calendar.MONTH);
            currentYear = mWeekCounter.get(Calendar.YEAR);
        }
    }

    public void loadPrevious() {
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        minDate.setTime(mMinCal.getTime());
        maxDate.setTime(mMaxCal.getTime());

        minDate.add(Calendar.MONTH, -1);
        maxDate.add(Calendar.MONTH, -1);

        buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());

        loadWeights();

    }

    public void loadCal(Calendar calendar) {
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        minDate.setTime(mMinCal.getTime());
        maxDate.setTime(mMaxCal.getTime());

        minDate.add(Calendar.MONTH, -1);
        maxDate.add(Calendar.MONTH, -1);

        buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());

        loadWeights();

    }

    public void loadNext() {
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        minDate.setTime(mMinCal.getTime());
        maxDate.setTime(mMaxCal.getTime());

        minDate.add(Calendar.MONTH, 1);
        maxDate.add(Calendar.MONTH, 1);

        buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());

        loadWeights();
    }

    public void loadWeights() {
        mWeights.clear();

        Map<String, IWeightItem> map = new HashMap<>();

        List<String> list = new ArrayList<>();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";

        // sqliteからデータを全取得
        Cursor c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, null, null, null, null, "date DESC");
        boolean mov = c.moveToFirst();

        while (mov) {
            IWeightItem item = new WeightItem();

            date = c.getString(0);
            weight = c.getString(1);
            bodyFatPercentage = c.getString(2);

            item.setWeight(weight);
            item.setBodyFatPercentage(bodyFatPercentage);

            map.put(date, item);

            // 次のレコードへ
            mov = c.moveToNext();
        }
        c.close();

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");


        int maxMonth = mMaxCal.get(Calendar.MONTH);
        int maxYear = mMaxCal.get(Calendar.YEAR);

        Calendar mWeekCounter = Calendar.getInstance(mLocale);
        mWeekCounter.setTime(mMinCal.getTime());
        int currentMonth = mWeekCounter.get(Calendar.MONTH);
        int currentYear = mWeekCounter.get(Calendar.YEAR);

        for (IWeekItem weekItem : getWeeks()) {
            for (IDayItem dayItem : weekItem.getDayItems()) {
                IWeightItem item = new WeightItem();
                item = map.get(sdf1.format(dayItem.getDate()));

                if (item == null) {
                    item = new WeightItem();
                }

                item.setDate(dayItem.getDate());
                currentMonth = dayItem.getDate().getMonth();

                if ((currentMonth == maxMonth // Up to, including the month.
                        || currentYear < maxYear) // Up to the year.
                        && currentYear == maxYear) { // But not > next yr.
                    mWeights.add(item);
                }

//                currentMonth = mWeekCounter.get(Calendar.MONTH);
                currentYear = mWeekCounter.get(Calendar.YEAR);
            }
        }
    }

    public void loadEvents(List<CalendarEvent> eventList, CalendarEvent noEvent) {

        for (IWeekItem weekItem : getWeeks()) {
            for (IDayItem dayItem : weekItem.getDayItems()) {
                boolean isEventForDay = false;
                for (CalendarEvent event : eventList) {
                    if (DateHelper.isBetweenInclusive(dayItem.getDate(), event.getStartTime(), event.getEndTime())) {
                        CalendarEvent copy = event.copy();

                        Calendar dayInstance = Calendar.getInstance();
                        dayInstance.setTime(dayItem.getDate());
                        copy.setInstanceDay(dayInstance);
                        copy.setDayReference(dayItem);
                        copy.setWeekReference(weekItem);
                        // add instances in chronological order
                        getEvents().add(copy);
                        isEventForDay = true;
                    }
                }
                if (!isEventForDay) {
                    Calendar dayInstance = Calendar.getInstance();
                    dayInstance.setTime(dayItem.getDate());
                    CalendarEvent copy = noEvent.copy();

                    copy.setInstanceDay(dayInstance);
                    copy.setDayReference(dayItem);
                    copy.setWeekReference(weekItem);
                    copy.setLocation("");
//                    copy.setTitle(getContext().getResources().getString(R.string.agenda_event_no_events));
                    copy.setPlaceholder(true);
                    getEvents().add(copy);
                }
            }
        }
    }

    public void loadCal (Locale locale, List<IWeekItem> lWeeks, List<IDayItem> lDays, List<CalendarEvent> lEvents) {
        mWeeks = lWeeks;
        mDays = lDays;
        mEvents = lEvents;
        setLocale(locale);
    }

    // endregion

    // region Private methods

    private List<IDayItem> getDayCells(Calendar startCal) {
        Calendar cal = Calendar.getInstance(mLocale);
        cal.setTime(startCal.getTime());
        List<IDayItem> dayItems = new ArrayList<>();

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
        if (offset > 0) {
            offset -= 7;
        }
        cal.add(Calendar.DATE, offset);

        Log.d(LOG_TAG, String.format("Buiding row week starting at %s", cal.getTime()));
        for (int c = 0; c < 7; c++) {
            IDayItem dayItem = mCleanDay.copy();
            dayItem.buildDayItemFromCal(cal);
            dayItems.add(dayItem);
            cal.add(Calendar.DATE, 1);
        }

        mDays.addAll(dayItems);
        return dayItems;
    }

    private void setLocale(Locale locale) {
        this.mLocale = locale;
        setToday(Calendar.getInstance(mLocale));
        mWeekdayFormatter = new SimpleDateFormat(getContext().getString(R.string.day_name_format), mLocale);
        mMonthHalfNameFormat = new SimpleDateFormat(getContext().getString(R.string.month_half_name_format), locale);
    }

    // endregion
}
