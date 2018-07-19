import java.util.*;
import java.io.*;
import java.math.*;

class Player {
    
    static int NUM_TRUCKS = 100;

    public static void main(String args[]) 
    {
        Scanner in = new Scanner(System.in);
        int boxCount = in.nextInt();
        float targetWeight = 0;
        float avBoxWeight = 0;
        float totalVol = 0;
        Box[] boxes = new Box[boxCount];
        Truck[] trucks = new Truck[NUM_TRUCKS];
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        for (int i = 0; i < boxCount; i++) 
        {
            float weight = in.nextFloat();
            float volume = in.nextFloat();
            totalVol += volume;
            targetWeight += weight;
            boxes[i] = new Box(weight, volume, i);
        }
        
        avBoxWeight = targetWeight/boxCount;
        targetWeight = targetWeight/NUM_TRUCKS;
       //OK lets create the carpark
        for(int i = 0; i < NUM_TRUCKS; i++)
        {
            trucks[i] = new Truck(i);
        }
        
        // Not needed in this implimentation BoxSort sorter = new BoxSort(); 
        boolean done = false;
        Arrays.sort(boxes); //sorts by weight
        int loopCount = 0;
        double lastDelta = 100;
        int[] output = new int[boxCount]; //To save our best result
        int heavyTrucks = 94;
        
        while(done == false)
        {
            done = false;
            for(Box el : boxes)
            {
                if(el.inTruck == true) continue; //done this one
                float lTruckW = 90000000;
                int lTruck = -1;
                for(int i = 0; i < NUM_TRUCKS; i++)
                {
                    if(trucks[i].totalWeight < lTruckW)
                        if(el.volume + trucks[i].getVol() < 100)
                        {
                            lTruckW = trucks[i].totalWeight;// get the lightest truck
                            lTruck = i;
                        }
                }
                trucks[lTruck].addBox(el);
                if(loopCount > 0)
                {
                    System.err.println("What???");
                }
                done = false; // if we added a box, we are not done.
            }
            
           
            for(int x = 0; x < NUM_TRUCKS; x++) // for each truck starting at the lightest
            {
                Truck temp1 = trucks[x];
                double temp1Delta = 100;
                
                for(int y = 0; y < temp1.getNumBoxes(); y++) //for every box on that truck
                {
                    temp1Delta = Math.abs(temp1.getWeight() - targetWeight);
                    if(temp1Delta < 0.19) continue; //if we have sorted this then don't worry
                    boolean boxDone = false;
                    //what will we have if we loose this box?
                    float temp1NewWeight = temp1.getWeight() - temp1.getBoxWeight(y);
                    float temp1NewVol = temp1.getVol() - temp1.getBoxVolume(y);
                    Box fromTemp1 = temp1.cargo.get(y);
                    float bestDelta = 100;
                    int xIndex = -1;
                    int yIndex = -1;
                    //vlues to store what we are swapping
                    for(int x1 = 99; x1 > heavyTrucks; x1--) // for every OTHER truck
                    {
                        if(boxDone == true) break;
                        if(x1 == x) continue; // no need to check ourselves
                        Truck temp2 = trucks[x1];
                        for(int y1 = 0; y1 < temp2.getNumBoxes(); y1++) // for every OTHER box)
                        {
                            float temp2NewVol = temp2.getVol() - temp2.getBoxVolume(y1);
                            if( (temp1NewVol + temp2.getBoxVolume(y1) ) <= 100) //if this box is small enough
                            {
                                //if it'll bring temp1 closer to the goal.
                                if( Math.abs( (temp1NewWeight + temp2.getBoxWeight(y1) )  - targetWeight) < temp1Delta)
                                {
                                    //AND if temp1 box will fit on the other truck
                                    if( (temp2NewVol + temp1.getBoxVolume(y) ) <= 100)
                                    {
                                        //IF this is the best so far
                                        if( Math.abs( (temp1NewWeight + temp2.getBoxWeight(y1) )  - targetWeight) < bestDelta)
                                        {
                                            //THEN we should save these these boxes
                                            bestDelta = Math.abs( (temp1NewWeight + temp2.getBoxWeight(y1) ) );
                                            xIndex = x1;
                                            yIndex = y1;
                                             
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    //Swap the boxes over.. as long as we found one
                    if(xIndex != -1)
                    {
                        Box fromTemp2 = trucks[xIndex].cargo.get(yIndex);
                                            
                        temp1.removeBox(y);
                        trucks[xIndex].removeBox(yIndex);
                                            
                        temp1.addBox(fromTemp2);
                        trucks[xIndex].addBox(fromTemp1);
                                            
                        done = false; 
                    }
                    else
                    {
                        Arrays.sort(boxes);
                    }
                }
            }
            loopCount++;
            
            /*
            Test to see if the difference of lightest to heaviest is smaller than last time
            */
            double h = 0;
            double l = 100000;
            int p = 0;
            for(Truck el: trucks)
            {
                double test = el.getWeight();
                if(test > targetWeight) p++;
                if( h < test) h = test;
                else if( l > test) l = test;
            }
            heavyTrucks = 99 - p;
            double delta = h - l;
            //Save our best result
            if( delta < lastDelta )
            {
                lastDelta = delta;
                for(int x = 0; x < NUM_TRUCKS; x++)
                {
                    for(Box el : trucks[x].cargo)
                    {
                        output[el.index] = x;
                    }
                }
                if( delta < 0.3) //JACKPOT!
                {
                    System.err.println("JACKPOT");
                    done = true; //kept from old code, just in case I change the nesting..
                    break;
                }
            }
            
            elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime > 48000)
            {
               done = true; // same, not technically needed, but if i change the nesting it will be
               break; 
            }
        }
    
        String outString = "";
        for(int i = 0; i < boxCount; i++)
        {
            outString += output[i];
            outString += " ";
        }
        System.out.println(outString);
    }
}

class Truck implements Comparable<Truck>
{
    int truckIndex;
    float totalWeight;
    boolean finished;
    boolean beenTried;
    LinkedList<Box> cargo;
    
    public Truck(int i)
    {
        cargo = new LinkedList<Box>();
        truckIndex = i;
        totalWeight = 0;
        finished = false;
        beenTried = false;
        return;
    }
    
    public float getBoxVolume(int i)
    {
        return cargo.get(i).volume;
    }
    
    public float getBoxWeight(int i)
    {
        return cargo.get(i).weight;
    }
    
    public float getRemainingVolume()
    {
        return (100 - this.getVol());
    }
    
    public float getVol()
    {
        float ret = 0;
        for(Box el : cargo)
        {
            ret += el.volume;
        }
        return ret;
    }
    
    public float getWeight()
    {
        float ret = 0;
        for(Box el : cargo)
        {
            ret += el.weight;
        }
        return ret;
    }
    
    public int getNumBoxes()
    {
        return cargo.size();
    }
    
    public int compareTo(Truck comparetruck) 
    {
        float thisWeight = this.totalWeight;
        float compWeight = comparetruck.totalWeight;
        
        if(thisWeight > compWeight) return 1;
        if(thisWeight < compWeight) return -1;
        else 
            return 0;
    }
    
    public boolean addBox(Box in) //returns false if we can add this box.
    {
        if (in == null)
        {
            System.err.println("prob NULL");
            return false;
        }
        
        if ((in.volume + this.getVol()) > 100) 
        {
            
            System.err.println("prob FULL");
            return false; //truck full
        }
        
        if (in.inTruck == true)
        {   
            System.err.println("prob IN TRUCK");
            return false; //already in a truck
        }
        
        cargo.add(in);
        this.totalWeight += in.weight;
        in.inTruck = true;
        return true;
    }
    
    public boolean removeBox(int i)
    {
        
        if(i > cargo.size()) 
        {
            System.err.println("box doesn't exist");   
            return false ;
        }
        
        Box ret = cargo.get(i);
        this.totalWeight -= ret.weight;
        ret.inTruck = false;
        this.cargo.remove(i);
        return true;
    }
    
    public void clearTruck()
    {
        for(Box el : cargo)
        {
            el.inTruck = false;
        }
        this.totalWeight = 0;
        this.finished = false;
        this.cargo.clear();
        return;
    }
}

class Box implements Comparable<Box>
{
    public float weight;
    public float volume;
    public int index;
    boolean inTruck; //has it been loaded
    
    @Override
    public int compareTo(Box comparebox) 
    {
        float thisWeight = this.weight;
        float compWeight = comparebox.weight;
        
        if(thisWeight > compWeight) return -1;
        if(thisWeight < compWeight) return 1;
        else 
            return 0;
    }
    
    public String toString()
    {
        String x = "weight " + weight + " volume " + volume + " index " + index;
        return(x);
    }
    
    public Box(float w, float v, int i)
    {
        weight = w;
        volume = v;
        index = i;
        inTruck = false;
        return;
    }
}

// Quicksort
/* OLD CODE. KEPT FOR REFERENCE
class BoxSort
{
    int partition(Box arr[], int low, int high)
    {
        float pivot = arr[high].volume; 
        int i = (low-1); // index of smaller element
        for (int j=low; j<high; j++)
        {
            // If current element is smaller than or
            // equal to pivot
            if (arr[j].volume <= pivot)
            {
                i++;
 
                // swap arr[i] and arr[j]
                Box temp;
                temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
 
        // swap arr[i+1] and arr[high] (or pivot)
        Box temp;
        temp = arr[i+1];
        arr[i+1] = arr[high];
        arr[high] = temp;
 
        return i+1;
    }
 
    void sort(Box arr[], int low, int high)
    {
        if (low < high)
        {
            // pi is partitioning index, arr[pi] is 
            //  now at right place 
            int pi = partition(arr, low, high);
 
            // Recursively sort elements before
            // partition and after partition
            sort(arr, low, pi-1);
            sort(arr, pi+1, high);
        }
    }
}
*/
