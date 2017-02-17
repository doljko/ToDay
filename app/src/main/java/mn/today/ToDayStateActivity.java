package mn.today;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyachi.stepview.HorizontalStepView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class ToDayStateActivity extends AppCompatActivity
        implements ToDayStateFragment.OnFragmentSelectedListener, ToDayStateFragment.OnDataPass {

    private ToDay parentFlow;
    private int currentElementPosition;
    private Integer[] millisInFlow;
    // Holds each currentElementPosition's completetion time matching to it's Flow Location
    private ToDayStateFragment fragment;
    private int flowStateFlag;
    private String activityStateFlag;
    private NotificationCompat.Builder mBuilder;
    private boolean overTimeFlag;
    private HorizontalStepView stepProgress;
    private List<String> stepViewContent;
    private int attentionIconPosition;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_state);

        parentFlow = new AppDataManager(this).load(getIntent().getStringExtra(AppConstants.EXTRA_PASSING_UUID));

        millisInFlow = new Integer[parentFlow.getChildElements().size()];

        overTimeFlag = AppConstants.FS_OVERTIME_FALSE;
        flowStateFlag = AppConstants.NOT_FINISHED;
        activityStateFlag = AppConstants.FS_UI_ACTIVE;
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.flowstate_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {

                return;
            }

            currentElementPosition = 0; //Location of starting element

            fragment = ToDayStateFragment.newInstance(
                    parentFlow.getChildAt(currentElementPosition)
            );


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction
                    .add(R.id.flowstate_fragment_container, fragment)
                    .commit();

        }
        stepViewContent = new ArrayList<>();
        generateNewStepViewContent();
        nextStepViewBatch();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void nextStepViewBatch() {
        stepProgress = (HorizontalStepView) findViewById(R.id.flowstate_step_view);
        attentionIconPosition = 0;
        stepProgress
                .setStepsViewIndicatorComplectingPosition(attentionIconPosition)
                .setStepViewTexts(stepViewContent)
                .setTextSize(16)
                .setStepsViewIndicatorCompletedLineColor(ContextCompat.getColor(this, android.R.color.black))
                .setStepsViewIndicatorUnCompletedLineColor(ContextCompat.getColor(this, R.color.black))
                .setStepViewComplectedTextColor(ContextCompat.getColor(this, android.R.color.black))
                .setStepViewUnComplectedTextColor(ContextCompat.getColor(this, R.color.black))
                .setStepsViewIndicatorCompleteIcon(ContextCompat.getDrawable(this, R.drawable.flag_black_48dp))
                .setStepsViewIndicatorDefaultIcon(ContextCompat.getDrawable(this, R.drawable.default_icon))
                // Consider changing to blank drawable?
                .setStepsViewIndicatorAttentionIcon(ContextCompat.getDrawable(this, R.drawable.attention));

        stepProgress.ondrawIndicator();
    }

    /**
     * Bleh.. Indexes
     * <p>
     * Generates the StepView's TextView content (ie. the elements' rank) in batches of 4.
     * If not enough elements are avail for a 4-sized batch, adjusts batch size
     * <p>
     * 1-indexed location    |_1__2__3__4_|  |_5__6__x__x_| << 4 Sized Batches, x = non existent element [size() and real life]
     * 0-indexed         [0][1][2][3]    [4][5][6][7]  <<
     */
    private void generateNewStepViewContent() {
        stepViewContent = new ArrayList<>();
        int batchSize = 4;

        int elementLocation = currentElementPosition + 1;
        // Gives non 0-index location value
        // currentElementPosition is based on a 0 index.

        if (currentElementPosition + batchSize > parentFlow.getChildCount()) {
            batchSize = (parentFlow.getChildCount() % batchSize);
            // determines remainder of elements that don't fit into standard 4 memeber batch
            for (int i = 0; i < batchSize; i++) {
                stepViewContent.add(String.valueOf(elementLocation++));
            }

            for (int i = 0; i < 4 - batchSize; i++) {
                stepViewContent.add("");
            }


            // This is hacky I know, but bleh.. the libs not perfect

        } else {
           /* Adds content to List */
            for (int i = 0; i < batchSize; i++) {
                stepViewContent.add(String.valueOf(elementLocation++));
            }
        }


    }

    private void incrementStepView() {

        attentionIconPosition++;
        stepProgress.setStepsViewIndicatorComplectingPosition(attentionIconPosition)
                .setStepViewTexts(stepViewContent);
        stepProgress.ondrawIndicator();

        if (attentionIconPosition % 4 == 0 & attentionIconPosition != 0) {
            generateNewStepViewContent();
            nextStepViewBatch();
            AppUtils.animateViewPulse(stepProgress, this);
        }
    }

    @Override
    public void onBackPressed() {


        new AlertDialog.Builder(this)
                .setTitle("Your current Flow will be cancelled.")
                .setCancelable(false)
                .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fragment.notifyBackPressed();
                        flowStateFlag = AppConstants.EARLY_EXIT;
                        buildCustomQuitToast(ToDayStateActivity.this).show();
                        ToDayStateActivity.super.onBackPressed();

                    }
                })
                .setNegativeButton("No Don't!", null)
                .show();


    }


    /**
     * Displays custom toast with random phrase from resource file.
     *
     * @param context
     */
    private Toast buildCustomQuitToast(Context context) {
        String[] array = context.getResources().getStringArray(R.array.quit_quotes);
        String randomStr = array[new Random().nextInt(array.length)];

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.quit_toast,
                (ViewGroup) findViewById(R.id.toast_layout_container));

        TextView text = (TextView) layout.findViewById(R.id.toast_fs_quit_quote);
        text.setText(randomStr);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        return toast;
    }


    /**
     * Retrieves the time taken to complete the task while creating a new fragment for the next task.
     * <p>
     * If there is no final task an exception is thrown and the Flow is completed
     *
     * @param v
     */
    @Override
    public void onNextSelected(View v) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        try {
            fragment.cancelTimerAndPassData(overTimeFlag);

            fragment = ToDayStateFragment.newInstance(
                    parentFlow
                            .getChildElements().get(
                            ++currentElementPosition
                    )
            );

            overTimeFlag = AppConstants.FS_OVERTIME_FALSE; // Resets for next elements

            incrementStepView();

            transaction.setCustomAnimations(
                    android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.flowstate_fragment_container, fragment)
                    .commit();


        } catch (IndexOutOfBoundsException e) {
                /* Index Out of Bounds Exception Thrown When Flow Ends */

            if (fragment != null) {
                flowStateFlag = AppConstants.FINISHED;
                transaction.remove(fragment);
                transaction.commit();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            }

        }
        if (flowStateFlag == AppConstants.FINISHED) {
            goToFinishScreen();
        }
    }


    /**
     * Parses recieved data and adds to the Integer[] tracking the amount of time taken for each
     * task to complete
     *
     * @param recievedData  , a bundle containing the desired completion time data
     * @param elementNumber , the location of the element in the flow and also the string used as a key
     */
    @Override
    public void onDataPass(Bundle recievedData, int elementNumber) {
        /* onDataPass is called after onNextSelected is called,
           therefore currentElementPosition is updated to the next element location
           while passData is sending bundle related to previous element
           therefore currentElementPosition-1 (basically a count)
         */

        millisInFlow[currentElementPosition] = recievedData.getInt(
                String.valueOf(elementNumber));
    }

    private void goToFinishScreen() {
        Intent i = new Intent(this, FinishedFlowActivity.class);
        i.putExtra(AppConstants.EXTRA_PASSING_UUID, parentFlow.getUuid());

        int timeInFlow = calculateTimeInFlow();

        i.putExtra(
                AppConstants.EXTRA_FORMATTED_TIME,
                AppUtils.buildTimerStyleTime(
                        timeInFlow
                )
        );

        i.putExtra(AppConstants.EXTRA_MILLIS_IN_FLOW, timeInFlow);

        finish();
        startActivity(i);
    }

    /**
     * Iterates through the Integer[] and returns a formatted String time output of the time
     * taken to complete all tasks in the flow
     *
     * @return
     */
    private int calculateTimeInFlow() {
        int time = 0;

        for (int i = 0; i <= millisInFlow.length - 1; i++) {
            time = time + millisInFlow[i];
        }

        return time;
    }

    /**
     * Creates dialog for user to input additional time
     * <p>
     * Adds flag to notify the fragment to pass the overTime value when cancelTimerPassData() method is called
     *
     * @param v
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onMoreTimeSelected(View v) {
        overTimeFlag = AppConstants.FS_OVERTIME_TRUE;
        final EditText inMinutes = new EditText(this);
        final AlertDialog.Builder customDialog = customDialog(inMinutes);

        customDialog.setPositiveButton("Lets Roll",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (inMinutes.getText().toString().equals("")) {
                            // Need to optimize this so that the dialog does NOT disappear and just display toast
                            Toast.makeText(ToDayStateActivity.this, "Zero minutes... you're messing with me!", Toast.LENGTH_LONG).show();

                        } else {
                            fragment.extendTime((int) Double.parseDouble(inMinutes.getText().toString()));

                            Toast.makeText(ToDayStateActivity.this, R.string.fs_more_time_confirm, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        customDialog.setNegativeButton("Nevermind",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

        customDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private AlertDialog.Builder customDialog(EditText inTime) {
        final EditText in = inTime;
        final AlertDialog.Builder newFlowDialog = new AlertDialog.Builder(ToDayStateActivity.this);
        //Sets up Layout Parameters
        LinearLayout layout = new LinearLayout(ToDayStateActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMarginStart(42);
        params.setMarginEnd(50);


        //Sets up length and 1 line filters
        in.setInputType(InputType.TYPE_CLASS_NUMBER);

        in.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(3)
        });

        //Adds the ET and params to the layout of the dialog box
        layout.addView(in, params);

        newFlowDialog.setTitle(R.string.fs_dialog_more_time_title);

        newFlowDialog.setView(layout);

        return newFlowDialog;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (flowStateFlag != AppConstants.FINISHED && flowStateFlag != AppConstants.EARLY_EXIT) {
            onPauseNotifier();
        }


    }

    /**
     * Sets up and sends out a notification to the user keeping track of current time in Flow
     * also notifies current fragment.
     */
    private void onPauseNotifier() {
        mBuilder = buildNotification();
        activityStateFlag = AppConstants.FS_NOTIFICATION_ACTIVE;
        fragment.notificationsActive(mBuilder, activityStateFlag);
        // Pass built notification to fragment
    }

    /**
     * Generates a notification with the pending intent to send the user to the Flow State at the current task
     * being completed
     *
     * @return
     */
    private NotificationCompat.Builder buildNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), ToDayStateActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(AppConstants.EXTRA_PASSING_UUID, parentFlow.getUuid());
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.flow_state_notify)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentIntent(intent)
                        .setContentTitle(getString(R.string.fs_notification_title))
                        .setContentText("In Flow State")
                        .setAutoCancel(false)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Gets an instance of the NotificationManager service

        return builder;
    }

    @Override
    protected void onResume() {
        activityStateFlag = AppConstants.FS_UI_ACTIVE;
        fragment.uiActive(activityStateFlag);
        super.onResume();

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ToDayState Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
