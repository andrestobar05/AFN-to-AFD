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
		String[] contenido = readFile(path).split("\n");
		alfabeto = contenido[0].split(",");
		cantEstados = Integer.parseInt(contenido[1]);
		estadosFinales = new int[contenido[2].split(",").length];
		for (int i = 0; i < estadosFinales.length; i++) {
			estadosFinales[i] = Integer.parseInt(contenido[2].split(",")[i]);
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
	public boolean accept(String string){
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
            System.out.println("No se proporcionó el archivo de entrada.");
            return;
        }
		// Crear un AFN con el archivo de entrada
		AFN afn = new AFN(args[0]);
		String bandera = args[1];
		if (bandera.equals("-f")) { // Archivo
			if (args.length < 3) {
				System.out.println("No se proporcionó el archivo de cuerdas.");
				return;
			}
			String pathCuerda = args[2];
			try (BufferedReader br = new BufferedReader(new FileReader(pathCuerda))) {
				String cuerda;
				while ((cuerda = br.readLine()) != null) {
					System.out.println("La cadena " + (afn.accept(cuerda) ? "es" : "no es") + " aceptada por el AFN.");
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
					System.out.println("La cadena " + (afn.accept(cuerda) ? "es" : "no es") + " aceptada por el AFN.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Bandera no reconocida.");
		}
	}
}