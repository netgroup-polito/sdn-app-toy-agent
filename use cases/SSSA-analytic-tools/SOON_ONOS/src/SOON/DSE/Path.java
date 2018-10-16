
package DSE;

import java.util.ArrayList;


public class Path {
    public ArrayList<Segment> segments;
    public int id;
    public Path(ArrayList<Segment> segments, int id){
        this.segments=segments;
        this.id=id;
    }
}
