/**
 * Created by huzaifa.aejaz on 5/13/17.
 */
public class Main {
    public static void main(String[] args) throws Exception {


        //SequenceDiagramParser se = new SequenceDiagramParser("/Users/huzaifa.aejaz/Desktop/input/","Main","main","a");
        SequenceDiagramParser se = new SequenceDiagramParser(args[0], args[1],args[2], args[3]);//
        se.sequenceDiagramController();


    }
}
