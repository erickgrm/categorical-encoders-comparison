/**
 *
 * @author Ulises
 *
 *  USO:
 *   String = N2S.Num2String((float) numero, posiciones + 1, decimales, 0s_alaIzquierda);
 */
class N2S {

  public static String Num2String (float Num, int NumDigits, int NumDecs, int ClearZeros){
    if (ClearZeros != 0 && ClearZeros != 1){
      System.out.println("****Invalid Flag (0-->\"0\"; 1-->\" \")****");
      System.out.println("****NULL string will be returned****");
      return "";
    }//endif
    if (NumDigits <= 1){
      System.out.println("****  Invalid number of digits  ****");
      System.out.println("****NULL string will be returned****");
      return "";
    }//endif
    if (NumDigits < NumDecs + 1){
      System.out.println("The number of decimals cannot be larger than the string's length!");
      System.out.println("                 (+1 for the decimal point)\n");
      System.out.println("****NULL string will be returned****");
      return "";
    }//endif
    if (NumDecs == 0){
      if ((int)Num == 0){
        String Ceros = "";
        for (int i = 0; i < NumDigits; i++){
          Ceros += "0";
        }
        return Ceros;
      }//endIf
    }else{
      if (Num == 0){
        String C = "0.";
        for (int i = 0; i < NumDecs; i++){
          C += "0";
        }
        return C;
      }//endif
      NumDigits++;        // Compensa el punto decimal
    }//endif
    String N2C [] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    String sign = "";
    if (Num < 0){
      sign = "-";
      Num = -Num;
    }
    String Cadena = "", C = "", C_i;
    int Residuo;
    int iNum = (int)Num;
    int NumEnts = NumDigits - NumDecs - 1;
    for (int i = 0 ; i < NumEnts; i++){
      Residuo = iNum % 10;
      C = N2C[Residuo] + C;
      iNum = iNum / 10;
    }//endFor
    if (ClearZeros == 1){
      boolean DigitsOff = true;
      for (int i = 0; i < NumEnts; i++){
        C_i = C.substring(i, i+1);
        if (C_i.equals("0") && DigitsOff){
          Cadena += " ";
        }
        else{
          if (DigitsOff){
            DigitsOff = false;
          }
          Cadena += C_i;
        }//endif
      }//endfor
    }else
      Cadena = C;
    //endif
      if (NumDecs != 0){
      Cadena += ".";
      int Digito;
      float Dig10;
      iNum = (int)Num;
      float Decimal = Num - (float)iNum;
      for (int i = 0; i < NumDecs; i++){
        Dig10 = Decimal*10;
        Digito = (int)Dig10;
        Cadena += N2C[Digito];
        Decimal = Dig10 - (float)Digito;
      }//endFor
    }//endif
    Cadena = sign + ltrim(Cadena);
    return Cadena;
  }//endNum2String
  
  public static String ltrim (String Cadena){
    String C = "", C_i;
    for (int i = 0; i < Cadena.length(); i++){
      C_i = Cadena.substring(i, i+1);
      if (!C_i.equals(" ")) 
        C = C + C_i;
    }
    return C;
  }
  
  public static void main(String[] args) throws Exception {
    String Test;
    float Num;
    for (int i=0; i<20; i++){
      Num = (float)i*3.1416f-50;
      Test = Num2String(Num,15,7,1);
      System.out.printf( "Numero = %15.7f ", Num);
      System.out.println("Cadena = " + Test);
    }//endFor
    System.out.println("=================== Otro test ======================");
    Num = (float)0.4861727808;
    Test = Num2String(Num,11,9,1);
    System.out.printf( "Numero = %11.9f ", Num);
    System.out.println("Cadena = " + Test);
    Num = (float)0.3540708934;
    Test = Num2String(Num,11,9,1);
    System.out.printf( "Numero = %11.9f ", Num);
    System.out.println("Cadena = " + Test);
  }//endMain
} //endClass
