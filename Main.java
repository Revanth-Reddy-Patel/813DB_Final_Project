import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class Crop {
    String name; // space
    int maxYieldValue;
    int minYieldValue;
    int maxPriceValue;
    int minPriceValue;

    public Crop(String name, int minYieldValue, int maxYieldValue, int maxPriceValue, int minPriceValue) {
        this.name = name;
        this.minYieldValue = minYieldValue;
        this.maxYieldValue = maxYieldValue;
        this.maxPriceValue = maxPriceValue;
        this.minPriceValue = minPriceValue;
    }
}

class State {
    String name;
    int minYieldValue;
    int maxYieldValue;

    public State(String name, int minYieldValue, int maxYieldValue) {
        this.name = name;
        this.minYieldValue = minYieldValue;
        this.maxYieldValue = maxYieldValue;
    }
}

class XAndY {
    int x;
    int y;

    public XAndY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getter
    public int getX() {
        return this.x;
    }

}

class Cultivation {
    int stateId;
    int cropId;
    int year;
    int yield;
    int price;
}

public class Main {
    public static void main(String[] args) throws IOException {

        int totalCrops = 5;

        GenerateCultivationRelation();

        GeneratePiecewiseReference(totalCrops);

    }

    static void GeneratePiecewiseReference(int totalCrops) throws IOException {
        Hashtable<Integer, State> states = GenerateStatesObject();
        Hashtable<Integer, Crop> crops = GenerateCropRange();

        List<Cultivation> completeData = ParseCultivationFile();
        StringBuilder piecewiseData = new StringBuilder();

        List<XAndY> allPriceValues = new ArrayList<>();
        List<XAndY> allYieldValues = new ArrayList<>();

        StringBuilder matlabInput = new StringBuilder();

        Map<Integer, Map<Integer, List<Cultivation>>> dataByCropIdAndStateId = completeData.stream()
                .collect(Collectors.groupingBy(c -> c.cropId,
                        Collectors.groupingBy(c -> c.stateId,
                                Collectors.mapping((Cultivation c) -> c, toList()))));

        for (int i = 1; i <= totalCrops; i++) { // each crop
            Map<Integer, List<Cultivation>> dataByState = dataByCropIdAndStateId.get(i);
            piecewiseData.append("\nCrop:" + i);
            piecewiseData.append("|" + crops.get(i).name);
            piecewiseData.append("\n****************************************************************");

            for (int j = 1; j <= 13; j++) { // each state
                piecewiseData.append("\nState:" + j);
                piecewiseData.append("|" + states.get(j).name);
                piecewiseData.append("\n---------");
                List<Cultivation> data = dataByState.get(j);
                List<XAndY> xAndYList = new ArrayList<>();
                List<XAndY> xAndZList = new ArrayList<>();

                for (Cultivation cultivation : data) {
                    int x = cultivation.year;
                    int y = cultivation.price;
                    int z = cultivation.yield;
                    xAndYList.add(new XAndY(x, y));
                    xAndZList.add(new XAndY(x, z));
                }
                allPriceValues.addAll(SortingByX(xAndYList));
                allYieldValues.addAll(SortingByX(xAndZList));

                piecewiseData.append("\nPrice: " + MakeString(SortingByX(xAndYList)));
                piecewiseData.append("\nYield" + MakeString(SortingByX(xAndZList)));
            }
        }
        matlabInput.append(MakeString(allPriceValues));
        matlabInput.append(MakeString(allYieldValues));

        WriteToFile(matlabInput.toString(), "matlabInput.txt");

        WriteToFile(piecewiseData.toString(), "piecewiseData.txt");
    }

    private static Cultivation convertToCultivationObject(String line) {
        Cultivation obj = new Cultivation();
        List<String> allMatches = new ArrayList<>();

        StringBuilder string = new StringBuilder(line);
        string.setCharAt(line.trim().length() - 1, ',');

        Matcher m = Pattern.compile("(?<==)(.*?)(?=,)")
                .matcher(string);
        while (m.find()) {
            allMatches.add(m.group());
        }
        obj.stateId = Integer.parseInt(allMatches.get(0));
        obj.cropId = Integer.parseInt(allMatches.get(1));
        obj.year = Integer.parseInt(allMatches.get(2));
        obj.yield = Integer.parseInt(allMatches.get(3));
        obj.price = Integer.parseInt(allMatches.get(4));
        return obj;
    }

    private static List<Cultivation> ParseCultivationFile() throws IOException {
        BufferedReader bufferReader = null;
        List<Cultivation> cultivationData = new ArrayList<>();

        try {
            bufferReader = new BufferedReader(new FileReader("Cultivation.txt"));
            String line = bufferReader.readLine();
            int lineNo = 1;
            while (line != null) {
                if (lineNo > 0) {
                    Cultivation data = convertToCultivationObject(line);
                    cultivationData.add(data);
                }
                line = bufferReader.readLine();
                lineNo++;
            }

            System.out.println("Total Data :" + cultivationData.size());

        } catch (IOException | NumberFormatException exception) {
            System.out.println(exception.getMessage());
        } finally {
            bufferReader.close();
        }

        return cultivationData;
    }

