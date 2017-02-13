package mn.today;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */


public class ToDayElement implements Parcelable {

    private String elementName;

    private String elementNotes;

    private int timeEstimate;
    // Time is stored in Millis

    private String timeUnits;

    private int location;

    public ToDayElement(String name, int timeEst, String units) {
        // Must make sure ToDayElement values cannot be null!
        this.elementName = name;
        this.timeEstimate = timeEst;
        this.timeUnits = units;
        this.elementNotes = "No notes.";
    } //End of constructor

    /*~~~~~ Getter and Setter Methods: ~~~~~*/

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public void setTimeEstimate(int timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    public void setTimeUnits(String timeUnits) {
        this.timeUnits = timeUnits;
    }

    public String getElementNotes() {
        return this.elementNotes;
    }

    /** Gets the name for ToDayElement
     * @return elementName
     */
    public String getElementName() {
        return elementName;
    }

    /** returns user's estimated completion time
     * @return timeEstimate time estimate
     */
    public int getTimeEstimate() {
        return timeEstimate;
    }

    /** returns the units the user entered
     * @return timeUnits units for the time
     */
    public String getTimeUnits() {
        return timeUnits;
    }

    /**
     * @return location
     */
    public int getLocation() {
        return location;
    }

    /** Sets the location in the flow array list
     * @param location the location to add
     */
    public void setLocation(int location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "\n#FLOW ELEMENT\n"
                + this.elementName + " "
                + "\nTime: " + this.getTimeEstimate()  + " "
                + "\nLocation " + this.getLocation() +"\n";
    }



    /* Parcel Implementation for Object Passing Between Activities! */
    private ToDayElement(Parcel in) {
        String[] data = new String[4];
        // To include: Id, Estimated Time, Task Name, E

        in.readStringArray(data);
        this.elementName = data[0];
        this.timeEstimate = Integer.parseInt(data[1]);
        this.timeUnits = data[2];
        this.elementNotes = data[3];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeStringArray(
                new String[] {
                        this.elementName,
                        String.valueOf(this.timeEstimate),
                        String.valueOf(this.timeUnits),
                        this.elementNotes
                }
        );
    }

    public static final Parcelable.Creator<ToDayElement> CREATOR =
            new Parcelable.Creator<ToDayElement>() {

                @Override
                public ToDayElement createFromParcel(Parcel source) {
                    return new ToDayElement(source);
                    //Using Parcelable constructor
                }

                @Override
                public ToDayElement[] newArray(int size) {
                    return new ToDayElement[size];
                }
            };

    public void setElementNotes(String elementNotes) {
        this.elementNotes = elementNotes;
    }
}
