/*
	Utilice esta clase para guardar la informacion de su
	AFN. NO DEBE CAMBIAR LOS NOMBRES DE LA CLASE NI DE LOS 
	METODOS que ya existen, sin embargo, usted es libre de 
	agregar los campos y metodos que desee.
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AFN{

	/*
		Implemente el constructor de la clase AFN
		que recibe como argumento un string que 
		representa el path del archivo que contiene
		la informacion del AFN (i.e. "Documentos/archivo.AFN").
		Puede utilizar la estructura de datos que desee
	*/
	private char[] alfabeto;
    private int cantEstados;
    private int[] estadosFinales;
	private String[][] matrizTransicion;
	private int estadoInicial = 1;


	public AFN(String path){
		// Se lee el archivo y se almacena la informacion en las variables
		String[] contenido = readFile(path).split("\n");
		// Se guarda el alfabeto
		String[] alfabetoStrings = contenido[0].split(",");
		alfabeto = new char[alfabetoStrings.length];
		for (int i = 0; i < alfabetoStrings.length; i++) {
			alfabeto[i] = alfabetoStrings[i].charAt(0);
		}
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
	public boolean accept(String string) {
		return acceptHelper(string, estadoInicial);
	}
	
	private boolean acceptHelper(String string, int estadoActual) {
		if (string.isEmpty()) {
			for (int estadoFinal : estadosFinales) {
				if (estadoActual == estadoFinal) {
					return true;
				}
			}
			return false;
		}
	
		char caracter = string.charAt(0);
		int indiceCaracter = -1;
		for (int i = 0; i < alfabeto.length; i++) {
			if (alfabeto[i] == caracter) {
				indiceCaracter = i;
				break;
			}
		}
	
		String cuerdaRestante = string.substring(1);
		String[] estadosCaracter = matrizTransicion[indiceCaracter + 1][estadoActual].split(";");
		String[] estadosLambda = matrizTransicion[0][estadoActual].split(";");
	
		Set<String> estadosTotales = new HashSet<>();
		Collections.addAll(estadosTotales, estadosCaracter);
		Collections.addAll(estadosTotales, estadosLambda);
	
		for (String estado : estadosTotales) {
			if (!estado.equals("")) {
				int nuevoEstado = Integer.parseInt(estado);
				if (acceptHelper(cuerdaRestante, nuevoEstado)) {
					return true;
				}
			}
		}

		return false;
	}
	
	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	public void toAFD(String afdPath) {
		// Se inicializa un set de sets para guardar los estados del AFD
		// Esto se hace porque no queremos estados repetidos
		Set<Set<Integer>> estadosAFD = new HashSet<>();
		// Se inicializa un mapeo de sets de estados a enteros para asignar un número a cada estado
		Map<Set<Integer>, Integer> mapeoEstados = new HashMap<>();
		// Se inicializa un set de sets para guardar los estados finales del AFD
		Set<Set<Integer>> estadosFinalesAFD = new HashSet<>();

		// Calcular la clausura lambda del estado inicial
		Set<Integer> estadoInicialAFD = clausuraLambda(Collections.singleton(estadoInicial));

		// Inicializar la lista de estados del AFD con el estado inicial
		estadosAFD.add(estadoInicialAFD);
		mapeoEstados.put(estadoInicialAFD, 1); // estado inicial es 1

		// Calcular los nuevos estados del AFD
		int nuevoEstado = 2; // comenzar desde 2
		// Se convierte el arreglo de estados finales a un set para facilitar la búsqueda
		Set<Integer> estadosFinalesSet = Arrays.stream(estadosFinales).boxed().collect(Collectors.toSet());

		for (Set<Integer> estadoAFD : estadosAFD) {
			for (char simbolo : alfabeto) {
				Set<Integer> nuevoEstadoAFD = cambio(estadoAFD, simbolo);
				nuevoEstadoAFD = clausuraLambda(nuevoEstadoAFD);

				if (!estadosAFD.contains(nuevoEstadoAFD)) {
					estadosAFD.add(nuevoEstadoAFD);
					mapeoEstados.put(nuevoEstadoAFD, nuevoEstado);

					if (nuevoEstadoAFD.stream().anyMatch(estado -> estadosFinalesSet.contains(estado))) {
						estadosFinalesAFD.add(nuevoEstadoAFD);
					}

					nuevoEstado++;
				}
			}
		}
		// Se escribe el AFD en un archivo
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(afdPath))) {
			// Escribir el alfabeto
			bw.write(String.join(",", Arrays.stream(alfabeto).mapToObj(String::valueOf).collect(Collectors.toList())));
			bw.newLine();
			// Escribir la cantidad de estados
			bw.write(String.valueOf(estadosAFD.size()));
			bw.newLine();
			// Escribir los estados finales
			bw.write(String.join(",", estadosFinalesAFD.stream().map(estado -> String.valueOf(mapeoEstados.get(estado))).collect(Collectors.toList())));
			bw.newLine();
			// Escribir la matriz de transición
			for (int i = 0; i < alfabeto.length; i++) {
				bw.write(String.join(",", estadosAFD.stream().map(estado -> String.join(";", cambio(estado, alfabeto[i]).stream().map(String::valueOf).collect(Collectors.toList()))).collect(Collectors.toList()));
				bw.newLine();
			}
			bw.write(String.join(",", estadosAFD.stream().map(estado -> String.join(";", clausuraLambda(estado).stream().map(String::valueOf).collect(Collectors.toList()))).collect(Collectors.toList()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Método para calcular la clausura lambda de un conjunto de estados
	private Set<Integer> clausuraLambda(Set<Integer> estados) {
		// Implementar el cálculo de la clausura lambda
	}

	// Método para calcular el conjunto de estados alcanzables por un símbolo desde un conjunto de estados
	private Set<Integer> cambio(Set<Integer> estados, char simbolo) {
		// Implementar el cálculo del conjunto de estados alcanzables por un símbolo
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