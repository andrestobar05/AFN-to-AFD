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
import java.util.stream.IntStream;

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
	public boolean accept(String cadena) {
		// Verificar que cada caracter de la cadena esta en el alfabeto
		for (char c : cadena.toCharArray()) {
			if (!new String(alfabeto).contains(String.valueOf(c))) {
				return false;
			}
		}
		/* Se inicializa el conjunto de estados actuales con la clausura lambda del estado inicial
		   porque puede empezar en un solo estado inicial o en varios estados iniciales por las transiciones lambda  */
		Set<Integer> estadosActuales = clausuraLambda(Collections.singleton(estadoInicial));
		// Llamar al metodo recursivo
		return acceptRecursivo(cadena, estadosActuales);
	}
	
	private boolean acceptRecursivo(String cadena, Set<Integer> estadosActuales) {
		// Caso base: Si la cadena esta vacia, verificar si el conjunto de estados actuales contiene algún estado final
		if (cadena.isEmpty()) {
			for (int estadoFinal : estadosFinales) {
				if (estadosActuales.contains(estadoFinal)) {
					return true;
				}
			}
			return false;
		}
	
		// Procesar el primer caracter de la cadena
		char caracter = cadena.charAt(0);
		// Calcular el conjunto de estados a los que se puede llegar con el caracter actual
		Set<Integer> nuevosEstados = cambio(estadosActuales, caracter);
		// Calcular la clausura lambda del nuevo conjunto de estados
		Set<Integer> estadosSiguientes = clausuraLambda(nuevosEstados);
	
		// Recursion con el resto de la cadena (sin el primer caracter) y el nuevo conjunto de estados
		return acceptRecursivo(cadena.substring(1), estadosSiguientes);
	}

	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	public void toAFD(String afdPath) {
		Set<Set<Integer>> estadosAFD = new HashSet<>();
		Map<Set<Integer>, Integer> mapeoEstados = new HashMap<>();
		Set<Set<Integer>> estadosFinalesAFD = new HashSet<>();
		Set<Integer> estadoInicialAFD = clausuraLambda(Collections.singleton(estadoInicial));
	
		// Agregar estado de error
		Set<Integer> estadoError = new HashSet<>();
		estadoError.add(0);
		estadosAFD.add(estadoError);
		mapeoEstados.put(estadoError, 0); // estado de error es 0
	
		estadosAFD.add(estadoInicialAFD);
		mapeoEstados.put(estadoInicialAFD, 1); // estado inicial es 1
		int nuevoEstado = 2;
	
		// Calcular los nuevos estados del AFD
		boolean seAñadioNuevoEstado;
		do {
			seAñadioNuevoEstado = false;
			Set<Set<Integer>> nuevosEstados = new HashSet<>();
			for (Set<Integer> estadoAFD : estadosAFD) {
				for (char simbolo : alfabeto) {
					Set<Integer> nuevoEstadoAFD = cambio(estadoAFD, simbolo);
					nuevoEstadoAFD = clausuraLambda(nuevoEstadoAFD);
		
					if (!estadosAFD.contains(nuevoEstadoAFD) && !nuevosEstados.contains(nuevoEstadoAFD)) {
						nuevosEstados.add(nuevoEstadoAFD);
						mapeoEstados.put(nuevoEstadoAFD, nuevoEstado);
						nuevoEstado++;
						seAñadioNuevoEstado = true;
					}
				}
			}
			estadosAFD.addAll(nuevosEstados);
			// Determinar los estados finales
			for (Set<Integer> estadoAFD : estadosAFD) {
				for (Integer estadoFinal : estadosFinales) {
					if (estadoAFD.contains(estadoFinal)) {
						estadosFinalesAFD.add(estadoAFD);
						break;
					}
				}
			}
			
		} while (seAñadioNuevoEstado);
	
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(afdPath))) {
			String alfabetoString = new String(alfabeto);
			String alfabetoOutput = String.join(",", alfabetoString.chars().mapToObj(c -> String.valueOf((char) c)).collect(Collectors.toList()));
			bw.write(alfabetoOutput);
			bw.newLine();
	
			String estadosSize = String.valueOf(estadosAFD.size());
			bw.write(estadosSize);
			bw.newLine();
	
			String estadosFinalesOutput = String.join(",", estadosFinalesAFD.stream().map(estado -> String.valueOf(mapeoEstados.get(estado))).collect(Collectors.toList()));
			bw.write(estadosFinalesOutput);
			bw.newLine();
	
			for (int i = 0; i < alfabeto.length; i++) {
				final int symbolIndex = i;
				String matrizTransicionOutput = String.join(",", estadosAFD.stream().map(estado -> {
					Set<Integer> estadoDestino = cambio(estado, alfabeto[symbolIndex]);
					if (!estadosAFD.contains(estadoDestino)) {
						return "0";
					}
					return String.valueOf(mapeoEstados.get(estadoDestino));
				}).collect(Collectors.toList()));
				bw.write(matrizTransicionOutput);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Metodo para calcular la clausura lambda de un conjunto de estados
	private Set<Integer> clausuraLambda(Set<Integer> estados) {
		/* Se inicializa un set para guardar la clausura lambda, inicialmente con los estados 
		dados porque todos los estados tienen clausura lambda con ellos mismos */
		Set<Integer> clausura = new HashSet<>(estados);
		// Se inicializa un booleano para saber si hubo un cambio en la clausura
		boolean cambio = true;
		while (cambio) {
			// Verificar si hay cambios en la clausura
			cambio = false;
			for (int estado : clausura) {
				String[] estadosLambda = matrizTransicion[0][estado].split(";");
				for (String estadoLambda : estadosLambda) {
					// Si el estado lambda no esta en la clausura, se agrega y se marca cambio como verdadero
					if (!estadoLambda.equals("") && clausura.add(Integer.parseInt(estadoLambda))) {
						cambio = true;
					}
				}
			}
		}
		return clausura;
	}

	// Metodo para calcular el conjunto de estados alcanzables por un simbolo desde un conjunto de estados
	private Set<Integer> cambio(Set<Integer> estados, char simbolo) {
		// Se inicializa un set para guardar los estados alcanzables
		Set<Integer> nuevosEstados = new HashSet<>();
		for (int estado : estados) {
			// Se busca el indice del simbolo en el alfabeto
			int index = searchIndex(alfabeto, simbolo);
			String[] estadosSiguientes = matrizTransicion[index + 1][estado].split(";");
			for (String estadoSiguiente : estadosSiguientes) {
				if (!estadoSiguiente.equals("")) {
					nuevosEstados.add(Integer.parseInt(estadoSiguiente));
				}
			}
		}
		return nuevosEstados;
	}

	private int searchIndex(char[] array, char target) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == target) {
				return i;
			}
		}
		return -1;
	}
	/*
		El metodo main debe recibir como primer argumento el path
		donde se encuentra el archivo ".afd" y debe empezar a evaluar 
		cuerdas ingresadas por el usuario una a una hasta leer una cuerda vacia (""),
		en cuyo caso debe terminar. Tiene la libertad de implementar este metodo
		de la forma que desee. Si se envia la bandera "-to-afd", entonces en vez de
		evaluar, debe generar un archivo .afd
	*/
	public static void main(String[] args) throws Exception{
		if (args.length < 1) {
			System.out.println("No se proporciono el archivo de entrada.");
			return;
		}
		// Crear un AFN con el archivo de entrada
		AFN afn = new AFN(args[0]);
		String bandera = null;
		if (args.length > 1) {
			bandera = args[1];
		}
		if (bandera != null && bandera.equals("-to-afd")){
			String pathArchivoAFD = null;
			if (args.length > 2) {
				pathArchivoAFD = args[2];
			}
			// Si no se da el path, se crea uno por defecto
			if (pathArchivoAFD == null) {
				pathArchivoAFD = "./AFDconvertido.afd";
			}
			// Crear un AFD con el archivo de entrada
			afn.toAFD(pathArchivoAFD);
			System.out.printf("Se ha creado el archivo AFD exitosamente en: %s.\n", pathArchivoAFD);
		}else {
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
		}
	}
}