/*
	Utilice esta clase para guardar la informacion de su
	AFN. NO DEBE CAMBIAR LOS NOMBRES DE LA CLASE NI DE LOS 
	METODOS que ya existen, sin embargo, usted es libre de 
	agregar los campos y metodos que desee.
*/
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AFN{

	/*
		Implemente el constructor de la clase AFN
		que recibe como argumento un string que 
		representa el path del archivo que contiene
		la informacion del AFN (i.e. "Documentos/archivo.AFN").
		Puede utilizar la estructura de datos que desee
	*/
	private String[] alfabeto;
    private int cantEstados;
    private int[] estadosFinales;
	private String[][] matrizTransicion;


	public AFN(String path){
		// Se lee el archivo y se almacena la informacion en las variables
		String[] contenido = readFile(path).split("\n");
		// Se guarda el alfabeto
		alfabeto = contenido[0].split(",");
		// Se guarda la cantidad de estados
		cantEstados = Integer.parseInt(contenido[1]);
		// Se inicializa el arreglo de estados finales
		estadosFinales = new int[contenido[2].split(",").length];
		// Se guardan los estados finales
		for (int i = 0; i < estadosFinales.length; i++) {
			estadosFinales[i] = Integer.parseInt(contenido[2].split(",")[i]);
		}
		// Se guarda la matriz de transicion
		matrizTransicion = new String[alfabeto.length + 1][cantEstados];
		for (int row = 0; row <= alfabeto.length; row++) {
			String[] transiciones = contenido[row + 3].split(","); // Se suma 3 porque las primeras 3 lineas no son transiciones
			for (int col = 0; col < cantEstados; col++) {
				matrizTransicion[row][col] = transiciones[col]; // Se guarda la transicion en la matriz
			}
		}
	}


	public String readFile(String path){
		// Implementar la lectura del archivo
		StringBuilder contenido = new StringBuilder();
		try (BufferedReader buff = new BufferedReader(new FileReader(path))) {
            String linea;
			// Leer el archivo linea por linea y almacenar el contenido en un StringBuilder
            while ((linea = buff.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contenido.toString();
	}

	/*
		Implemente el metodo accept, que recibe como argumento
		un String que representa la cuerda a evaluar, y devuelve
		un boolean dependiendo de si la cuerda es aceptada o no 
		por el AFN. Recuerde lo aprendido en el proyecto 1.
	*/
	public boolean accept(String string, int estadoActual){
		// Caso base: si la cadena está vacía, verificamos si el estado actual es un estado de aceptación
		if (string.isEmpty()) {
			for (int estadoFinal : estadosFinales) {
				if (estadoActual == estadoFinal) {
					return true;
				}
			}
			return false;
		}
	
		// Se obtiene el primer caracter de la cuerda
		String caracter = string.substring(0, 1);
		// Se obtiene el indice del caracter en el alfabeto
		int indiceCaracter = -1;
		for (int i = 0; i < alfabeto.length; i++) {
			if (alfabeto[i].equals(caracter)) {
				indiceCaracter = i;
				break;
			}
		}
	
		// Se obtiene la cuerda restante
		String cuerdaRestante = string.substring(1);
	
		// Se recorre la matriz de transicion
		for (int i = 0; i < cantEstados; i++) {
			// Se obtienen los estados a los que se transiciona con el caracter
			String[] estadosCaracter = matrizTransicion[indiceCaracter + 1][i].split(";");
			for (String estado : estadosCaracter) {
				if (estado.equals("0")) {
					if (accept(cuerdaRestante, i)) {
						return true;
					}
				}
			}
			// Se obtienen los estados a los que se transiciona con lambda
			String[] estadosLambda = matrizTransicion[0][i].split(";");
			for (String estado : estadosLambda) {
				if (estado.equals("0")) {
					if (accept(string, i)) {
						return true;
					}
				}
			}
		}
	
		// Si no se acepta la cuerda
		return false;
	}

	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	public void toAFD(String afdPath){
	}

	/*
		El metodo main debe recibir como primer argumento el path
		donde se encuentra el archivo ".afd", como segundo argumento 
		una bandera ("-f" o "-i"). Si la bandera es "-f", debe recibir
		como tercer argumento el path del archivo con las cuerdas a 
		evaluar, y si es "-i", debe empezar a evaluar cuerdas ingresadas
		por el usuario una a una hasta leer una cuerda vacia (""), en cuyo
		caso debe terminar. Tiene la libertad de implementar este metodo
		de la forma que desee. 
	*/
	public static void main(String[] args) throws Exception{
		if (args.length < 1) {
            System.out.println("No se proporciono el archivo de entrada.");
            return;
        }
		// Crear un AFN con el archivo de entrada
		AFN afn = new AFN(args[0]);
		String bandera = args[1];
		if (bandera.equals("-f")) { // Archivo
			if (args.length < 3) {
				System.out.println("No se proporciono el archivo de cuerdas.");
				return;
			}
			String pathCuerda = args[2];
			try (BufferedReader br = new BufferedReader(new FileReader(pathCuerda))) {
				String cuerda;
				while ((cuerda = br.readLine()) != null) {
					System.out.println("La cuerda " + (afn.accept(cuerda) ? "es" : "no es") + " aceptada por el AFN.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (bandera.equals("-i")) { // Interactivo
			try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
				String cuerda;
				while (true) {
					System.out.print("Ingrese una cuerda (o una cuerda vacía para salir): ");
					cuerda = br.readLine();
					if (cuerda.length() == 0) {
						break;
					}
					System.out.println("La cuerda " + (afn.accept(cuerda) ? "es" : "no es") + " aceptada por el AFN.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Bandera no reconocida.");
		}
	}
}