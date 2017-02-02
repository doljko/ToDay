package mn.today.weather;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */
public class WeatherSyncAlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startWakefulService(context, new Intent(context, WeatherSyncService.class));
    }
}
