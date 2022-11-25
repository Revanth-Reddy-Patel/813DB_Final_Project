import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

class Crop{
    String name; // space
    int maxValue;
    int minValue;

    public Crop(String name, int minValue, int maxValue){
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}

class State{
    String name;
    int minValue;
    int maxValue;

    public State(String name, int minValue, int maxValue){
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}

public class Main {
    public static void main(String[] args) {

//        Cultivation(StateID,CropID,Year,Yield)  :- StateID=5, CropID =1, Year =2011, Yield=2124.
//        int states = 13;
//        int crops = 14;

        int startYear= 2011;
        int endYear= 2021;

        Hashtable<Integer, State> states = GenerateStatesObject();
        Hashtable<Integer, Crop> crops = GenerateCropRange();

        StringBuilder finalContent = new StringBuilder();
        finalContent.append("begin %MLPQ% \n");

        for (; startYear<=endYear; startYear++){

            for(Map.Entry<Integer, State> state : states.entrySet()) {
                int stateId = state.getKey();
                State stateValue = state.getValue();

                for(Map.Entry<Integer, Crop> crop : crops.entrySet()) {
                    int cropId = crop.getKey();
                    Crop cropValue = crop.getValue();

                    int  min_avg_StateAndCrop = (stateValue.minValue + cropValue.minValue)/2;
                    int  max_avg_StateAndCrop = (stateValue.maxValue + cropValue.maxValue)/2;


                    finalContent.append( GetLine(stateId, cropId, startYear, getRandomNumber(min_avg_StateAndCrop, max_avg_StateAndCrop)));

                }
            }

        }
        finalContent.append("end %MLPQ% ");

        WriteToFile(finalContent.toString());
    }

    private static Hashtable<Integer, Crop> GenerateCropRange(){
        Hashtable<Integer, Crop> CropIdAndName = new Hashtable<>();

        CropIdAndName.put(1, new Crop("Paddy",1500, 3000));
        CropIdAndName.put(2, new Crop("Wheat",2000, 5000));
        CropIdAndName.put(3, new Crop("Jowar",500, 2500));
        CropIdAndName.put(4, new Crop("Bajra",900, 2500));
        CropIdAndName.put(5, new Crop("Maize",1500, 7000));
        CropIdAndName.put(6, new Crop("Ragi",500, 2500));
        CropIdAndName.put(7, new Crop("Barley",1000,2500));
        CropIdAndName.put(8, new Crop("Sugarcane",700, 3500));
        CropIdAndName.put(9, new Crop("Groundnut", 800, 2000));
        CropIdAndName.put(10, new Crop("Sesamum",900, 1500));
        CropIdAndName.put(11, new Crop("Castor",1400, 4500));
        CropIdAndName.put(12, new Crop("Linseed",3000, 6000));
        CropIdAndName.put(13, new Crop("Cotton",1500, 2500));
        CropIdAndName.put(14, new Crop("Jute",3000,4000));

        return CropIdAndName;
    }

    private static Hashtable<Integer, State>  GenerateStatesObject() {
        Hashtable<Integer, State> stateIdAndName = new Hashtable<>();
        //Paddy range
        stateIdAndName.put(1, new State("Uttar Pradesh", 2000,  2500));
        stateIdAndName.put(2, new State("Madhya Pradesh", 1600, 2200 ));
        stateIdAndName.put(3, new State("Punjab",3700, 4500 ));
        stateIdAndName.put(4, new State("Rajasthan", 500, 1500 ));
        stateIdAndName.put(5, new State("West Bengal", 2500, 3200));
        stateIdAndName.put(6, new State("Haryana", 3000, 3500));
        stateIdAndName.put(7, new State("Bihar", 2200, 2600));
        stateIdAndName.put(8, new State("Maharashtra", 2000, 2500));
        stateIdAndName.put(9, new State("Andhra Pradesh", 3500, 4000));
        stateIdAndName.put(10, new State("Telangana", 2800, 3400));
        stateIdAndName.put(11, new State("Tamil Nadu", 1000, 2500));
        stateIdAndName.put(12, new State("Karnataka", 2400, 3400));
        stateIdAndName.put(13, new State("Others", 2500, 3500));

        return  stateIdAndName;
    }

    static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    static String GetLine(int stateId, int cropId, int year, int yield){
        return String.format("Cultivation(StateID,CropID,Year,Yield)  :- StateID=%s, CropID=%s, Year=%s, Yield=%s. \n", stateId, cropId, year, yield);
    }

    static void WriteToFile(String output){
        try {
            File myObj = new File("output.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                System.out.println(output);
                FileWriter myWriter = new FileWriter("output.txt");
                myWriter.write(output);
                myWriter.close();
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}