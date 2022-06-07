import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class conexion {

	static Properties config = ficheroConfiguracion();
	
	//Conectarme a la base de datos
		public static Connection getConexion(){
			try {
				
				String cadConexion = "jdbc:sqlserver://"+config.getProperty("servidor")+":"+config.getProperty("puerto")+";database="+config.getProperty("nombre")+";user="+config.getProperty("usuario")+";password="+config.getProperty("password")+";encrypt=false";
				
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				Connection con = DriverManager.getConnection(cadConexion);
				System.out.println("");
				System.out.println("| Conexion a la BBDD"+config.getProperty("nombre")+" correcta |");
				System.out.println("");
				return con;
			}catch(SQLException e) {
				e.printStackTrace();
				return null;
			}catch(ClassNotFoundException c) {
				c.printStackTrace();
				return null;
			}
		}
		
		
		//Conectarme a la base de datos
				public static Connection getConexion2BBDD(){
					try {
						
						String cadConexion = "jdbc:sqlserver://"+config.getProperty("servidor")+":"+config.getProperty("puerto")+";database="+config.getProperty("nombreBBDD2")+";user="+config.getProperty("usuario")+";password="+config.getProperty("password")+";encrypt=false";
						
						Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
						Connection con = DriverManager.getConnection(cadConexion);
						System.out.println("");
						System.out.println("| Conexion a la BBDD"+config.getProperty("nombreBBDD2")+" correcta |");
						
						return con;
					}catch(SQLException e) {
						e.printStackTrace();
						return null;
					}catch(ClassNotFoundException c) {
						c.printStackTrace();
						return null;
					}
				}
		
	
		//Metodo para realizar consultas
		public ResultSet realizarSQL(String consulta, Connection con) throws ClassNotFoundException {
		
			try {
				
				Statement sql = con.createStatement();
				ResultSet rs = sql.executeQuery(consulta);
				return rs;
				
			}catch(SQLException e) {
				e.printStackTrace();
				return null;
			}

		}
		
		
		//Metodo para realizar consultas
		public int realizarSQLDeclaracionBBDD(String consulta, Connection con) throws ClassNotFoundException {
				ResultSet rs = null;
					try {
				
						PreparedStatement ps = con.prepareStatement(consulta);
						ps.executeUpdate();
						
						return 1;
					}catch(SQLException e) {
						e.printStackTrace();
						return 0;
					}

		}
		
		public static Properties ficheroConfiguracion() {
			Properties propiedades = new Properties();
			InputStream entrada = null;
			
			try {
				
				entrada = new FileInputStream("C:\\ficheros\\fichero347\\configuracion.properties");
				propiedades.load(entrada);	
			
			}catch(FileNotFoundException e) {
				
				e.printStackTrace();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
			return propiedades;
		}
		
		
	
}
