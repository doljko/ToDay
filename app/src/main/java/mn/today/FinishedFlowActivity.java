package mn.today;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class FinishedFlowActivity extends AppCompatActivity {

    private String[] deleteMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished_flow);

        Toolbar toolbar = (Toolbar) findViewById(R.id.finished_flow_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        String timeComplete = getIntent().getStringExtra(AppConstants.EXTRA_FORMATTED_TIME);
        int millisInFlow = getIntent().getIntExtra(AppConstants.EXTRA_MILLIS_IN_FLOW,0);

        // Gets the Flow Manager Util saved in TheHubActivity from Complex Preferences

        TextView msg = (TextView)findViewById(R.id.praise_msg);
        TextView complete = (TextView) findViewById(R.id.flow_name);
        TextView time = (TextView) findViewById(R.id.complete_time);

        AppDataManager appData = new AppDataManager(this);
        ExportDataManager statsData = new ExportDataManager(this);

        Flow finishedFlow = appData.load(getIntent().getStringExtra(AppConstants.EXTRA_PASSING_UUID));
        finishedFlow.addCompletionToken();
        finishedFlow.addToLifeTimeStats(millisInFlow);

        String[] array = this.getResources().getStringArray(R.array.praise_msg);
        String randomStr = array[new Random().nextInt(array.length)];

        msg.setText(randomStr);
        complete.setText(finishedFlow.getName() + " was finished in:");
        time.setText(timeComplete);

        appData.overwrite(finishedFlow.getUuid(), finishedFlow);

        String[] exportData = prepareCSVExport(finishedFlow, millisInFlow);

        statsData.saveToCSV(exportData);

    }

    private String[] prepareCSVExport(Flow finishedFlow, int actualMillisInFlow) {
        /*
            ArrayList[0] = flowName;
            ArrayList[1] = childrenCount
            ArrayList[2] = Estimated Hours
            ArrayList[3] = Estimated Minutes
            ArrayList[4] = completion tokens
            ArrayList[5] = life time in flow (hrs)
         */
        ArrayList<String> exportData = finishedFlow.buildStatsExportList();

        /* Adds actual hours taken to complete flow to export data */
        exportData.add(
                String.valueOf(
                        AppUtils.calcHours(actualMillisInFlow)
                )
        );
        /* Adds actual minutes taken to complete flow to export data */
        exportData.add(String.valueOf(
                AppUtils.calcRemainderMins(actualMillisInFlow)
                )
        );

        /* Adds seconds taken to complete flow to export data */
        exportData.add(String.valueOf(
                AppUtils.calcRemainderSecs(actualMillisInFlow)
                )
        );


        return exportData.toArray(new String[exportData.size()]);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void completeRun(View v) {
        onBackPressed();
    }

    public void repeatRun(View v) {
        AppDataManager util = new AppDataManager(this);
        Flow flowToRepeat = util.load(getIntent().getStringExtra(AppConstants.EXTRA_PASSING_UUID));

        Intent in = new Intent(this, FlowStateActivity.class);
        in.putExtra(AppConstants.EXTRA_PASSING_UUID,flowToRepeat.getUuid());

        startActivity(in);
    }

}
