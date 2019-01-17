package bigdata;

import java.util.ArrayList;
import java.util.Collections;

public class HeightLevelFinder {
    private ArrayList<Integer> bounds;

    public HeightLevelFinder(ArrayList<Integer> b){
        bounds = b;
        if(!bounds.contains(0))
            bounds.add(0);
        Collections.sort(bounds);
    }

    public int whichLevelIs(int value){
        if(value == 0)
            return 0;
        int lower_bound;
        int higher_bound;
        for(int i = 0; i < bounds.size() - 1; i++){
            lower_bound = bounds.get(i);
            higher_bound = bounds.get(i+1);
            if(value > lower_bound && value <= higher_bound)
                return i+1;
        }
        return bounds.size(); // no bounds found, we're above all, return max
    }

    public int getNbLevels(){
        return bounds.size()+1;
    }
}
