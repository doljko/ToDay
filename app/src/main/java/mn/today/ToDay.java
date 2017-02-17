package mn.today;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */

public class ToDay implements Parcelable {

    private String name;

    private int completionTokens;

    private LinkedList<ToDayElement> childToDayElements;
    // Keeps track of the current ToDayElements which belong to this ToDay

    private int totalTime=0;
    // Time is stored in Millis

    private int lifeTimeInToDay=0;

    private String uuid;

    /** Overloaded ToDay Object constructor.
     * @param name name of the ToDay object being instantiated
     * @param time total time estimate of the ToDay object
     */
    public ToDay(String name, double time) {
        this.name = name;
        this.totalTime = (int) time;
        this.completionTokens=0;
        this.childToDayElements = new LinkedList<>();
        this.uuid = UUID.randomUUID().toString();
    } // End of overload constructor


    /*~~~~~~~~~ Getters & Setters ~~~~~~~~~*/

    public int getLifeTimeInToDay() {
        return lifeTimeInToDay;
    }

    public void setLifeTimeInToDay(int lifeTimeInToDay) {
        this.lifeTimeInToDay = lifeTimeInToDay;
    }

    public String getUuid() {
        return uuid;
    }


    public LinkedList<ToDayElement> getChildElements() {
        return childToDayElements;
    }
    public void setChildElements(LinkedList<ToDayElement> newDataSet) {
        this.childToDayElements = newDataSet;
    }

    /** Sets the name for the ToDay
     * @param name desired name of the ToDay object
     */
    public void setName(String name) {
        this.name = name;
    }

    /** Gets the name of the ToDay
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /** Gets the current Element count for the ToDay
     * @return childToDayElements.size()
     */
    public Integer getChildCount() {
        return childToDayElements.size();
    }

    /** Gets the total estimated time for the ToDay
     * @return totalTime
     */
    public double getTime() {
        return totalTime;
    }

    public int getHours() {
        return AppUtils.calcHours(totalTime);
    }

    public int getMinutes() {
        return AppUtils.calcRemainderMins(totalTime);
    }
    /**
     * Returns H and M formatted time for the total ToDay Time
     * @return String, time
     */
    public String getFormattedTime() {
        // Total time includes hrs and minutes, int truncates the double
        return AppUtils.buildCardViewStyleTime(this.totalTime);
    }


    /**
     * Returns number of completion tokens in this ToDay
     * @return completionTokens, int
     */
    public int getCompletionTokens() {
        return completionTokens;
    }



    /*~~~~~~~~ Action Methods ~~~~~~~~~~*/

    @Override
    public boolean equals(Object o) {
        ToDay other = (ToDay) o;
        String otherUniqueId = other.getUuid();
        return this.uuid.equals(otherUniqueId);
    }

    /**
     * Reassigns Child Element Location Parameters to match their current location in the LinkedList.
     */
    public void reassignChildLocations() {
        for (int i=0; i<childToDayElements.size(); i++) {
            childToDayElements.get(i).setLocation(i);
        }
    }

    /**
     * Adds a completion token to the current ToDay
     */
    public void addCompletionToken(){
        completionTokens++;
    }


    /** Retrieves the ToDayElement at the specified index position within
     *  the ToDay's children ArrayList
     *
     * @param childLocation the index position which the ToDayElement is at
     * @return ToDayElement the element which has been found
     */
    public ToDayElement getChildAt(int childLocation) {
        return childToDayElements.get(childLocation);
    }

    /** Adds the ToDayElement received via Parameters to the ToDay's
     *  children ArrayList
     *
     * @param newElement the Element being added to the ToDay's ArrayList
     */
    public void add(ToDayElement newElement) {

        childToDayElements.add(newElement);

        totalTime = totalTime + newElement.getTimeEstimate();
    }

    public void reorderChildAt(int originalLocation, int newLocation) {
        ToDayElement target = this.childToDayElements.remove(originalLocation);

        this.childToDayElements.add(newLocation, target);

        this.reassignChildLocations();
    }


