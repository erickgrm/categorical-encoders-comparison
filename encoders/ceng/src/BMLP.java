
/*
 *
 * Training of Neural Network by Backpropagation
 * Author and copyright: Angel Fernando Kuri Morales
 * Clerical modifications and adaptation to linux by github.com/erickgrm
 * modification: 500 epochs to 300 epochs
 *
 */
import java.util.Random;

class BMLP {

    static int maxN = 25000, maxmO = 82, maxmI = 549, Epochs = 300;
    static double eta1 = 0.1, eta2 = 0.01, mu1 = 0.3, mu2 = 0.5;    // Learning rate and momentum 
    static double Train[][] = new double[maxN][maxmO];              // Array to store the read file 
    static String C[] = new String[12];                             // # of coefficients for mI=f(mO,N)
    static double W1[][] = new double[maxmI + 1][maxmO];            // Weights from entry layer I to hidden layer J, W1[j][i]
    static double W2[] = new double[maxmI + 1];                     // Weights from hidden layer J to final node, W1[]
    static double d_H[] = new double[maxmI], d_O;                   // Hidden layer gradients 
    static double Y[] = new double[maxmI];                          // Hidden layers output 
    static boolean Hits[] = new boolean[maxN];                      // Hits array
    static int effN, N, mO, mI, maxRand;                            // No of tuples, no of entry neurons, no of neurons in hidden layer
    static double DW_ji[] = new double[maxmI];                      // (back) Weights change from hidden layer J to entry layerI 
    static double DW_ji_Ant[] = new double[maxmI];                  // (back) Weights change of hidden layer in a previous moment
    static double DW_jk, DW_jk_Ant, DW_jk_Ant_Bias;
    static String FName;
    static double RMS[] = new double[maxmO];
    static Random rnd = new Random();
    static double avg_err;
    
    //Set mI (Number of Hidden Neurons)
    public static void setMI() {
        //    Degrees   Coefficient
        C[0] = "35	22.7555389";
        C[1] = "01	0.966301284";
        C[2] = "13	23.42519755";
        C[3] = "42	535.1062835";
        C[4] = "24	-28.99757073";
        C[5] = "56	-5.758880312";
        C[6] = "52	-414.4118964";
        C[7] = "22	133.9032431";
        C[8] = "12	-34.2975469";
        C[9] = "62	128.049467";
        C[10] = "32	-355.8550098";
        C[11] = "14	-4.825964161";
        String Linea[];
        double fmI = 0, fmO = (double) (mO - 2) / 80d, fN = (double) (effN - 100) / 24900d;
        for (int i = 0; i < 12; i++) {
            Linea = C[i].split("\t");
            int p1 = Integer.parseInt(Linea[0].substring(0, 1));	// Get the two digits	 
            int p2 = Integer.parseInt(Linea[0].substring(1, 2));	// of the powers		
            double C = Double.parseDouble(Linea[1]);              // Get the coefficient
            fmI = fmI + Math.pow(fmO, p1) * Math.pow(fN, p2) * C;		// Calculate mI*
        }//endFor
        mI = (int) (fmI * 549 + 0.5);
        if (mI < 1) {
            mI = 1;						// Calculate mI=f(mI*)
        }
        mI = mI + 2;						// Add a safety factor
    }//endSetmI

    // Returns max in array 
    public static double Range(double RMS[]) {
        double maX = 0d;
        for (int i = 0; i < mO; i++) {
            if (RMS[i] >= maX) {
                maX = RMS[i];
            }//endif
        }//endfor
        return maX;
    }//endRange

    // Returns array of doubles corresponding to the n-th column
    public static double[] getColumn(int n) {
        double col[] = new double[N];
        for (int i = 0; i < N; i++) {
            col[i] = Train[i][n];
        }//endfor
        return col;
    }//end_getColumn

    /*
     * Replace the last column with the n-th column.
     *replaceColumn(4) will do the following
     * 1    2   3   4  5  -->  1    2   3  4   5 
     * X1  X2  X3  X4  Y  -->  X1  X2  X3  Y  X5 
     */
    public static void replaceColumn(int n) {
        double last[] = getColumn(mO);          //Get the 'Y' column
        double aux[] = getColumn(n);            //Get the 'N-th' column (Independent variable)
        for (int i = 0; i < N; i++) {
            Train[i][n] = last[i];
            Train[i][mO] = aux[i];
        }//endfor
    }//end_replaceColumn

    /*
     * Permutes columns in the following fashion
     * X1 X2 X3 | Y -> X1 X2 Y | X3 -> X1 X3 Y | X2
     */
    public static void evaluateAllNN() {
        //We explore all the diferent combinations of possible columns
        for (int i = 0; i <= mO; i++) {
            replaceColumn(mO - i);              // Exchange column mO to i
            RMS[i] = Err4NN();
        }//endfor
    }//end evaluateAllNN

