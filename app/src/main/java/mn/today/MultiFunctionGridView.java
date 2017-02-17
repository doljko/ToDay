package mn.today;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.askerov.dynamicgrid.DynamicGridView;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class MultiFunctionGridView extends DynamicGridView {
    private String gridState;
    private GridMultiSelectListener mMultiChoiceCallback;
    private GridSortingListener mReorderCallback;
    private Context mContext;
    private int startLocation;
    private int insertionLocation;

    public MultiFunctionGridView(Context context, String inState) {
        super(context);
        this.mContext = context;
        this.mReorderCallback = (GridSortingListener) context;
        this.mMultiChoiceCallback = (GridMultiSelectListener) context;
        this.gridState = inState;
    }

    public MultiFunctionGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mReorderCallback = (GridSortingListener) context;
        this.mMultiChoiceCallback = (GridMultiSelectListener) context;
    }

    public MultiFunctionGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.mMultiChoiceCallback = (GridMultiSelectListener) context;
        this.mReorderCallback = (GridSortingListener) context;
    }


    /**
     * The grid has multiple functional states:
     * --> Drag and Drop Mode
     * --> Selection Deletion Mode
     *
     * Sets the current functional state of the grid based on gridState input
     * @param status current stats of the grid
     */
    public void setGridFunctionState(String status) {

        if (status.equals(AppConstants.GS_DRAG_DROP)) {
            this.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
            this.setOnDropListener(new DynamicGridView.OnDropListener() {
                @Override
                public void onActionDrop()
                {
                    mReorderCallback.reorderElements(startLocation, insertionLocation);
                    stopEditMode();
                }
            });

            this.setOnDragListener(new DynamicGridView.OnDragListener() {
                @Override
                public void onDragStarted(int position) {
                    startLocation = position;
                }

                @Override
                public void onDragPositionsChanged(int oldPosition, int newPosition) {
                    insertionLocation = newPosition;
                }
            });
            this.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    startEditMode(position);
                    return true;
                }
            });

        } else {
            this.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
            this.setMultiChoiceModeListener(new MultiChoiceListener());
            this.setSelector(R.color.colorAccent);
        }

    }

    class MultiChoiceListener implements GridView.MultiChoiceModeListener {


        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mMultiChoiceCallback.updateActionMenuCheckState(mode);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return mMultiChoiceCallback.createActionMenu(mode, menu);

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {

                case R.id.action_delete_selected_items:
                    return mMultiChoiceCallback.actionMenuItemClicked(mode,item);

                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.

                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMultiChoiceCallback.destroyActionMenu(mode);
        }


    }


    public interface GridSortingListener {
        void reorderElements(int originalLocation, int insertionPosition);

    }

    public interface GridMultiSelectListener {
        boolean createActionMenu(ActionMode mode, Menu menu);
        boolean actionMenuItemClicked(ActionMode mode, MenuItem item);
        void destroyActionMenu(ActionMode mode);
        void updateActionMenuCheckState(ActionMode mode);
    }
}
