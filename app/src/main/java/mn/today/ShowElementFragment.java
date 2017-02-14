package mn.today;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */



public class ShowElementFragment extends Fragment {

    private onEditPasser mEditCallback;
    private Context mContext;
    private EditText changeName;
    private EditText changeTime;
    private TextView originalNotes;
    private EditText changeNotes;
    private TextView originalName;
    private TextView originalTime;
    private ToggleButton units;
    private Button finishedBut;

    public ToDayElement getCurrentElement() {
        return currentElement;
    }

    private ToDayElement currentElement;
    /**
     * The fragment argument representing the section number for this
     * fragment.*/

    public ShowElementFragment() {
    }// Blank Constructor

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ShowElementFragment newInstance(ToDayElement inElement) {
        ShowElementFragment fragment = new ShowElementFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.EXTRA_ELEMENT_PARCEL, inElement);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_element, container, false);

        units = (ToggleButton) rootView.findViewById(R.id.fragment_se_units_toggle);
        originalName = (TextView) rootView.findViewById(R.id.switcher_se_name_TV);
        originalTime = (TextView) rootView.findViewById(R.id.switcher_se_time_TV);
        originalNotes = (TextView) rootView.findViewById(R.id.switcher_se_notes_TV);
        changeName = (EditText) rootView.findViewById(R.id.switcher_se_name_ET);
        changeTime = (EditText) rootView.findViewById(R.id.switcher_se_time_ET);
        changeNotes = (EditText) rootView.findViewById(R.id.switcher_se_notes_ET);

        finishedBut = (Button) rootView.findViewById(R.id.fragment_se_finished_but);

        currentElement = getArguments()
                .getParcelable(AppConstants.EXTRA_ELEMENT_PARCEL);


        units.setClickable(false);

        originalName.setText(currentElement.getElementName());

        originalNotes.setText(String.valueOf(currentElement.getElementNotes()));

        setTimeTextOf(AppConstants.ORIGINAL_TIME);

        return rootView;
    }


    private void setTimeTextOf(String viewToSetText) {
        if (viewToSetText.equals(AppConstants.ORIGINAL_TIME)) {
            switch (currentElement.getTimeUnits()) {
                case AppConstants.UNIT_MINUTES:
                    originalTime.setText(
                            String.valueOf(
                                    AppUtils.convertToTotalMinutes(
                                            currentElement.getTimeEstimate()
                                    )
                            )
                    );
                    units.setChecked(false);
                    break;
                case AppConstants.UNIT_HOURS:
                    originalTime.setText(
                            String.valueOf(
                                    AppUtils.calcHours(
                                            currentElement.getTimeEstimate()
                                    )
                            )
                    );
                    units.setChecked(true);
                    break;

                default:
                    break;
            }
        } else if (viewToSetText.equals(AppConstants.CHANGE_TIME)) {
            switch (currentElement.getTimeUnits()) {
                case AppConstants.UNIT_MINUTES:
                    changeTime.setText(
                            String.valueOf(
                                    AppUtils.convertToTotalMinutes(
                                            currentElement.getTimeEstimate()
                                    )
                            )
                    );
                    units.setChecked(false);
                    break;

                case AppConstants.UNIT_HOURS:
                    changeTime.setText(
                            String.valueOf(
                                    AppUtils.calcHours(
                                            currentElement.getTimeEstimate()
                                    )
                            )
                    );
                    units.setChecked(true);
                    break;
                default:
                    break;
            }
        }

    }


    private String returnUnitValue(ToggleButton units) {
        if (units.isChecked()) {
            return AppConstants.UNIT_HOURS;

        } else {
            return AppConstants.UNIT_MINUTES;
        }
    }


    public void beginEdits() {

        changeName = AppUtils.setNameInputFilters(changeName);

        units.setClickable(true);

        changeNotes.setText(currentElement.getElementNotes());

        changeName.setText(currentElement.getElementName());

        setTimeTextOf(AppConstants.CHANGE_TIME);

        switchersShowNext();

        finishedBut.setVisibility(View.VISIBLE);
    }

    public void finishEdits(String status) {


        Bundle b = new Bundle();
        if (status.equals(AppConstants.STATUS_CANCELLED)) {

            switchersShowNext();

            finishedBut.setVisibility(View.INVISIBLE);

            passEdits(b, AppConstants.STATUS_CONFIRM_CANCEL);
            units.setClickable(false);

            OGGlassesAnimation(0);

        } else if (status.equals(AppConstants.STATUS_COMMIT_EDITS)) {

            if (changeName.getText().toString().equals("") ||
                    changeTime.getText().toString().equals("") ||
                    changeTime.getText().toString().equals("0")) {
                Toast.makeText(mContext, R.string.designer_text_validation_msg, Toast.LENGTH_LONG).show();

            } else {

            /* Pass data to parent activity */

                String timeUnits = returnUnitValue(units);
                int newMillisTime=1000*60;

                switch(timeUnits) {
                    case "minutes":
                        // Will be in the tens form since minutes split, c
                        newMillisTime = AppUtils.minsToMillis(
                                Integer.parseInt(
                                        changeTime.getText().toString()
                                )
                        );
                        break;
                    case "hours":
                        newMillisTime = AppUtils.hrsToMillis(
                                Integer.parseInt(
                                        changeTime.getText().toString()
                                )
                        );
                        break;
                    default:
                        break;

                }
                b.putString(AppConstants.KEY_NEW_NAME, changeName.getText().toString());
                b.putInt(AppConstants.KEY_NEW_TIME, newMillisTime);
                b.putString(AppConstants.KEY_NEW_UNITS, timeUnits);
                b.putString(AppConstants.KEY_NEW_NOTES, changeNotes.getText().toString());

                passEdits(b, AppConstants.STATUS_CONFIRM_EDITS);

            }


        }

    }

    private void switchersShowNext() {
        ViewSwitcher switcherName = (ViewSwitcher) getView().findViewById(R.id.fragment_se_switcher_name);
        ViewSwitcher switcherTime = (ViewSwitcher) getView().findViewById(R.id.fragment_se_switcher_time);
        ViewSwitcher switcherNotes = (ViewSwitcher) getView().findViewById(R.id.fragment_se_switcher_notes);
        switcherName.showNext();
        switcherTime.showNext();
        switcherNotes.showNext();
    }


    private void passEdits(Bundle b, String status){
        mEditCallback.onEditsPassed(b, status);
    }

    public interface onEditPasser {
        void onEditsPassed(Bundle b,String status);
    }

    private void OGGlassesAnimation(int onOffStatus) {
        ImageView ogMLG = (ImageView) getView().findViewById(R.id.fragment_se_og_glasses);
        Animation slideIn = AnimationUtils.loadAnimation(getActivity(),android.R.anim.slide_in_left);
        Animation fadeout = AnimationUtils.loadAnimation(getActivity(),android.R.anim.fade_out);
        switch (onOffStatus) {
            case 1:
                ogMLG.startAnimation(slideIn);
                ogMLG.setVisibility(View.VISIBLE);
                break;
            case 0:
                ogMLG.startAnimation(fadeout);
                ogMLG.setVisibility(View.INVISIBLE);
                break;
        }
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
            mEditCallback = (onEditPasser) context;
            this.mContext = context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement the appropriate interface");
        }
    }
}