    static void GenerateCultivationRelation() {
        int startYear = 2017;
        int endYear = 2021;
        int loopYear = endYear;

        Hashtable<Integer, State> states = GenerateStatesObject();
        Hashtable<Integer, Crop> crops = GenerateCropRange();

        StringBuilder cultivationFile = new StringBuilder();
//        cultivationFile.append("begin %MLPQ% \n");
        for (Map.Entry<Integer, Crop> crop : crops.entrySet()) {
            int cropId = crop.getKey();
            Crop cropValue = crop.getValue();

            for (Map.Entry<Integer, State> state : states.entrySet()) {
                int stateId = state.getKey();
                State stateValue = state.getValue();

                int min_avg_yield_StateAndCrop = (stateValue.minYieldValue + cropValue.minYieldValue) / 2;
                int max_avg_yield_StateAndCrop = (stateValue.maxYieldValue + cropValue.maxYieldValue) / 2;

                for (; loopYear >= startYear; loopYear--) {

                    int yield = getRandomNumber(min_avg_yield_StateAndCrop, max_avg_yield_StateAndCrop);
                    int price = getRandomNumber(cropValue.minPriceValue, cropValue.maxPriceValue);

                    cultivationFile.append(GetLine(stateId,
                            cropId,
                            loopYear,
                            yield,
                            price
                    ));
                }
                loopYear = endYear;
            }
        }
//        cultivationFile.append("end %MLPQ% ");

        WriteToFile(cultivationFile.toString(), "Cultivation.txt");
    }

    private static StringBuilder MakeString(List<XAndY> xAndYValues) {
        StringBuilder xAndYString = new StringBuilder();

        xAndYString.append("\nX: [");
        for (XAndY xValue : xAndYValues) {
            xAndYString.append(xValue.x + " ");
        }
        xAndYString.deleteCharAt(xAndYString.length() - 1);

        xAndYString.append("]\n");

        xAndYString.append("Y: [");
        for (XAndY yValue : xAndYValues) {
            xAndYString.append(yValue.y + " ");
        }
        xAndYString.deleteCharAt(xAndYString.length() - 1);

        xAndYString.append("]\n");
        return xAndYString;
    }

    private static List<XAndY> SortingByX(List<XAndY> xAndYValues) {
        List<XAndY> xSorted;
        xSorted = xAndYValues;
        xSorted.sort(Comparator.comparingInt(XAndY::getX));
        return xSorted;
    }

    private static Hashtable<Integer, Crop> GenerateCropRange() {
        Hashtable<Integer, Crop> CropIdAndName = new Hashtable<>();

        CropIdAndName.put(1, new Crop("Paddy", 1500, 3000, 150, 170));
        CropIdAndName.put(2, new Crop("Wheat", 2000, 5000, 155, 165));
        CropIdAndName.put(3, new Crop("Jowar", 500, 2500, 150, 163));
        CropIdAndName.put(4, new Crop("Bajra", 900, 2500, 170, 195));
        CropIdAndName.put(5, new Crop("Maize", 1500, 7000, 145, 175));

        return CropIdAndName;
    }

    private static Hashtable<Integer, State> GenerateStatesObject() {
        Hashtable<Integer, State> stateIdAndName = new Hashtable<>();
        //Paddy range
        stateIdAndName.put(1, new State("Uttar Pradesh", 2000, 2500));
        stateIdAndName.put(2, new State("Madhya Pradesh", 1600, 2200));
        stateIdAndName.put(3, new State("Punjab", 3700, 4500));
        stateIdAndName.put(4, new State("Rajasthan", 500, 1500));
        stateIdAndName.put(5, new State("West Bengal", 2500, 3200));
        stateIdAndName.put(6, new State("Haryana", 3000, 3500));
        stateIdAndName.put(7, new State("Bihar", 2200, 2600));
        stateIdAndName.put(8, new State("Maharashtra", 2000, 2500));
        stateIdAndName.put(9, new State("Andhra Pradesh", 3500, 4000));
        stateIdAndName.put(10, new State("Telangana", 2800, 3400));
        stateIdAndName.put(11, new State("Tamil Nadu", 1000, 2500));
        stateIdAndName.put(12, new State("Karnataka", 2400, 3400));
        stateIdAndName.put(13, new State("Others", 2500, 3500));

        return stateIdAndName;
    }

    static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    static String GetLine(int stateId, int cropId, int year, int yield, int price) {
        return String.format("Cultivation(StateID,CropID,Year,Yield,Price) :- StateID=%s, CropID=%s, Year=%s, Yield=%s, Price=%s. \n", stateId, cropId, year, yield, price);
    }

    static void WriteToFile(String output, String fileName) {
        try {
            File myObj = new File(fileName);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                System.out.println(output);
                FileWriter myWriter = new FileWriter(fileName);
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