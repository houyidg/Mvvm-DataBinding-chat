package utils;

import android.os.Environment;


public class AppConfig {
//    public static final boolean DEBUG = true;
    public static final boolean DEBUG = false;
    public static final double BASE_RECONN_TIME = 2000.0D;
    public static final int MSG_TRY_COUNT = 3;//
    public static float CACHE_VOICE_MB_SIZE = 0.0F;
    public static int screenWidth=0;
    public static final String CHAT_GET_MSG_TYPE = "Audio";
    public static final int CHAT_LIST_COUNT = 20;
    public static final int PRE_LOAD_ROOM_MSGS_COUNT=60;
    public static final String CHAT_SHOW_LIKE_ME_COUNT = "10";
    public static int CURRENT_ROOM_CHAT_COUNT = 0;
    public static String CURRENT_ROOM_ID;
    public static final int EVENT_CARD_SHOW_CHAT_SIZE = 4;
    public static final float EVENT_PHOTO_SCALE = 2.666667F;
    public static int EVENT_PHOTO_WIDTH = 0;
    public static final String HOLD_MSG_ID = "hold_msg_id";
    public static final float MAX_RECORD_TIME = 60.0F;
    public static final String MEDIA_SUFFIX = ".amr";
    public static final String PARAMS_EVENT_ID = "params_event_id";
    public static final String PARAMS_PHOTO = "params_photo";
    public static final String PARAMS_EVENT_TITLE = "params_event_title";
    public static final String PATH_SAVE_AUDIO;
    public static final int PLAY_VOICE_MOST_TIME = 20;
    public static final String PLAY_VOICE_START_NAME = "http";
    public static final int PRE_LOAD_EVENT_SIZE = 3;
    public static float RV_CARD_HEIGHT = 1000F;
    public static float RV_SCROLL_TOP = 0.0F;
    public static int GENE_RV_ITEM_HEIGHT =0;
    public static float SHORTEST_PHOTO_WIDTH = 0.0F;
    public static float SelectedRvScale = 0.0F;
    public static final String TAG = "AppConfig";
    public static final int TRY_RECONNECT_COUNT = 3;
    public static final float VIEWPAGER_SCALE_VALUE = 0.25F;
    public static final long VOICE_PLAY_SCALE_TIME = 1500L;
    public static boolean eventsMsgListIsRefreshed = false;
    public static final String pushHub = "chat";
    public static final String pushMsgUrl = "https://event-chat.sparxo.com/";
    public static int visitedItemCount;
    public static int scanCodeRequestCode=1111;
    public static boolean isCheckVersion=false;

    static {
        CURRENT_ROOM_ID = "";
        CURRENT_ROOM_CHAT_COUNT = 20;
        PATH_SAVE_AUDIO = Environment.getExternalStorageDirectory().getPath() + "/Glood/cache/Audio/";
        CACHE_VOICE_MB_SIZE = 1000.0F;
        SHORTEST_PHOTO_WIDTH = 85.0F;
        EVENT_PHOTO_WIDTH = 0;
        SelectedRvScale = 1.3F;
        eventsMsgListIsRefreshed = false;
        visitedItemCount = 5;
    }
}