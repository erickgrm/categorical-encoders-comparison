
/*
 * CREA TABLA DE DATOS CON RELACIONES PONDERADAS
 */

import java.io.*;
import java.util.Random;

class CreaTabla_eng {

    static BufferedReader Kbr = new BufferedReader(new InputStreamReader(System.in));
    static PrintStream so = System.out;
    static PrintStream Fps;
    static BufferedReader Fbr;
    static String Resp, FName;
    static int N;
    static Float sumWeight;
    static Float Param[][] = new Float [5][4];    // Arreglo de parámetros
    static String Lugar [] = new String[3];  	  // Arreglo de lugares
    static String Etnia [] = new String[4];  	  // Arreglo de razas
    static String Linea[],linea="";

    public static void main(String[] args) throws Exception {
	    Lugar[0]="North";Lugar[1]="Center";Lugar[2]="South";
	    Etnia[0]="White";Etnia[1]="Asian";Etnia[2]="Indian";Etnia[3]="Other";
	    int Edad=20;
	    String LDN="A";
	    int Esc=12;
	    String Raza="white";
	    String Sexo="M";
	    float Sueldo=10000;
//
        int i,j;
        boolean ask = false;

        while (true) {
            if (ask) {
                so.println("\nWant to read another file?");
                so.println("\"S\" to continue; other to end...)");
                Resp = Kbr.readLine().toUpperCase();
                if (!Resp.equals("S")) {
                    break;
                }
                so.println("\nGive me the name of the file of weights...");
                so.println("\n<ENTER> for the same.");
                Resp = Kbr.readLine();
            } else {
                so.println("\nGive me the name of the file of weights...");
                Resp = Kbr.readLine();
                ask = true;
            }//endif
            if (!Resp.equals("")) {
                FName = Resp;
            }//endif
            try {Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FName))));}
            catch (Exception e1) {so.println("No se encontro \"" + FName + "\"");continue;}
            N=0;
            while ((Fbr.readLine())!=null) {N++;}
            Fbr.close();
		if (N!=5){so.println("Must be 5 lines of data; only read "+N);continue;}
            String[] Linea = null;
            Fbr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FName))));
            linea=Fbr.readLine();
            Linea =linea.split("\t");
            N=Linea.length;
            if (N!=3) {so.println("Must be 3 values for \"Age\"..."); continue;}
            linea=Fbr.readLine();
            Linea =linea.split("\t");
            N=Linea.length;
            if (N!=3) {so.println("Must be 3 values for \"Birthplace (zones)\"..."); continue;}
            linea=Fbr.readLine();
            Linea =linea.split("\t");
            N=Linea.length;
            if (N!=4) {so.println("Must be 4 values for \"Years of study\"..."); continue;}
            linea=Fbr.readLine();
            Linea =linea.split("\t");
            N=Linea.length;
            if (N!=4) {so.println("Must be 4 values for \"Race\"..."); continue;}
            linea=Fbr.readLine();
            Linea =linea.split("\t");
            N=Linea.length;
            if (N!=2) {so.println("Must be 2 values for \"Sex\"..."); continue;}
            Fbr.close();
            Fbr=new BufferedReader(new InputStreamReader(new FileInputStream(new File(FName))));
            sumWeight=0f; Float maxWeight_j;
            String VarName []= new String [5];
            VarName[0]="Age";
            VarName[1]="Birthplace";
            VarName[2]="Years of study";
            VarName[3]="Race";
            VarName[4]="Sex";
            so.println();
            so.println("PARAMETERS:\n");
            for (i=0;i<5;i++) {
                linea = Fbr.readLine();
                Linea = linea.split("\t");
                maxWeight_j=-1f;
		    so.println(VarName[i]);
                so.println("-------------------");
/*
 *			Calcula el valor máximo asignado al j-ésimo peso
 */
                for (j=0;j<Linea.length;j++) {	
                    Param[i][j] = Float.parseFloat(Linea[j]);
                    if (Param[i][j]>maxWeight_j) maxWeight_j=Param[i][j];
			  so.printf("Param[%3.0f][%3.0f]= %15.7f\n",(float)i,(float)j,Param[i][j]);
                }//endfor
                so.println();
/*
 *			Se acumulan los pesos máximos
 */
                sumWeight=sumWeight+maxWeight_j;
            }//endFor
		boolean loop=true;
 		while (loop){
	            so.println("How many tuples do you wish to generate?");
      	      N=Integer.parseInt(Kbr.readLine());
			so.println("A table will be produced with  "+N+" registers separated by <TAB>");
			while (true){
				so.println("Confirm (S/N)");
				Resp=Kbr.readLine().toUpperCase();
				if (Resp.equals("S")) {loop=false;break;}
				if (Resp.equals("N")) break;
			}//endWhile
		}//endWhile
		so.println("Give me the name of the output file:");
		Resp=Kbr.readLine();
		PrintStream Fps=new PrintStream(new FileOutputStream(new File(Resp)));
		Fps.println("Age"+"\t"+"Place"+"\t"+"Studies"+"\t"+"Race"+"\t"+"Sex"+"\t"+"Salary");
		float Weight;
		for (i=0;i<N;i++){
			Edad=(int)(Math.random()*50)+21;			//Entre 20 y 70
			while (true){
			if (Edad<=30) {Weight=Param[0][0];break;}
			if (Edad<=40) {Weight=Param[0][1];break;}
			              {Weight=Param[0][2];break;}}
			int ldn=(int)(Math.random()*3);			//Entre 0 y 2
			while (true){
			if (ldn==0) {Weight=Weight+Param[1][0];break;}
			if (ldn==1) {Weight=Weight+Param[1][1];break;}
			            {Weight=Weight+Param[1][2];break;}}
			LDN =Lugar[ldn];
			Esc =(int)(Math.random()*22)+4;			//Entre 4 y 25
			while (true){
			if (Esc<=12) {Weight=Weight+Param[2][0];break;}
			if (Esc<=17) {Weight=Weight+Param[2][1];break;}
			if (Esc<=19) {Weight=Weight+Param[2][2];break;}
			             {Weight=Weight+Param[2][3];break;}}
			int raza=(int)(Math.random()*4);			//Entre 0 y 3
			while (true){
			if (raza==0) {Weight=Weight+Param[3][0];break;}
			if (raza==1) {Weight=Weight+Param[3][1];break;}
			if (raza==2) {Weight=Weight+Param[3][2];break;}
			             {Weight=Weight+Param[3][3];break;}}
			Raza=Etnia[raza];
			if (Math.random()>0.5){
				Weight=Weight+Param[4][0];
				Sexo="M";
			}else{
				Weight=Weight+Param[4][1];
				Sexo="F";
			}//endIf
/*
 *			En "Weight" se calcula el paso proporcional asignado
 *			al sueldo en función de los pesos de cada uno de
 *			los valores de peso asignados a cada atributo
 */
			Weight=Weight/sumWeight;
			Sueldo=(float)(Math.random()*145000+5000)*Weight;	//Entre 5,000 y 150,000
			String lineO=N2S.Num2String((float)Edad,3,0,0);		//Edad
			lineO= lineO+"\t"+LDN;						//Lugar de nacimiento
			lineO= lineO+"\t"+N2S.Num2String((float)Esc,3,0,0);	//Escolaridad
			lineO= lineO+"\t"+Raza;						//Raza
			lineO= lineO+"\t"+Sexo;
			lineO= lineO+"\t"+N2S.Num2String(Sueldo,10,2,1);
			Fps.println(lineO);
		}//Otra línea de valores en el archivo destino
		Fps.close();
        }	 //Se cierra el "While" de lectura de archivos de pesos
    }		 //Se cierra "Main"
}		 //Se cierra la clase
