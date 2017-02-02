package mn.today.weather;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.Toast;

import mn.today.BuildConfig;
import mn.today.CalendarUtils;
import mn.today.R;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */
public class WeatherSyncService extends IntentService {

    /**
     * {@link SharedPreferences} string that packs previously synced weather for today
     * This will be updated every 24h
     */
    public static final String PREF_WEATHER_TODAY = "weatherToday";
    /**
     * {@link SharedPreferences} string that packs previously synced weather for tomorrow
     * This will be updated every 24h
     */
    public static final String PREF_WEATHER_TOMORROW = "weatherTomorrow";
    /**
     * {@link SharedPreferences} boolean that contains preference for showing weather
     */
    public static final String PREF_WEATHER_ENABLED = "weatherEnabled";
    public static final String TAG = WeatherSyncService.class.getName();
    private static final int[] HOUR_INDICES = new int[]{8, 14, 20};
    private static final String SEPARATOR = "|";
    // indicate if service is trigger while UI is active, not from alarm
    private static final String EXTRA_ACTIVE = "extra:active";

    private ForecastIOService mForecastService;

    /**
     * Gets previously synced weather information, triggers a new fetch if never synced
     * @param context    context
     * @return  previously synced weather information or null if never synced
     */
    @Nullable
    public static Weather getSyncedWeather(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String[] today =unpack(sp.getString(PREF_WEATHER_TODAY, null)),
                tomorrow = unpack(sp.getString(PREF_WEATHER_TOMORROW, null));
        // initiate a new remote fetch if some sync data are missing
        if (today == null || tomorrow == null) {
            Toast.makeText(context, R.string.updating_weather, Toast.LENGTH_SHORT).show();
            context.startService(new Intent(context, WeatherSyncService.class)
                    .putExtra(EXTRA_ACTIVE, true));
            return null;
        } else {
            return new Weather(today, tomorrow);
        }
    }

    /**
     * Packs retrieved forecast into string for storing into {@link SharedPreferences},
     * that can be later unpacked via {@link #unpack(String)}
     * @param forecast    retrieved forecast
     * @return  packed forecast string, or null if forecast is unavailable
     * @see {@link #unpack(String)}
     */
    private static String pack(ForecastIOService.Forecast forecast) {
        if (forecast == null || forecast.hourly == null ||
                forecast.hourly.data == null || forecast.hourly.data.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < HOUR_INDICES.length; i++) {
            int hourIndex = HOUR_INDICES[i];
            if (hourIndex >= forecast.hourly.data.length ||
                    forecast.hourly.data[hourIndex] == null) {
                sb.append(SEPARATOR);
            } else {
                sb.append(forecast.hourly.data[hourIndex].icon)
                        .append(SEPARATOR)
                        .append(forecast.hourly.data[hourIndex].temperature);
            }
            if (i < HOUR_INDICES.length - 1) {
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }

    /**
     * Unpacks a packed forecast string into an array of information
     * that has been previously packed via {@link #pack(ForecastIOService.Forecast)}
     * to construct a {@link Weather} instance
     * @param string    packed forecast string
     * @return  unpacked information, or null if no valid packed forecast string
     * @see {@link #pack(ForecastIOService.Forecast)}
     */
    private static String[] unpack(String string) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        return string.split("\\" + WeatherSyncService.SEPARATOR, -1);
    }

    public WeatherSyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        cancelScheduledAlarm();
        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PREF_WEATHER_ENABLED, false);
        if (!enabled) {
            persist(null, PREF_WEATHER_TODAY);
            persist(null, PREF_WEATHER_TOMORROW);
            return;
        }
        Location location = getLocation();
        if (location == null && intent.getBooleanExtra(EXTRA_ACTIVE, false)) {
            notifyLocationError();
        }
        long todaySeconds = CalendarUtils.today() / DateUtils.SECOND_IN_MILLIS,
                tomorrowSeconds = todaySeconds + DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS;
        persist(fetchForecast(location, todaySeconds), PREF_WEATHER_TODAY);
        persist(fetchForecast(location, tomorrowSeconds), PREF_WEATHER_TOMORROW);
        scheduleAlarm();
    }

    private void cancelScheduledAlarm() {
        // cancel a previously scheduled alarm if any
        PendingIntent alarmIntent;
        if ((alarmIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(this, WeatherSyncAlarmReceiver.class), PendingIntent.FLAG_NO_CREATE)) != null) {
            ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(alarmIntent);
        }
    }

    private void scheduleAlarm() {
        // schedule for update in next 24h
        ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis() + AlarmManager.INTERVAL_DAY,
                PendingIntent.getBroadcast(this, 0,
                        new Intent(this, WeatherSyncAlarmReceiver.class), 0));
    }

    private void notifyLocationError() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WeatherSyncService.this, R.string.error_location,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @VisibleForTesting
    @Nullable
    protected Location getLocation() {
        Location location = null;
        // try network provider first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            location = ((LocationManager) getSystemService(LOCATION_SERVICE))
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        // if not available try GPS provider
        if (location == null && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = ((LocationManager) getSystemService(LOCATION_SERVICE))
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return location;
    }

    @VisibleForTesting
    protected ForecastIOService getForecastService() {
        if (mForecastService == null) {
            mForecastService = new Retrofit.Builder()
                    .baseUrl(ForecastIOService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ForecastIOService.class);
        }
        return mForecastService;
    }

    private ForecastIOService.Forecast fetchForecast(Location location, long timeSeconds) {
        if (location == null) {
            return null;
        }
        try {
            return getForecastService()
                    .forecast(location.getLatitude(), location.getLongitude(), timeSeconds)
                    .execute()
                    .body();
        } catch (IOException e) {
            return null;
        }
    }

    private void persist(ForecastIOService.Forecast forecast, String preferenceKey) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(preferenceKey, pack(forecast))
                .apply();
    }

    interface ForecastIOService {
        String BASE_URL = "https://api.forecast.io/";

        @GET("forecast/" + BuildConfig.FORECAST_IO_API_KEY +
                "/{latitude},{longitude},{time}?exclude=currently,daily,flags")
        Call<Forecast> forecast(@Path("latitude") double latitude,
                                @Path("longitude") double longitude,
                                @Path("time") long timeSeconds);

        class Forecast {
            Hourly hourly;
        }

        class Hourly {
            DataPoint[] data;
        }

        class DataPoint {
            String icon;
            float temperature;
        }
    }
}