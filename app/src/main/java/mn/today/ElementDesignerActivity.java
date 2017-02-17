package mn.today;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class ElementDesignerActivity extends AppCompatActivity {

    // The Element Designer is the resource provider, SandBoxActivity is the blacksmith!
    private Spinner selectTime;
    private EditText nameInput;
    private EditText timeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_element_designer);

        nameInput = (EditText)findViewById(R.id.designer_name_input);
        timeInput = (EditText)findViewById(R.id.designer_time_input);
        selectTime = (Spinner)findViewById(R.id.designer_select_time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.select_time,android.R.layout.simple_spinner_item);

        //Specifying each layout for the dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        selectTime.setAdapter(adapter);

        setClickListeners();



    }

    private void setClickListeners() {
        selectTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /** Confirms completion of a Flow Element by validating input,
     *  instantiating a new FlowElement object, and passing the
     *  data back to the previous activity.
     * @param view the checkmark clicked in view
     */
    public void doneElement(View view) {
        String elementName = nameInput.getText().toString();
        String elementTime = timeInput.getText().toString();
        String timeUnits = selectTime.getSelectedItem().toString();


        if (!elementName.equals("") && !elementTime.equals("")) {

            ToDayElement newElement = buildNewElement(elementName,elementTime,timeUnits);


            Intent returnData = new Intent();
            returnData.putExtra(AppConstants.EXTRA_ELEMENT_PARCEL, newElement);
            setResult(RESULT_OK, returnData);
            finish();

        } else {
            Toast.makeText(ElementDesignerActivity.this, R.string.designer_text_validation_msg, Toast.LENGTH_LONG).show();
        }
    }

    private ToDayElement buildNewElement(String elementName, String elementTime, String timeUnits) {
        if (timeUnits==null) {
            timeUnits = AppConstants.UNIT_MINUTES;
        }

        int timeInMillis=60000;

        switch (timeUnits) {

            case AppConstants.UNIT_MINUTES:
                timeInMillis =
                        AppUtils.minsToMillis(
                                Integer.parseInt(elementTime)
                        );
                break;

            case AppConstants.UNIT_HOURS:
                timeInMillis =
                        AppUtils.hrsToMillis(
                                Integer.parseInt(elementTime)
                        );
                break;
            default:
                break;
        }

        return new ToDayElement(elementName, timeInMillis, timeUnits);
    }

    /**
     * On "X" pressed, delete the element being created and return to the previous screen
     */
    public void forgetElement(View view) {
        onBackPressed();
    }

    /**
     * On Back Pressed, delete the current element being made and return to previous screen
     */
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED,null);
        super.onBackPressed();
    }
}