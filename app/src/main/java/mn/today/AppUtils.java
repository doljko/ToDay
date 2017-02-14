package mn.today;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class AppUtils {

    /**
     * Calculates total minutes from millisecond input
     *
     * @param millis
     * @return
     */
    public static int convertToTotalMinutes(int millis) {
        return (millis/(1000*60));
    }

    private static int convertToTotalSeconds(int millis) {
        return (millis/(1000));
    }

    public static int calcRemainderSecs(int millis) {
        return convertToTotalSeconds(millis)%60;
    }

    /**
     * Calculates remaining minutes (ie. <60mins) from millis input
     *
     */
    public static int calcRemainderMins(int millis) {
        return convertToTotalMinutes(millis)%60;
    }

    /**
     * Calculates whole hours from millisecond input
     *
     * @param millis
     * @return
     */
    public static int calcHours(int millis) {
        return convertToTotalMinutes(millis)/60;
    }

    /**
     * Builds String output of time in the style of: hrs H mins M
     *
     * @param millis
     * @return
     */
    public static String buildCardViewStyleTime(int millis) {
        return String.valueOf(calcHours(millis)) +"H " + String.valueOf(calcRemainderMins(millis)) +"M";
    }

    /**
     * Builds String output of time in the style of: HH:MM:SS
     * @param millis
     * @return
     */
    public static String buildTimerStyleTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millis)
                ),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millis)
                )

        );
    }

    public static String buildStandardTimeOutput(int millis) {
        int hrs = calcHours(millis);
        int mins = calcRemainderMins(millis);
        if (hrs>0 && mins >=0) {
            return hrs + " hrs\n" + mins + " mins";
        } else if (hrs>0) {
            return hrs + " hours.";
        } else if (mins>=0) {
            return mins + " minutes";
        } else {
            return "";
        }
    }

    public static int minsToMillis(int mins) {
        return (mins*60*1000);
    }

    public static int hrsToMillis(int hrs) {
        return (hrs*60*60*1000);
    }


    public static int millisToSecs(int millis) {
        return (millis/1000);
    }

    public static void animateViewRotateFade(View v, String animationState) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
        rotate.setDuration(250);
        AnimatorSet animSetFS = new AnimatorSet();
        switch (animationState) {
            case AppConstants.ANIMATION_ENTRY:
                ObjectAnimator alphaEntry = ObjectAnimator.ofFloat(v, "alpha",0f, 1f);
                alphaEntry.setDuration(200);
                animSetFS.play(alphaEntry).before(rotate);
                animSetFS.start();
                break;
            case AppConstants.ANIMATION_EXIT:
                ObjectAnimator alphaExit = ObjectAnimator.ofFloat(v, "alpha",1f, 0f);
                alphaExit.setDuration(200);
                animSetFS.play(rotate).before(alphaExit);
                animSetFS.start();
                break;
        }
    }

    public static void animateViewPulse(View v, Context context) {
        Animation pulse = AnimationUtils.loadAnimation(context, R.anim.pulse);
        v.startAnimation(pulse);
    }

    /**
     * Adds input filters to supplied edit text to allow only:
     *  A-Z, a-z, 0-9, and special characters (%$!@)
     * @param viewToFilter
     * @return
     */
    public static EditText setNameInputFilters(EditText viewToFilter) {
        viewToFilter.setInputType(InputType.TYPE_CLASS_TEXT);
        viewToFilter.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(20)
        });
        return viewToFilter;
    }

    public static void setTimeInputFilters(EditText timeInputFilters) {
    }

    public static void sendFeedback(Context callingActivity) {
        Intent gmailIntent = new Intent(Intent.ACTION_SENDTO);
        // Hard coding classes is bad..
        gmailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        gmailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[] {
                        callingActivity.getResources().getString(R.string.email_pressurelabs)
                });
        gmailIntent.putExtra(Intent.EXTRA_SUBJECT, callingActivity.getResources().getString(R.string.feedback_subject_msg));
        gmailIntent.putExtra(Intent.EXTRA_TEXT,  callingActivity.getResources().getString(R.string.feedback_body_msg));

        try {
            callingActivity.startActivity(gmailIntent);
        } catch(ActivityNotFoundException ex) {
            try {
                callingActivity.startActivity(Intent.createChooser(gmailIntent, "Which app?"));
            } catch (Exception e) {
                Toast.makeText(callingActivity, R.string.feedback_failed_msg, Toast.LENGTH_SHORT).show();
            }
        }

    }

    public static void showMessage(Context ctx,String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }
}
