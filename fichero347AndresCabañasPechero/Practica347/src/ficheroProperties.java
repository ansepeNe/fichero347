import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class ficheroProperties {

public static void main(String[] args) {
		
		Properties propiedades = new Properties();

		try {

			propiedades.setProperty("password", "12345Ab##");
			propiedades.setProperty("usuario", "sa");
			propiedades.setProperty("servidor", "localhost");
			propiedades.setProperty("puerto",  "1433");
			propiedades.setProperty("nombre", "SBODemoES");
			propiedades.setProperty("nombreBBDD2", "DeclaracionesEmitidas");
			
			propiedades.setProperty("fichero347", "C:\\ficheros\\fichero347\\fichero347.txt");
			propiedades.store(new FileWriter("C:\\ficheros\\fichero347\\configuracion.properties"), "Fichero de Configuracion");
			
		}catch(FileNotFoundException e) {
			
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println("FICHERO ACTUALZIADO CON EXITO!");
		
	}
	
}
