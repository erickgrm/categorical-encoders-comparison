/*
 *  Author & Copyright: Angel Fernando Kuri Morales
 *  Clerical modifications and adaptation to Linux by EGR at github.com/erickgrm
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.io.*;

public class CENG {

/*
 *	Parameters for the Genetic Algorithm (EGA)
 *
 */
    static int N = 25, D = 22, G = 100;                  // # of Individuals | # of Bits per instance | # of Generations
    static double Pc = 1.0,  Pm = 0.08;                  // Prob of crossing | Prob of Mutation         
/*
 *
 */
    static HashMap<String, Double> instanceMap = new LinkedHashMap<>();
    static int maxN = 500, minN = 1;                     // Max # of Individuals | Min # of Individuals
    static int maxV = 100, minV = 1;                     // Max # of Categorical Variables | Min # of Categorical Variables
    static int N_2, V, L, L_2, nT, nC, nN, B2M, B2M_;	 // # of Categorical Variables | Genoma's Lenght | # of Tuples | # of Categorical variables | # of numeric Variables                                                 
    static int Nx2 = N * 2;                              // # of Individuals * 2   
    static double fitness[];                             // Array of the Fitness of the Individuals
    static double AvgErr[];				 	             // Array for average errors
    static double Norm = 1 / Math.pow(2, D);             
    static Random seed;                                  // Random seed
    static double Var[][] = new double[maxN][maxV];      // Array containing the Fenotypes
    static String genome[] = new String[Nx2];            // Array of the Individuals
    static double[][] numeric;                           // Array of numeric values
    static String[][] categorical;                       // Array of categorical values
    static double[][] catToNum;                          // Array of categorical to numeric values
    static double[][] train;                             // Array with the numeric and categorical (casted) data.
    static Object[] nomInstances;                        // Array with the instances of the categorical values 
    static double lastFitness = +1000000d;	        	 // Check las fitness values 
    static int sameFit = 0;				                 // Counter for how long fitness does not change
    static PrintStream so = System.out;
    static boolean Stop;
    public static BufferedReader Kbr;
    static int TotSeed=0, Seed=0;  

    // Duplicate the first N individuals.
    public static void Duplicate(double fitness[], String genome[]) {
        for (int i = 0; i < N; i++) {
            genome[N + i] = genome[i];
            fitness[N + i] = fitness[i];
        }//endFor
    }//endDuplicate

    public static void Crossover(String genome[]) {
        int N_i, P;
        String LI, MI, RI, LN, MN, RN;
        for (int i = 0; i < N_2; i++) {
            //if (Math.random() > Pc) {
            //    continue;
            //}
            N_i = N - i - 1;
            P = 0;
            while (!(1 <= P & P <= L_2 - 1)) {
                P = (int) (seed.nextDouble() * L_2);
            }
            LI = genome[i].substring(0, P);
            MI = genome[i].substring(P, P + L_2);
            RI = genome[i].substring(P + L_2);
            LN = genome[N_i].substring(0, P);
            MN = genome[N_i].substring(P, P + L_2);
            RN = genome[N_i].substring(P + L_2);
            genome[i] = LI.concat(MN).concat(RI);
            genome[N_i] = LN.concat(MI).concat(RN);
        }//endFor
    }//endCrossover

    public static void Mutate(String genome[]) {
        int nInd, nBit;
        for (int i = 1; i <= B2M; i++) {
            nInd = -1;
            while (nInd < 0 | nInd >= N) {
                nInd = (int) (seed.nextDouble() * N);
            }
            nBit = -1;
            while (nBit < 0 | nBit >= L) {
                nBit = (int) (seed.nextDouble() * L);
            }
            /*
             *		** Mutation **
             */
            String mBit = "0";
            String G = genome[nInd];
            if (nBit != 0 & nBit != L - 1) {
                if (G.substring(nBit, nBit + 1).equals("0")) {
                    mBit = "1";
                }
                genome[nInd] = G.substring(0, nBit).concat(mBit).concat(G.substring(nBit + 1));
                continue;
            }//endif
            if (nBit == 0) {
                if (G.substring(0, 1).equals("0")) {
                    mBit = "1";
                }
                genome[nInd] = mBit.concat(G.substring(1));
                continue;
            }//endif
//		if (nBit==L-1){
            if (G.substring(L - 1).equals("0")) {
                mBit = "1";
            }
            genome[nInd] = G.substring(0, L - 1).concat(mBit);
//		}endIf
        }//endFor
    }//endMutate

    public static void prepareData(int k) {
        //Fill the HashMap with the value of the fenotype of individual id
        for (int i = 0; i < V; i++) {
            instanceMap.put((String) nomInstances[i], Var[k][i]);
        }//endfor

        // Get the value of the HashMap Value and populate the catToNum Array
        for (int i = 0; i < nT; i++) {
            for (int j = 0; j < nC; j++) {
                catToNum[i][j] = instanceMap.get(categorical[i][j]);
            }//endFor
        }//endFor

        // Fill the Double Array with Numeric+catToNum arrays.
        // This is the array the NN will receive.
        for (int i = 0; i < nT; i++) {
            int aux = 0;
            for (int j = 0; j < Archivo.mO; j++) {
                if (j < nN) {
                    train[i][j] = numeric[i][j];
                } else {
                    train[i][j] = catToNum[i][aux];
                    aux++;
                }//endIf
            }//endFor
        }//endFor
    }//endPrepareData

    /*
     * Conversion from binary to float
     */
    public static void GetFenotiposOfGenoma(int i) {
        double Var_k;
        String G = genome[i];
        int j = 0;
        for (int k = 0; k < V; k++) {		// k-th variable
            if (G.substring(j, j + 1).equals("0")) {
                Var_k = 0;
            } else {
                Var_k = 1;	// Bit "0"
            }
            for (int l = 1; l < D; l++) {		//l-th bit
                Var_k = Var_k * 2;
                j++;
                if (G.substring(j, j + 1).equals("1")) {
                    Var_k = Var_k + 1;
                }
            }//endFor                       ** Another bit
            Var[i][k] = Var_k * Norm;		// k-th variable adjusted to decimal
            j++;
        }//endFor                         ** Another variable
    }//endFenotipo

    /*
     * Evaluation of individual
     * */
    public static void Evaluate(double fitness[], String genome[]) throws Exception {
        for (int i = 0; i < N; i++) {
            GetFenotiposOfGenoma(i);
            prepareData(i);           
            fitness[i] = BMLP.train();
//            PrintStream TestFile = new PrintStream(new FileOutputStream(new File("RED"+i+".xls")));
//         	  TestFile.println(fitness[i]);
            AvgErr[i] = BMLP.avg_err;
            //System.out.print(".");
        }//endFor
    }//endEvaluate

    /*		Select best N individuals
     *
     */
    public static void Select(double fitness[], String genome[]) {
        double fitnessOfBest, fTmp;
        String sTmp;
        int indexOfBest;					// Minimiza
        for (int i = 0; i < N; i++) {
            fitnessOfBest = fitness[i];
            indexOfBest = i;
            for (int j = i + 1; j < Nx2; j++) {
                if (fitness[j] < fitnessOfBest) {
                    fitnessOfBest = fitness[j];
                    indexOfBest = j;
                }//endIf
            }//endFor
            if (indexOfBest != i) {
                sTmp = genome[i];
                genome[i] = genome[indexOfBest];
                genome[indexOfBest] = sTmp;
                fTmp = fitness[i];
                fitness[i] = fitness[indexOfBest];
                fitness[indexOfBest] = fTmp;
            }//endIf
        }//endFor
        double bestAvgErr = 1d;
        for (int i = 1; i < N; i++) {
            if (AvgErr[i] < bestAvgErr) {
                bestAvgErr = AvgErr[i];
            }
        }
        AvgErr[0] = bestAvgErr;
    }//endSelect

    /*
     * Starting population of N random individuals 
     */
    public static void StartPopulation(String genome[]) {
        for (int i = 0; i < N; i++) {
            genome[i] = "";
            for (int j = 1; j <= L; j++) {
                if (seed.nextDouble() < 0.5) {
                    genome[i] = genome[i].concat("0");
                } else {
                    genome[i] = genome[i].concat("1");
                }//endIf
            }//endFor
        }//endFor
    }//endStartPopulation

    public static void main(String[] args) throws IOException, Exception {
        // Flag for whether to print to standard output
        double verbose;
        verbose = Double.parseDouble(args[1]);

        if(verbose == 1.0){
            so.println("\n\t\t\tCENG VERSION 2.0 [2015/06/17]\n");
        }
        Kbr = new BufferedReader(new InputStreamReader(System.in));
        Archivo file = new Archivo();
        BMLP NeuralNet = new BMLP();
        String fileName;
        fileName = "ceng_input.txt"; // Hardcoded for testing, Feb-2020, erickgrm 

        String codes;
        codes = "ceng_codes.txt"; // Hardcoded for testing, Feb-2020, erickgrm 

        String trainOutput;

        Archivo.FName = fileName;
        seed = new Random();
        /*
         *
         *while (true) {
         *    so.println("Give me pseudo-random number root:");
         *    try {
         *        Seed = Integer.parseInt(Kbr.readLine());
         *        break;
         *    } catch (IOException | NumberFormatException e1) {
         *        so.println("Must be an integer!");
         *    }
         *}
         */
        Seed = 0; // Hardcoded for testing, 21-Feb-2020, erickgrm 
        seed.setSeed(Seed);
        // Read the file and compute all the variables        
        file.readFile(args);
        //Set the variables for the EGA
        V = Archivo.nI;                                 // # of Instances of all the categorical variables
        L = V * D;                                      // Length of the Genome
        L_2 = L / 2;
        N_2 = N / 2;
        nT = Archivo.N;                                 // # of Tuples
        nC = Archivo.nC;                                // # of categorical variables
        if (nC == 0) {
            so.println("\t***NO CATEGORICAL VARIABLES WERE DETECTED***");
            so.println("\n\t\t***PROGRAM ENDS***");
            return;
        }//endIf
        nN = Archivo.nN;                                // # Number of Numerical Variables
        numeric = new double[nT][nN];                   // Initialize the Double Array with the right dienions [nT][nN]
        categorical = new String[nT][nC];               // Initialize the String Array with the right dimensions [nT][nC]
        catToNum = new double[nT][nC];                  // Initialize the Double Array with the right dimensions [nT][nC]
        train = new double[nT][Archivo.mO];             //
        categorical = Archivo.categorical;              // Get the Array
        numeric = Archivo.numeric;                      // Get the numericsl Array
        instanceMap = Archivo.instance;                 // Get the HashMap <Instance, Code>
        nomInstances = instanceMap.keySet().toArray();  // Names of all the categorical instances
        fitness = new double[Nx2];
        AvgErr = new double[Nx2];
        B2M = (int) ((double) (N * L) * Pm);
        B2M_ = B2M;	  	  //Bits to Mutate
        //Set the variables for the Neural Network
        BMLP.N = Archivo.N;
        BMLP.mO = Archivo.mO - 1;
        BMLP.FName = Archivo.FName;
        BMLP.Train = train;
        BMLP.effN = Archivo.effN;
        //Set the Number of Hidden Neurons.
        BMLP.setMI();

        if(verbose == 1.0){
            System.out.println("----------------File Properties");
            System.out.println("File Name:             " + Archivo.FName);
            System.out.println("Tuples:                " + nT);
            System.out.println("Categorical Variables: " + nC);
            System.out.println("Numerical Variables:   " + nN);
            System.out.println("Categorical Instances: " + V);
            System.out.println("----------------Eclectic-GA Parameters");
            System.out.println("Generations:           " + G);
            System.out.println("Individuals:           " + N);
            System.out.println("Length of Genome:      " + L);
            System.out.println("Crossover Probability: " + Pc);
            System.out.println("Mutation Probability:  " + Pm);
            System.out.println("----------------Perceptron Parameters");
            System.out.println("Effective calculated tuples: " + BMLP.effN);
            System.out.println("Epochs/BMLP:                 " + BMLP.Epochs);
            System.out.println("Calculated Hidden Neurons:   " + BMLP.mI);
            System.out.println("Eta1: " + BMLP.eta1);
            System.out.println("Eta2: " + BMLP.eta2);
            System.out.println("Mu1:  " + BMLP.mu1);
            System.out.println("Mu2:  " + BMLP.mu2);
            System.out.println("----------------------------------");
        }

        //Create all the individuals
        StartPopulation(genome);
        TotSeed=Seed;    
        Evaluate(fitness, genome);
        seed.setSeed(TotSeed);
        int out = 1;
        if(verbose == 1.0){
            so.println("Generation:");
        }
        for (int i = 0; i <= G; i++) {
            //so.println("Generation: " + i + "\t ***CREATE FILE \"STOP\" AT ANY TIME TO END PROCESS***");
            
            if(verbose == 1.0){
                so.print(i +","); 
            }
            
            Duplicate(fitness, genome);         //Copy N best individuals to lower matrix
            Crossover(genome);					//Crossover i vs. N-i+1
            Mutate(genome);						//Mutate B2M bits
	        TotSeed=Seed;    
        	Evaluate(fitness, genome);
	        seed.setSeed(TotSeed);
            Select(fitness, genome);
            BufferedReader StopBr;                    	// Buffered Reader
            try {
                StopBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File("STOP"))));
                so.println("\n\t **PROCESS STOPPED BY USER***");
                Stop = true;
                break;
            } catch (Exception e1) {
                Stop = false;
            }
            /*
             *		CHECK FOR PREMATURE CONVERGENCE
             *		AND DETERMINE CATASTROPHIC MUTATION
             */
            if (fitness[0] <= 0.01d) {
                out = 2;
                break;
            }		// End if 1% accuracy is reached
            if (fitness[0] == lastFitness) {
                sameFit++;		// Count one more
            } else {
                lastFitness = fitness[0];
                sameFit = 0;
                if (B2M != B2M_) {
                    so.println("B2M --> " + B2M_);
                    B2M = B2M_;
                }
            }
        }//endfor
        GetFenotiposOfGenoma(0);
        prepareData(0);
        if(verbose == 1.0){
        so.println("\nBest Fitness: 0" + N2S.Num2String((float) fitness[0], 9, 8, 1) + "\n");
        }
        if (!Stop && verbose == 1.0) {
            while (true) {
                if (out == 1) {
                    so.println("\n\t*** Max number of generations reached***");
                    break;
                }
                if (out == 2) {
                    so.println("\n\t*** <0.001 accuracy reached ***");
                    break;
                }
            }//endWhile
        }//endIf
        PrintStream Codes = new PrintStream(new FileOutputStream(new File(codes)));
        Codes.println("Fitness: "+ N2S.Num2String((float) fitness[0],11,9,1));
        Codes.println("Average: "+ N2S.Num2String((float) AvgErr[0], 11,9,1));
        for (int i = 0; i < V; i++) {
            String Var_0i = N2S.Num2String((float) Var[0][i], 11,6,1);
            Codes.println(nomInstances[i] + ": " + Var_0i);
        }//endfor
        Codes.close();
        if (Stop) {
            so.println("\n\t\t***REMEMBER TO DELETE FILE \"STOP\"***\n");
        }//endIf
    }
}
