package com.example.soundmotionlogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by glaput on 1/23/16.
 */
public class Constants {

    public static final boolean DEBUG = false;

    public static final int PHONE_PAGE_NAVIGATION_INTRO = 0;
    public static final int PHONE_PAGE_NAVIGATION_WATCH_CONNECTION = 1;
    public static final int PHONE_PAGE_NAVIGATION_ACTIVITY_LOG = 2;
    public static final int PHONE_PAGE_NAVIGATION_EXPERIMENTER_SETTINGS = 3;

    public static final String CAPABILITY_FAST_ACCEL = "FIGLAB_FASTACCEL_02"; // Change this??
    public static final String PATH_CONNECTED_STATUS = "/status";

    public static final int WATCH_PAGE_DEBUG =0;
    public static final int WATCH_PAGE_STANDBY = 1;
    public static final int WATCH_PAGE_PROMPT = 2;
    public static final int WATCH_PAGE_HAND_ACTION = 3;
    public static final int WATCH_PAGE_BODY_ACTION = 4;
    public static final int WATCH_PAGE_OPEN_PHONE = 5;
    public static final int WATCH_PAGE_THANK_YOU = 6;
    public static final int WATCH_PAGE_TEST_CONNECTION = 7;
    public static final int WATCH_PAGE_SUCCESS = 8;

    /////////////////////////////////
    // Status-Related Messages
    /////////////////////////////////
    public static final String KEY_STATUS = "key-status";
    public static final int CONNECTED_STATUS_SUCCESS = 1;
    public static final int CONNECTED_STATUS_START_STREAMING = 2;
    public static final int CONNECTED_STATUS_STOP_STREAMING = 3;

    // Activity-Logging Related Messages
    public static final int ACTIVITY_LOG_OPEN_PHONE = 4;
    public static final int ACTIVITY_LOG_CUSTOM_LABEL_SAVED = 5;
    public static final int ACTIVITY_LOG_CUSTOM_LABEL_CANCELLED = 6;
    public static final int ACTIVITY_LOG_LABEL_SUCCESS = 7;
    public static final int ACTIVITY_LOG_LABEL_IGNORED = 16;
    public static final int ACTIVITY_LOG_LABEL_ILL_DEFINED = 21;
    public static final int PHONE_ACK = 8;
    public static final int CONNECTION_DESTROYED = 9;
    public static final int CONNECTED_STATUS_START_TEST = 10;
    public static final int CONNECTED_STATUS_STOP_TEST = 11;
    public static final int ACTIVITY_STANDBY = 12;
    public static final int CHECK_CUSTOM_LABEL_REQUESTS = 13;
    public static final int RECONNECT = 14;
    public static final int HEARTBEAT = 15;
    public static final int BATTERY_LEVEL = 17;
    public static final int ACTIVITY_PHONE_HAND_ACTION_LABEL = 18;
    public static final int CLEAR_DATA = 19;
    public static final int ACTIVITY_PHONE_BODY_ACTION_LABEL = 20;

    //////////////////////////////
    // Application Events
    /////////////////////////////
    public static final int WATCH_APP_EVENTS = 22;
    public static final String KEY_WATCH_APP_EVENT_NAME = "key-watch-app-event-name";
    public static final String WATCH_APP_EVENT_DATA_COLLECTION_BEGIN = "DATA_COLLECTION_BEGIN";
    public static final String WATCH_APP_EVENT_DATA_COLLECTION_END = "DATA_COLLECTION_END";
    public static final String WATCH_APP_EVENT_MAIN_PROMPT_OPEN = "SCREEN_MAIN_PROMPT_OPEN";
    public static final String WATCH_APP_EVENT_HAND_ACTION_LABEL_OPENED = "SCREEN_HAND_ACTION_OPENED";
    public static final String WATCH_APP_EVENT_HAND_ACTION_LABEL_SELECTED = "SCREEN_HAND_ACTION_LABELED";
    public static final String WATCH_APP_EVENT_PHONE_PROMPT_OPENED = "SCREEN_PHONE_PROMPT_OPENED";
    public static final String WATCH_APP_EVENT_BODY_ACTION_LABEL_OPENED = "SCREEN_BODY_ACTION_OPENED";
    public static final String WATCH_APP_EVENT_BODY_ACTION_LABEL_SELECTED = "SCREEN_BODY_ACTION_LABELED";
    public static final String WATCH_APP_EVENT_LABEL_SUCCESS = "LABEL_SUCCESS";
    public static final String WATCH_APP_EVENT_LABEL_DISMISSED = "LABEL_DISMISSED";
    public static final String WATCH_APP_EVENT_LABEL_ILLDEFINED = "LABEL_ILLDEFINED";
    public static final String WATCH_APP_EVENT_DO_NOT_DISTURB = "DO_NOT_DISTURB";