    /**
     * Removes the supplied collection of child elements to remove from this ToDay.
     *
     * * On avg case:
     *      O(n) of calculating totalTime of deletedChildElement
     *                          <
     *      O(n) recalculating totalTime from remaining elements
     *
     *
     * @param deletedChildElements
     */
    public void removeSelectedCollection(LinkedList<ToDayElement> deletedChildElements) {
        childToDayElements.removeAll(deletedChildElements);

        for (ToDayElement removed: deletedChildElements) {
            totalTime = totalTime - removed.getTimeEstimate();
        }

    }

    /**
     * Calculates the totalTime value from all elements in
     * the current ToDay
     */
    public void recalculateTotalTime() {
        totalTime=0;
        for (ToDayElement element: childToDayElements) {
            totalTime=totalTime + element.getTimeEstimate();
        }
    }


    /** Overriding of original toString() because its natural
     *  implementation is no bueno!
     *
     * @return ToDay
     */
    @Override
    public String toString() {
        return "ToDay{" +
                "childToDayElements=" + childToDayElements +
                ", completionTokens='" + completionTokens+
                ", uuid=" + uuid +
                ", name='" + name  +
                ", totalTime=" + totalTime +
                '}';
    }

    public String pingStats() {
        return "Number of Elements: " + this.getChildCount() +
                "\n Estimated Time: " + this.getFormattedTime() +
                "\n Times completed: " + this.getCompletionTokens();
    }

    public ArrayList<String> buildStatsExportList() {
        ArrayList<String> temp = new ArrayList<>();

        temp.add(this.getName());
        temp.add(String.valueOf(this.getChildCount()));
        temp.add(String.valueOf(this.getHours()));
        temp.add(String.valueOf(this.getMinutes()));
        temp.add(String.valueOf(this.getCompletionTokens()));
        temp.add(
                String.valueOf(
                        AppUtils.calcHours(
                                this.getLifeTimeInToDay()
                        )
                )
        );
        return temp;
    }

    public void addToLifeTimeStats(int statToAdd) {
        lifeTimeInToDay = lifeTimeInToDay + statToAdd;
    }
    /* ~~~~~~~~~~~~ PARCEL IMPLEMENTATION ~~~~~~~~~~*/
    // Parcel Implementation to pass data from the Stream to the Sandbox about
    // the current ToDay object.
    // Still need to make method to calculate the total time for the ToDay based on elements
    // The order of READING and WRITING is important (Read and write in same order)
    private ToDay(Parcel in) {
        String[] data = new String[4];
        // data[0] = name
        // data[1] = totalTime
        // data[2] = uuid
        // data[3] = completionTokens

        in.readStringArray(data);
        this.name = data[0];
        this.totalTime = Integer.parseInt(data[1]);
        this.uuid = data[2];
        this.completionTokens = Integer.parseInt(data[3]);
        in.readList(childToDayElements,getClass().getClassLoader());

          /* Similar implementation:
              this.name = parcel.readString();
              this.totalTime= parcel.readString();
              this.ToDayManagerIndex = parcel.readString();
              this.childToDayElements = parcel.readArrayList(null);
         */

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        /* Similar implementation:
            dest.writeString(this.name);
            dest.writeString(this.totalTime);
            dest.writeString(this.ToDayManagerIndex);
            dest.writeList(childToDayElements);
         */
        destination.writeStringArray(
                new String[] {
                        this.name,
                        String.valueOf(this.totalTime),
                        this.uuid,
                        String.valueOf(this.completionTokens)

                }
        );
        destination.writeList(this.childToDayElements);
        // Writes the childToDayElements to the parcel
    }

    public static final Parcelable.Creator<ToDay> CREATOR =
            new Parcelable.Creator<ToDay>() {

                @Override
                public ToDay createFromParcel(Parcel source) {
                    return new ToDay(source);
                    //Using Parcelable constructor
                }

                @Override
                public ToDay[] newArray(int size) {
                    return new ToDay[size];
                }
            };

    public ToDayElement find(ToDayElement elementToFind) {
        return this.childToDayElements.get(
                this.childToDayElements.indexOf(elementToFind)
        );
    }



}
