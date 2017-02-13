package mn.today;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class SandBoxActivity extends AppCompatActivity
        implements MultiFunctionGridView.GridMultiSelectListener,
        MultiFunctionGridView.GridSortingListener {
    // The SandBox serves as a hub for the flows, with several functions:
    //
    ///// and the user can specify if they've finished the task move to next activity || need more time (+why) || ask help (slack)

    private Flow currentFlow;
    // Flow currently being worked on
    private AppDataManager util;
    private MultiFunctionGridView elementGridView;
    private SandBoxGridAdapter gridAdapter;
    private Toast currentToast = null;
    private LinkedList<FlowElement> gridContent;
    private String menuState;
    private SmallBang mSmallBang;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Not keeping Flow Object's in their own capsules
        setContentView(R.layout.activity_sand_box);
        util = new AppDataManager(this);



        Toolbar sbToolbar = (Toolbar) findViewById(R.id.sb_activity_toolbar);

        setSupportActionBar(sbToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        elementGridView = (MultiFunctionGridView) findViewById(R.id.drag_drop_gridview);

        gridContent = new LinkedList<>();


        menuState = AppConstants.MENU_ITEMS_NATIVE;

        ((MultiFunctionGridView) elementGridView).setGridFunctionState(AppConstants.GS_MCL_CHECKABLE);

        mSmallBang = SmallBang.attach2Window(this);
    }

    /* Allows the menu items to appear in the toolbar */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_sandbox, menu);

        MenuItem statistics = menu.findItem(R.id.action_flow_statistics);
        if (menuState.equals(AppConstants.MENU_ITEMS_PARTIAL)) {
            statistics.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }


    /* Invokes methods based on the icon picked in the toolbar */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* When the user selects one of the app bar items, the system
        calls your activity's onOptionsItemSelected() callback method,
        and passes a MenuItem object to indicate which item was clicked
        Requires menu items to use app:showAsAction="xyz"*/

        switch (item.getItemId()) {

            case R.id.action_reorder_elements:
                toggleSortingState();
                return true;


            case R.id.action_flow_statistics:
                View v = findViewById(R.id.action_flow_statistics);
                displayStatsPopup(v);
                return true;

            case R.id.action_send_feedback:
                AppUtils.sendFeedback(this);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void toggleSortingState() {
        /* If hit toggle and menu is set to native, want to change to partial action bar view */
        if (menuState.equals(AppConstants.MENU_ITEMS_NATIVE)) {
            menuState=AppConstants.MENU_ITEMS_PARTIAL;

            getSupportActionBar().setTitle(R.string.sb_sort_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            invalidateOptionsMenu();

            ((MultiFunctionGridView) elementGridView).setGridFunctionState(AppConstants.GS_DRAG_DROP);

            mSmallBang.bang(elementGridView,75,new SmallBangListener() {
                @Override
                public void onAnimationStart() {

                }

                @Override
                public void onAnimationEnd() {

                }
            });

            toggleFABVisibility(AppConstants.FAB_HIDE);
        } else if (menuState.equals(AppConstants.MENU_ITEMS_PARTIAL)) {
            menuState=AppConstants.MENU_ITEMS_NATIVE;

            getSupportActionBar().setTitle(currentFlow.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            invalidateOptionsMenu();
            ((MultiFunctionGridView) elementGridView).setGridFunctionState(AppConstants.GS_MCL_CHECKABLE);

            mSmallBang.bang(elementGridView,100,new SmallBangListener() {
                @Override
                public void onAnimationStart() {

                }

                @Override
                public void onAnimationEnd() {

                }
            });

            toggleFABVisibility(AppConstants.FAB_NATIVE);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        prepareContextualContent();

        setClickListeners();
    }

    /**
     * Prepares content based on the context (specific Flow) the activity is using
     *
     */
    private void prepareContextualContent() {
        currentFlow = util.load(getIntent().getStringExtra(AppConstants.EXTRA_PASSING_UUID));
        getSupportActionBar().setTitle(currentFlow.getName());

        gridContent.clear();

        gridContent.addAll(currentFlow.getChildElements());

        gridAdapter = new SandBoxGridAdapter(this, gridContent,3);
        // Passes the number of elements in the Flow's child elements to set the
        // Adapter's initial size
        elementGridView.setAdapter(gridAdapter);
    }

    /**
     * Sets the onClick action when an element in the grid is clicked.
     */
    private void setClickListeners() {

        elementGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                goShowElementActivity(position, v);

            }
        });
    }

    private void goShowElementActivity(int position, View clickedView) {


        Intent in = new Intent(SandBoxActivity.this, ShowElementActivity.class);
        in.putExtra(AppConstants.EXTRA_PASSING_UUID, currentFlow.getUuid());
        in.putExtra(AppConstants.EXTRA_POSITION_SELECTED, position);

        String transitionName = getString(R.string.transition_explode);

        // Define the view that the animation will start from

        ActivityOptionsCompat options =

                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        clickedView,   // Starting view
                        transitionName    // The String
                );

        ActivityCompat.startActivity(this, in, options.toBundle());

    }

    /** Launches a new Element Designer Activity waiting to receive a new
     *  FlowElement Object back as a Parcel
     *
     * @param view the FAB clicked to launch the function
     */
    public void createElement(View view) {
        Intent in = new Intent(SandBoxActivity.this, ElementDesignerActivity.class);
        startActivityForResult(in, AppConstants.DESIGNER_REQUEST_CODE);
        //Starts new activity waiting for the return data
    }

    /** Receives Parcel Object from previous Element Designer Activity
     *  and creates a reference object to point to the Parcel
     *
     * @param requestCode the code being requested
     * @param resultCode the code being recieved
     * @param data the Parcelled object stored as intent data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        FlowElement newElement;

        if(requestCode == AppConstants.DESIGNER_REQUEST_CODE && resultCode==RESULT_OK) {
            newElement = data.getParcelableExtra(AppConstants.EXTRA_ELEMENT_PARCEL);
            addElementToFlow(newElement);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Adds the newElement passed via parameter to the currentFlow's ArrayList
     *  while setting its FlowIndex and UpdatingSandBox Count
     *
     * @param newElement the FlowElement being saved
     */
    private void addElementToFlow(FlowElement newElement) {
        newElement.setLocation(currentFlow.getChildCount());

        gridContent.add(newElement);
        currentFlow.add(newElement);

        gridAdapter.notifyDataSetUpdated(gridContent);

        setClickListeners();

        util.overwrite(currentFlow.getUuid(),currentFlow);

    }

    /**
     * Prevents Toast Spam by canceling hanging Toasts before showing others
     *
     * @param text
     */
    private void showToast(String text)
    {
        if(currentToast != null)
        {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        currentToast.show();
    }



    public void goFlowState(View v) {
        if (currentFlow.getChildElements().isEmpty()) {
            this.showToast(
                    "Look's like there is no elements yet!"
            );
        } else {
            Intent in = new Intent(this, FlowStateActivity.class);
            in.putExtra(AppConstants.EXTRA_PASSING_UUID,currentFlow.getUuid());
            startActivity(in);
        }
    }

    /**
     * Deletes selected elements by getting the checked elements in the gridview,
     * iterating through each to add the element paired to the specific selection key to temp List.
     *
     * Removes the elements present in the temp List from the Flow's childElements List, updates the adapter
     * and saves the new flow.
     *
     * Provides additional option to undo via SnackBar
     */
    private void deleteSelection() {
        SparseBooleanArray selection = elementGridView.getCheckedItemPositions();

        final LinkedList<FlowElement> reference = new LinkedList<>(gridContent);
        final LinkedList<FlowElement> deletedChildElements = new LinkedList<>();

        try {
            for (int i=0; i<selection.size();i++) {
                deletedChildElements.add(gridContent.get(selection.keyAt(i)));
                /* If the element was checked it will be in the selection SBA,
                   so we add it to the collection of elements to remove but getting the element in the grid content
                   at the index = to the key found in the selection SBA
                 */
            }

            currentFlow.removeSelectedCollection(deletedChildElements);

            gridContent.clear();
            gridContent.addAll(currentFlow.getChildElements());
            gridAdapter.notifyDataSetUpdated(gridContent);

            util.overwrite(currentFlow.getUuid(), currentFlow);

        } catch (Exception e) {
            Toast.makeText(this,R.string.sandbox_delete_exception,Toast.LENGTH_LONG);
        }


        Snackbar bar = Snackbar.make(elementGridView, R.string.snackbar_sandbox_msg, Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentFlow.setChildElements(reference);
                        currentFlow.recalculateTotalTime();
                        gridContent.clear();
                        gridContent.addAll(currentFlow.getChildElements());
                        gridAdapter.notifyDataSetUpdated(gridContent);
                        util.overwrite(currentFlow.getUuid(),currentFlow);
                    }
                });


        bar.show();
    }

    @Override
    public void onBackPressed() {
        if (elementGridView.isEditMode()) {
            elementGridView.stopEditMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void reorderElements(int originalLocation, int desiredPosition) {

        currentFlow.reorderChildAt(originalLocation, desiredPosition);

        gridContent.clear();
        gridContent.addAll(currentFlow.getChildElements());

        gridAdapter.notifyDataSetUpdated(gridContent);
        util.overwrite(currentFlow.getUuid(),currentFlow);
    }

    private void toggleFABVisibility(String fabState) {
        FloatingActionButton flowState = (FloatingActionButton) findViewById(R.id.fab_flow_state);
        FloatingActionButton newElement = (FloatingActionButton) findViewById(R.id.fab_new_flow_element);


        switch (fabState) {
            case AppConstants.FAB_HIDE:
                AppUtils.animateViewRotateFade(flowState, AppConstants.ANIMATION_EXIT);
                AppUtils.animateViewRotateFade(newElement, AppConstants.ANIMATION_EXIT);
                break;
            case AppConstants.FAB_NATIVE:
                AppUtils.animateViewRotateFade(flowState, AppConstants.ANIMATION_ENTRY);
                AppUtils.animateViewRotateFade(newElement, AppConstants.ANIMATION_ENTRY);
                break;
            default:
                break;
        }

    }




    @Override
    public boolean createActionMenu(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gridview_context,menu);
        mode.setTitle("Select Items");
        mode.setSubtitle("One item selected");
        getSupportActionBar().hide();
        return true;
    }

    @Override
    public boolean actionMenuItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_selected_items:
                deleteSelection();
                mode.finish();
                return true;
            default:
                return true;
        }

    }

    @Override
    public void destroyActionMenu(ActionMode mode) {
        getSupportActionBar().show();

    }

    @Override
    public void updateActionMenuCheckState(ActionMode mode) {
        int selectCount = elementGridView.getCheckedItemCount();

        mode.setSubtitle("" + selectCount + " item(s) selected");

    }

    private void displayStatsPopup(View anchor) {
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window_flow_stats, null);

        RelativeLayout viewGroup = (RelativeLayout)  layout.findViewById(R.id.popup_stats);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(layout, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        TextView elementCount = (TextView) viewGroup.findViewById(R.id.stats_total_elements);
        TextView timeEstimate = (TextView) viewGroup.findViewById(R.id.stats_total_time);
        TextView completeCount = (TextView) viewGroup.findViewById(R.id.stats_times_complete);
        TextView lifeTimeInFlow = (TextView) viewGroup.findViewById(R.id.stats_life_time_spent_in_flow);

        elementCount.setText(
                String.valueOf(currentFlow.getChildCount())
        );

        timeEstimate.setText(
                String.valueOf(currentFlow.getFormattedTime())
        );

        completeCount.setText(
                String.valueOf(currentFlow.getCompletionTokens())
        );

        lifeTimeInFlow.setText(
                String.valueOf(
                        AppUtils.buildTimerStyleTime(currentFlow.getLifeTimeInFlow()))
        );

        popup.setBackgroundDrawable(new BitmapDrawable(null,""));
        popup.setFocusable(true);
        popup.showAsDropDown(anchor);

    }
}