    //////////////////////////////
    // Data Related Messages
    /////////////////////////////
    public static final String KEY_HAND_ACTION_LABEL = "key-hand-action";
    public static final String KEY_BODY_ACTION_LABEL = "key-body-action";
    public static final String KEY_CUSTOM_LABEL_REQUEST_STATUS = "key-custom-labels-request-status";
    public static final String KEY_BATTERY_LEVEL = "key-battery-level";
    public static final String OPEN_PHONE_ACTION_TYPE = "key-custom-action-type";
    public static final String OPEN_PHONE_HAND_ACTION = "key-hand-action";
    public static final String OPEN_PHONE_BODY_ACTION = "key-body-action";

    /////////////////////////////
    // Experimenter Settings
    ////////////////////////////
    public static final String EXPERIMENTER_SECTION_PASSWORD = "ppp"; //"ilovescotthudson";
    public static final String SHAREDPREF_KEY_HAND_ACTION_LIST = "key-sharedpref-hand-action";
    public static final String SHAREDPREF_KEY_BODY_ACTION_LIST = "key-sharedpref-body-action";

    public static final String SHAREDPREF_EXPERIMENT_USER_ID = "key-sharedpref-experiment-userid";
    public static final String SHAREDPREF_EXPERIMENT_POLLING_FREQUENCY = "key-sharedpref-experiment-polling-frequency";
    public static final String SHAREDPREF_EXPERIMENT_CAPTURE_DURATION = "key-sharedpref-experiment-capture-duration";
    public static final String SHAREDPREF_EXPERIMENT_RUNNING = "key-sharedpref-experiment-running";

    public static final int SOUNDSTREAMER_SAMPLING_RATE = 16000;
    public static final int SOUNDSTREAMER_BUFFER_CHUNK_SIZE = 16000;

    //////////////////////////
    // Compensation Related
    //////////////////////////
    public static float COMPENSATION_PER_LABEL = 0.25f; // $0.25 per label
    public static final int MAX_LABELS_PER_DAY = 60;
    public static final String SHAREDPREF_EXPERIMENT_GLOBAL_COMPENSATION_COUNT = "key-sharedpref-global-compensation-count";
    public static final String SHAREDPREF_EXPERIMENT_DAILY_COMPENSATION_COUNT = "key-sharedpref-daily-compensation-count";

    public static String toTitleCase(String s)
    {

        final String DELIMITERS = " '-/";

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c : s.toCharArray()) {
            c = (capNext)
                    ? Character.toUpperCase(c)
                    : Character.toLowerCase(c);
            sb.append(c);
            capNext = (DELIMITERS.indexOf((int) c) >= 0);
        }
        return sb.toString();
    }

    public static class DoNotDisturb
    {
        Calendar start;
        Calendar end;
        Date start_d;
        Date end_d;
        int hour_range = 10;
        SimpleDateFormat sdf;

        public DoNotDisturb()
        {
            start = Calendar.getInstance();
            start.set(Calendar.HOUR_OF_DAY,22);
            start.set(Calendar.MINUTE,0);
            end = (Calendar)start.clone();
            end.set(Calendar.MINUTE,0);
            end.add(Calendar.HOUR_OF_DAY,hour_range); // Add 10 Hours

            sdf = new SimpleDateFormat("HH:mm");
            try {
                start_d = sdf.parse(String.format("%s:%01d", start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE)));
                end_d = sdf.parse(String.format("%s:%01d", end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE)));
            } catch (ParseException e) {
                e.printStackTrace();;
            }

        }

        public boolean willDisturb(Calendar cal)
        {
            Date test_d;
            boolean isSplit = false, isWithin = false;
            try {
                test_d = sdf.parse(String.format("%s:%01d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
                isSplit = (end_d.compareTo(start_d) < 0);
                if (isSplit)  {
                    isWithin = (test_d.after(start_d) || test_d.before(end_d));
                }
                else
                {
                    isWithin = (test_d.after(start_d) && test_d.before(end_d));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return isWithin;
        }
    };

    static DoNotDisturb doNotDisturb = new DoNotDisturb();
    public static boolean WillDisturb()
    {
        Calendar c = Calendar.getInstance();
        return doNotDisturb.willDisturb(c);
    }

}