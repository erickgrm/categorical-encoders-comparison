/**
 * @author Ulises
 * Clerical modifications by Erickgrm
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

/*
 * Class that reads the data from a File.
 */
public class Archivo {

    public static int maxI = 200, maxN = 15000, maxmO = 82, maxnI = 100;                     // Maximum # of categorical instances
    public static int N, effN, mO, nC, nI, nN;                                              // N, mO and number of Categorical Variables | # of numeric Variables (nN)
    public static double[][] numeric = new double[maxN][maxmO];                             // Array with the numerical values
    public static String[][] categorical = new String[maxN][maxmO];                         // Array with the categorical values
    public static String FName = "DatosMestizo.xls";                                        // Name of the file
    public static HashMap<String, Double> instance = new LinkedHashMap<String, Double>();   // Hashmap of Category and
    static Boolean[] IsCategorical = new Boolean[maxI];                                     // Boolean Array
    static BufferedReader Fbr;                                                              // Buffered Reader
    //    static String args[] = new String[2];                                                // Parameters array
    static Runtime rt = Runtime.getRuntime();                                               // Runtime

    // Reads, count many records, numerical and categorical variables are
    // contained in the file. Also gets the effective value of N.
    public void readFile(String[] args) throws IOException {
    	  boolean Title=false;
        try {
            Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FName))));
            while ((Fbr.readLine()) != null) N++;
            Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FName))));
		String linea=Fbr.readLine();
            String[] Linea = null;
            Linea = linea.split("\t"); double Test;
		try{for (int i=0;i<Linea.length;i++) Test=Double.parseDouble(Linea[i]);}
		catch (Exception e1) {N--;Title=true;}  
            //We check that N does not exceeds maxN
            if (N > maxN) {
                System.out.println("Exceeded the allowed number of vectors in the trainig file (" + maxN + ")\n");
            } else {
                Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FName))));
                double Valor;
                if (Title) linea = Fbr.readLine();
                linea = Fbr.readLine();
                Linea = linea.split("\t");
                mO = Linea.length;

                //mO must not exceed maxmO
                if (mO > maxmO) {
                    System.out.println("Exceeds the accepted number of vectors in the training file (" + maxN + ")\n");
                } else {
                    for (int i = 0; i < mO; i++) {
                        try {
                            Valor = Double.parseDouble(Linea[i]);
                            IsCategorical[i] = false;
                        } catch (Exception e) {
                            IsCategorical[i] = true;
                            nC++;                       //Number of Categorical Variables
                        }
                    }
                }
                //Fill the Double and String Arrays
                Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FName))));
		    if (Title) linea = Fbr.readLine();
                for (int i = 0; i < N; i++) {
                    linea = Fbr.readLine();
                    Linea = linea.split("\t");
                    int x = 0, y = 0;
                    for (int j = 0; j < mO; j++) {
                        if (IsCategorical[j]) {                               // If it's a categorical value
                            categorical[i][x] = Linea[j];
                            instance.putIfAbsent(Linea[j], 0d);                // Fill the Hashmap with a default value of 0
                            x++;
                        } else {                                              // If it's a numeric value
                            Valor = Double.parseDouble(Linea[j]);
                            numeric[i][y] = Valor;
                            y++;
                        }//endIf
                    }//endFor
                }//endLines

                // Update the number of unique categorical instances
                nI = instance.size();                       //Number of categorical Instances
                nN = mO - nC;                               //Number of numeric Variables
                if (nI > maxnI) {                           //If it exceeds maxNI we stop
                    System.out.println("La cantidad de instancias categoricas excede el limite de (" + maxnI + ").");
                } else {                                    //else
                    //Scale the numeric data
                    normalize();
                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < nN; j++) {
                            //System.out.print(numeric[i][j] + " ");
                        }
                        //System.out.println("");
                    }

                    /*
                     * Execute the compressor and calculate the new value of N
                     * Adapted to get bpc value as external argument to readfile
                     * github.com/erickgrm
                     */
                    // Effective Value of N
                    double cValue = 0d;
                    cValue = Double.parseDouble(args[0]);
                    effN = (int) (N * (cValue/8));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("An error was detected: " + e);
        }
    }

    // Scale all the numerical values into [0,1] range
    public static void normalize() {
        double[] maxNumeric = new double[mO - nC];
        double[] minNumeric = new double[mO - nC];

        for (int i = 0; i < mO - nC; i++) {
            maxNumeric[i] = numeric[0][i];
            minNumeric[i] = numeric[0][i];
        }

        for (int i = 1; i < N; i++) {
            for (int j = 0; j < mO - nC; j++) {
                if (maxNumeric[j] < numeric[i][j]) {
                    maxNumeric[j] = numeric[i][j];
                }
                if (minNumeric[j] > numeric[i][j]) {
                    minNumeric[j] = numeric[i][j];
                }
            }
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < mO - nC; j++) {
                //System.out.println(numeric[i][j] + "- " + minNumeric[j] + "/" + maxNumeric[j]);
                //if(i==0) System.out.print(numeric[i][j]+ "\t"); System.out.print("|"); 

                
                numeric[i][j] = (numeric[i][j] - minNumeric[j]) / (maxNumeric[j] - minNumeric[j]);
                //if(i==0) System.out.print(numeric[i][j]+ "\t"); System.out.print("|");
                //System.out.println(numeric[i][j]);
            }
        }
    }
}
