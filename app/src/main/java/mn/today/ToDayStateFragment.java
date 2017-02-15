package mn.today;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class ToDayStateFragment extends Fragment {
    private ElementTimer elementTimer;
    private static ProgressBar progressBar;
    private ToDayElement element;
    private int progress;
    private TextView timeDisplay;
    private OnFragmentSelectedListener mCallback;
    private OnDataPass dataPasser;
    private Context mContext;
    private String activityStateFlag;
    private NotificationCompat.Builder mNotify;
    private int overTime; //millis

    /**
     * Interface to allow onClick between FlowStateActivity and Fragments
     * Must be implemented.
     */
    public interface OnFragmentSelectedListener {
        void onNextSelected(View view);
        void onMoreTimeSelected(View view);
    }

    /**
     * Interface to allow data passing between FlowStateActivity and Fragments
     * Must be implemented.
     */
    public interface OnDataPass {
        void onDataPass(Bundle b, int elementNumber);
    }


    /**
     * Passes a bundle key:value pair with the elementNumber by calling
     * the implemented onDataPass Inteface method in the parent activity
     *
     * @param b , the bundle containing the k:v pair
     * @param elementNumber , the current location of the element in the flow
     */
    private void passData(Bundle b, int elementNumber) {
        dataPasser.onDataPass(b, elementNumber);
    }


    /**
     * Adds completed time to an overflow variable overTime.
     *
     * Resets progress bar, begins a new timer with the
     * @param minsToExtend
     */
    public void extendTime(int minsToExtend) {
        overTime = overTime + elementTimer.getTimeFinishedInMilliSecs();
        int millisToExtend = AppUtils.minsToMillis(minsToExtend);


        progress = AppUtils.millisToSecs(millisToExtend);

        progressBar.setMax(progress);
        progress = progressBar.getMax();
        progressBar.setProgress(progress);

        elementTimer= new ElementTimer(millisToExtend, 1000);
        startTimerOnUi();

        Animation fadeout = AnimationUtils.loadAnimation(getActivity(),android.R.anim.fade_out);
        TextView notFinished = (TextView) getView().findViewById(R.id.fragment_fe_not_finished);
        notFinished.startAnimation(fadeout);
        notFinished.setVisibility(View.INVISIBLE);
    }
    /**
     * Cancels the running UI Timer, creates a bundle key:value pair
     * with:
     *
     * key = element location in flow
     * value = time taken to finish the element.
     *
     * then passes the data via helper passData
     */
    public void cancelTimerAndPassData(boolean overTimeFlag) {
        if (overTimeFlag == AppConstants.FS_OVERTIME_FALSE) {
            if (element.getLocation()<0) {
                // Do not pass Data
                elementTimer.cancel();
            } else {
                Bundle b = new Bundle();
                b.putInt(
                        String.valueOf(element.getLocation()),
                        elementTimer.getTimeFinishedInMilliSecs()
                );
                passData(b, element.getLocation());
                elementTimer.cancel();
            }

        } else {
             /* Overtime is true therefore return overtime */
            if (element.getLocation()<0) {

                elementTimer.cancel();
            } else {
                overTime = overTime + elementTimer.getTimeFinishedInMilliSecs();
                Bundle b = new Bundle();
                b.putInt(
                        String.valueOf(element.getLocation()),
                        overTime
                );
                passData(b, element.getLocation());
                elementTimer.cancel();
            }
        }


    }


    /**
     * Creates Fragment View containing progress bar, element name and options for continuing to next
     * fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_running_element, container, false);

        TextView name = (TextView) view.findViewById(R.id.fragment_fe_task_name);
        timeDisplay = (TextView) view.findViewById(R.id.fragment_fe_time_display);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        final TextView next = (TextView) view.findViewById(R.id.fragment_fe_next);

        element = (FlowElement)getArguments().get(AppConstants.FRAGMENT_FLOW_STATE);
        assert element != null;

        progress = AppUtils.millisToSecs(element.getTimeEstimate());

            /* onTick occurs every 1000ms (1s), therefore need progress to tick at that rate
               but because using progress--, need to convert the millis to seconds to subtract
             */
        progressBar.setMax(progress);

        name.setText(
                element.getElementName()
        );

        progress = progressBar.getMax();
        progressBar.setProgress(progress);

        elementTimer= new ElementTimer(element.getTimeEstimate(), 1000);
        activityStateFlag=AppConstants.FS_UI_ACTIVE;

        startTimerOnUi();


        new Runnable() {

            @Override
            public void run() {
                new CountDownTimer(2500,1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        if (getActivity()!=null){
                            progressBar.setProgress(0);
                            Animation fadeinAnimation = AnimationUtils.loadAnimation(getActivity(),android.R.anim.fade_in);
                            next.setAnimation(fadeinAnimation);
                            next.setVisibility(View.VISIBLE);
                        } else {
                            next.setVisibility(View.VISIBLE);
                        }
                    }
                }.start();
            }
        }.run();

        return view;

    }


    /**
     * Creates new instance of a fragment with blank bundle.
     * @param e
     * @return
     */
    public static FlowStateFragment newInstance(FlowElement e) {
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.FRAGMENT_FLOW_STATE, e);
        FlowStateFragment fragment = new FlowStateFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Starts the Element Timer on the UI Thread.
     */
    private void startTimerOnUi() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run () {
                elementTimer.start();
            }
        });

    }

    /**
     * Ensures that all interface callbacks are assigned context
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Ensures container activity implements callback interface!
        try {
            mCallback = (OnFragmentSelectedListener) context;
            dataPasser = (OnDataPass) context;
            this.mContext = context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement the appropriate interface");
        }
    }

    /**
     * Blank constructor
     */
    public FlowStateFragment() {

    }

    /**
     * Basic CountDownTimer with some additional properties for determining time remaining and
     * passing data regarding time between multiple activities.
     */
    class ElementTimer extends CountDownTimer {
        public int getTimeFinishedInMilliSecs() {
            return (int) (timeStart-timeRemaining);
        }
        public long getTimeRemainingMilliSecs() {
            return timeRemaining;
        }
        long timeStart;
        long timeRemaining;
        private NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        /**
         * Reduces Progress Bar and sets text to current time remaining
         *
         * @param millisUntilFinished
         */
        @Override
        public void onTick(long millisUntilFinished) {
            if (activityStateFlag.equals(AppConstants.FS_NOTIFICATION_ACTIVE)) {
                mNotifyMgr.notify(
                        AppConstants.FLOW_STATE_NOTIFICATION_ID,
                        mNotify
                                .setContentText(
                                        "You've got " +  AppUtils.buildTimerStyleTime(millisUntilFinished) + " left in " + element.getElementName()
                                )
                                .build()
                );
            }

            timeRemaining=millisUntilFinished;

            progressBar.setProgress(--progress);

            timeDisplay.setText(AppUtils.buildTimerStyleTime(millisUntilFinished));


        }

        /**
         * Produces animation, sets text to finish
         */
        @Override
        public void onFinish() {
            if (activityStateFlag.equals(AppConstants.FS_NOTIFICATION_ACTIVE)) {
                mNotifyMgr.notify(
                        AppConstants.FLOW_STATE_NOTIFICATION_ID,
                        mNotify
                                .setContentText(
                                        getString(R.string.fs_task_finish_msg)
                                )
                                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                                .build()
                );
            }
            progressBar.setProgress(0);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
            timeDisplay.setText(getString(R.string.fs_task_finish_msg));
            timeDisplay.setAnimation(fadeInAnimation);

            TextView notFinished = (TextView) getView().findViewById(R.id.fragment_fe_not_finished);
            notFinished.setVisibility(View.VISIBLE);
            notFinished.startAnimation(fadeInAnimation);
        }

        public ElementTimer (long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.timeStart=millisInFuture;
        }
    }

    /**
     * Cancels timer activity in fragment if FlowState is exited early.
     *
     */
    public void notifyBackPressed() {
        elementTimer.cancel();
    }


    /**
     * Creates reference to passed notification object for use in the timer subclass
     * Sets notification flag active for notifyDataSetUpdated.
     *
     * @param unbuiltNotify
     */
    public void notificationsActive(NotificationCompat.Builder unbuiltNotify, String inState) {
        mNotify = unbuiltNotify;
        this.activityStateFlag=inState;
    }

    public void uiActive(String inState) {
        NotificationManager mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.activityStateFlag=inState;
        mNotifyMgr.cancel(AppConstants.FLOW_STATE_NOTIFICATION_ID);
            /* When UI is active, a notification should not be present */
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("RESUME", "progress is: " + progress);

        progressBar.invalidate();

        Log.d("RESUME", "getMax is: " + String.valueOf(progressBar.getMax()));

        progressBar.setProgress(0);
        progressBar.setProgress(progress);

        this.uiActive(activityStateFlag);
        /* Displays updated progress bar with time remaining */

    }
}
