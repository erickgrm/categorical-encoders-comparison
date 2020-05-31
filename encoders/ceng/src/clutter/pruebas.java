
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Ulises
 */
public class pruebas {

    static String fileName = "DatSagas4.txt";
    static double[][] train;                      // Array with the numeric and categorical (casted) data.
    static int rows = 3, columns = 5;
    static double count = 1d;
    static double[] array = new double[10];
    static Random seed;
    static AtomicInteger atomInt = new AtomicInteger();
    ;
    static double[] arreglo = new double[10];

    public static void printExchangedColumns() {
        train = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                train[i][j] = count;
                count++;
            }
        }

        System.out.println("Matriz Original");
        System.out.println("---------------");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(train[i][j] + "\t");
            }
            System.out.println("");
        }
        System.out.println("---------------");

        BMLP.N = rows;
        BMLP.mO = columns - 1;
        BMLP.FName = Archivo.FName;
        BMLP.Train = train;
        BMLP.effN = Archivo.effN;
        //Set the Number of Hidden Neurons.
        BMLP.setMI();

        BMLP.evaluaAllNN();
    }

    public static void test() {
        System.out.println(array.length);
        array[0] = seed.nextDouble();
        System.out.println(array[0]);
//        for (int i=0;i<10;i++){
//            array[i] = seed.nextDouble();
//            System.out.println(array[i]);
//        }
    }

    public static double doWork() {
        System.out.println("Tiempo 1: " + (System.currentTimeMillis()));
        int n = atomInt.incrementAndGet();
        System.out.println("incrementAndGet(): " + n);
        Random seed = new Random();
        //seed.setSeed(291192);
        Double avg = 0d;
        for (int j = 0; j < 1000000000; j++) {
            avg = avg + seed.nextDouble();
        }
        avg = avg / 1000000000;
        arreglo[n] = avg;
        System.out.println("arreglo[" + n + "]: " + (n + avg));
        return avg;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        long t = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            es.submit(
              new Callable() {
            	public Object call() throws Exception {
            	return doWork();}
     	        }
     	      );
        }
        es.shutdown();
        System.out.println("Tiempo 0: " + (System.currentTimeMillis() - t));
        
 /*
  *        Random seed = new Random();
         double avg=0;
         long t = System.currentTimeMillis();
         for(int i=0;i<1;i++){
         for(int j =0;j<1000000000;j++){
         avg = avg + seed.nextDouble();
         }
         avg = avg / 1000000000;
         }
         System.out.println("Tardo: " + (System.currentTimeMillis()-t));
   */      
    }
}