    /*
     * Calculates the accumulated error according to the number of training epochs
     *
     */
    public static double Err4NN() {
        int Tuple = 0;
        int i, j, m;
        double Y_O;                                         // Predicte value for a tuple nT
        double maxErr;                                      // Maximum Error of the last Epoch
        double Err_nE, ErrEpoch_nE, Err_nE_2, sqrt_ErrEpoch_nE, X;
        double absErr, maxAbsErr;
        maxRand = (int) ((float) N * .85);                  // Number of random probes

        // Simple seed for all NNs
        rnd.setSeed(261192);

        // Initialise weights of hidden layer with numbers in [-0.1, 0.1]
        for (i = 0; i <= mO; i++) {			// Connections of the j-th neuron in H {mI   Neuronas en H}
            for (j = 1; j <= mI; j++) {         // To the i-th neuron in I {mO+1 Neuronas en I}
                if (rnd.nextDouble() < 0.5) {
                    W1[j][i] = -rnd.nextDouble() * .1;
                } else {
                    W1[j][i] = +rnd.nextDouble() * .1;
                }//endif
            }//endfor
        }//endfor

        // Initialise weights of output layer with numbers in [-0.1, 0.1]
        for (j = 0; j <= mI; j++) {                     // Connections of the j-th neuron in O to the j-th neuron in H    
            if (rnd.nextDouble() < 0.5) { 		        // -0.1<=W1[j][i]<=+0.1
                W2[j] = -rnd.nextDouble() * .1;
            } else {
                W2[j] = +rnd.nextDouble() * .1;
            }//endif
        }//endfor

        // Initialise changes for momentum
        DW_jk_Ant_Bias = 0d;                        // Change for the weight of neuron jk in a previous moment
        DW_jk_Ant = 0d;                             // Change for the weigth of the bias neuron jk in a previous moment
        for (j = 0; j <= mI; j++) {
            DW_ji_Ant[j] = 0d;                      // Change for the weight in the neuron ji in a previous moment
        }//endfor

        // Calculates error for an epoch 
        for (int nE = 0; nE < Epochs; nE++) {
            maxAbsErr = -1d;                        // Maximum Error 
            ErrEpoch_nE = 0d;                       // Error for Epoch nE

            // Fill up array with false Hits 
            for (int nT = 0; nT < N; nT++) {
                Hits[nT] = false;                               
            }//endfor

            /*
             //Pruebas
             System.out.println("");
             System.out.println("Pesos de la capa Entrada-Intermedia");
             for (j = 1; j <= mI; j++) {
             for (i = 0; i <= mO; i++) {
             System.out.print("W1[" + j + "][" + i + "]: " + W1[j][i] + "\t");
             }
             System.out.println("");
             }

             System.out.println("--------------------------- \n");

             System.out.println("Pesos de la capa Intermedia-Salida");
             for (j = 0; j <= mI; j++) {
             System.out.print("W2[" + j + "]: " + W2[j] + "\t");
             }

             System.out.println("");
             System.out.println("--------------------------- \n");

             System.out.println("Valores de la tupla numero 0");
             for (i = 0; i <= mO; i++) {
             System.out.print(Train[0][i] + "\t");
             }
             System.out.println("");
             System.out.println("");

             // End Prueba
             */
            //  For cycle for all N tuples
            for (int nT = 0; nT < N; nT++) {
                Tuple = getTuple(nT);                           // Get pointer to random tuple
                //Tuple = nT;
                for (j = 1; j <= mI; j++) {                     // For cycle for all mI neurons
                    Y[j] = W1[j][0];
                    for (i = 1; i <= mO; i++) {                 // For cycle for all columns from i to mO
                        X = Train[Tuple][i - 1];
                        Y[j] = Y[j] + (X * W1[j][i]);           // Dot product for each neuron by input
                    }//endFor_Columnas
                    Y[j] = Math.tanh(Y[j]);			            // Activation function: tanh
                }//endFor_Neuronas

                // Neuron in output layer 
                Y_O = W2[0];                                    // Add bias to output neuron 
                for (j = 1; j <= mI; j++) {
                    Y_O = Y_O + (Y[j] * W2[j]);           	    // Activaction function: linear (for output layer)
                }//end_For

                // Calculates error
                Err_nE = Train[Tuple][mO] - Y_O;                // Error = Real_value - Estimated_value
                absErr = Math.abs(Err_nE);                      // Absoluto value
                if (absErr > maxAbsErr) {
                    maxAbsErr = absErr;
                }//endIf
                Err_nE_2 = Err_nE * Err_nE;                     // Error^2
                // Accumulated error for each epoch 
                ErrEpoch_nE = ErrEpoch_nE + Err_nE_2;

                //Backpropagate
                // Biases are weights with input 1
                d_O = Err_nE * 1;                                   // (linear) Gradient the output neuron Eq. 4.14

                // Contribution of the Bias Neuron to the output layer
                W2[0] = W2[0] + (eta2 * d_O * 1) + (mu2 * DW_jk_Ant_Bias);
                DW_jk_Ant_Bias = (eta2 * d_O * 1);

                // Update of the Weights of the other Neurons in the last layer.
                for (i = 1; i <= mI; i++) {                                  // Update all weight of the output layer
                    W2[i] = W2[i] + (eta2 * d_O * Y[i]) + (mu2 * DW_jk_Ant); // W2 = W2 + DWjk + Momentuum
                    DW_jk_Ant = (eta2 * d_O * Y[i]);                         // Save changes for the next iteration
                }//endfor

                /*
                 * Local gradients for the hidden layers
                 * Calculated for eachhidden layer according to the local gradient
                 * of the next layer (Eq. 4.38, Haykin)
                 */
                for (j = 1; j <= mI; j++) {
                    d_H[j] = ((1 - (Y[j] * Y[j])) * d_O * W2[j]);
                }//endfor

                /*
                 * Once weights for the hidden layers are known weights are updated
                 */
                for (i = 0; i <= mO; i++) {
                    for (j = 1; j <= mI; j++) {
                        if (i == 0) {                           // Bias column 
                            // Update bias
                            DW_ji[j] = (eta1 * d_H[j] * 1);                          // Calculate change for the Bias weight 
                            W1[j][i] = W1[j][i] + DW_ji[j] + (mu1 * DW_ji_Ant[j]);   // Update weight of the Bias neuron
                            DW_ji_Ant[j] = DW_ji[j];                                 // save weight change 
                        } else {
                            DW_ji[j] = (eta1 * d_H[j] * Train[Tuple][i - 1]);
                            W1[j][i] = W1[j][i] + DW_ji[j] + (mu1 * DW_ji_Ant[j]);
                            DW_ji_Ant[j] = DW_ji[j];
                        }//endIf
                    }//endFor_Neuronas
                }//endFor_Columnas
            }//endForTuplas			
            sqrt_ErrEpoch_nE = Math.sqrt(ErrEpoch_nE / N);
        }//endForEpocas

        /*
         * For cycle over all neurons obtaining max error Y-Y_O)
         */
        maxErr = -10000000d;
        for (int nT = 0; nT < N; nT++) {
            for (j = 1; j <= mI; j++) {
                //Y[j] = 0d;
                Y[j] = W1[j][0];
                for (i = 1; i <= mO; i++) {        	   // Bias, 1 is put as value for X 
                    X = Train[nT][i - 1];
                    Y[j] = Y[j] + (X * W1[j][i]);  	   // Dot product for each neuron for input
                }//endFor
                Y[j] = Math.tanh(Y[j]);		           // Activation function: tanh
            }//endFor

            // Output neuron
            Y_O = W2[0];                               // Add bias to output neuron
            for (j = 1; j <= mI; j++) {
                Y_O = Y_O + (Y[j] * W2[j]);        	   // Activation for output layer: linear
            }//endfor
            Err_nE = Math.abs(Train[nT][mO] - Y_O);    // Error absolute(true-estimated)
            if (Err_nE >= maxErr) {
                maxErr = Err_nE;
            }//end_If
        }//end_For
        return maxErr;
    }//endErr4NN

