package data;

public class StationData {
    public int id = 999;
    public String temp = "?";

    public StationData(String str) {
        id = Integer.parseInt(str.substring(0, str.indexOf("=")));
//            System.out.print(str);
//            System.out.println(id);

        temp = str.substring(str.indexOf("=")+1, str.indexOf(",")); // TODO the others
    }
}
