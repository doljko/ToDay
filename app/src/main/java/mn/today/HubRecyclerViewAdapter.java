package mn.today;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class HubRecyclerViewAdapter extends RecyclerView.Adapter<HubRecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<ToDay> flowList;
    private onCardClickListener cardClickCallback;

    public HubRecyclerViewAdapter(Context mContext, ArrayList<ToDay> flowList) {
        this.mContext = mContext;
        this.flowList = flowList;
        this.cardClickCallback = (onCardClickListener) mContext;
    }

    /**
     * Represents each CardView present in the RecyclerView
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public View card;
        public ToDay flow;
        // The Flow this card represents
        public int position;
        // The position of this card in the recycler view
        public TextView name, elements, timeEstimate;
        public EditText rename;
        public ViewSwitcher switcher;

        public ViewHolder(View view) {
            super(view);
            card = view;
            name = (TextView) view.findViewById(R.id.item_flow_name);
            elements = (TextView) view.findViewById(R.id.item_element_count);
            timeEstimate = (TextView) view.findViewById(R.id.item_total_time);
            rename = (EditText) view.findViewById(R.id.hub_item_flow_rename);
            switcher = (ViewSwitcher) view.findViewById(R.id.hub_rename_switcher);
            /* Set up views for set operations */
        }

        /**
         * Sets the view up with the parameters
         *
         * @param inFlow
         * @param inPosition
         */
        public void prepare(ToDay inFlow, int inPosition) {
            /* since data is not avaliable until onBindViewHolder assign Flow and position here */
            this.flow = inFlow;
            this.position = inPosition;
            setOnClicks();
        }

        /**
         * Sets onClick and onLongClick actions using the parent activities' implemented
         * interface methods.
         */
        private void setOnClicks() {
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /* Passes Flow but passes the memory address of the childFlowElements
                     instead of the actual object containing the
                      */
                    cardClickCallback.onCardClick(flow);
                }
            });

            card.setLongClickable(true);

            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // View is the child view provided from AdapterView parent
                    return cardClickCallback.onCardLongClick(flow, position, card);
                }
            });


        }



    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_flow_hub, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ToDay flow = flowList.get(position);
        holder.name.setText(String.valueOf(flow.getName()));
        // String.valueOf() otherwise Resources$NotFoundException thrown
        holder.elements.setText(String.valueOf(flow.getChildCount()));

        holder.timeEstimate.setText(String.valueOf(flow.getFormattedTime()));
        holder.prepare(flow, position);


    }

    @Override
    public int getItemCount() {
        return flowList.size();
    }


    public interface onCardClickListener {
        void onCardClick(ToDay clickedFlow);
        boolean onCardLongClick(ToDay longClickedFlow, int cardPosition, View cardViewClicked);
    }
}