   /*
    * Get pointer to random tuple 
    */
    public static int getTuple(int i) {
        int j;
        // Get tuples sequentially
        if (i > maxRand) {						// If already searched 85%
            for (j = 0; j < N; j++) {                       // Traverse remaining
                if (!Hits[j]) {                             // Until one is unused
                    Hits[j] = true;					// Mark it as used
                    return j;						// Return the index
                }//endIf						// Or keep looking
            }//endFor
        }//endIf

        // Random tuples
        while (true) {
            j = (int) ((float) rnd.nextDouble() * (float) (N - 1));     // Propose an element
            if (!Hits[j]) {						// If not used
                Hits[j] = true;                                         // Mark it as used
                return j;						// Return the index
            }//endIf							// Or keep looking
        }//endWhile
    }//endGetProbe

    // Testing for the neural network
    public static void evaluaAllNN() {
        for (int i = 0; i <= mO; i++) {
            replaceColumn(mO - i);
            System.out.println("---------------");
            for (int j = 0; j < N; j++) {
                for (int z = 0; z <= mO; z++) {
                    System.out.print(Train[j][z] + "\t");
                }
                System.out.println("");
            }
            System.out.println("---------------");
            System.out.println("");
        }
    }

    public static double train() {
        evaluateAllNN();
    	  avg_err=0d;
        for (int i=0;i<mO;i++){
        	avg_err=avg_err+RMS[i];
        }avg_err=avg_err/(double)mO;
        return Range(RMS);
    }
}
