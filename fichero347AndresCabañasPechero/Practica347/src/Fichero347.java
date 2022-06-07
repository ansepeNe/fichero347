import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Fichero347 {
	
	static conexion cn = new conexion();
	static Properties config = ficheroConfiguracion();
	
	public static void main(String[]args) throws ClassNotFoundException, SQLException {
		
		
		Scanner sc = new Scanner(System.in);

		String ayoFichero = "";
		String numDeclaracion = "";
		System.out.println("==========================================================");
		System.out.println("Introduce el AÑO sobre el que se va a Generar el Fichero: ");
		System.out.println("==========================================================");
		ayoFichero = sc.nextLine();
		
		//Abro una conexion con mi base de DeclaracionesEmitidas
		Connection con_BBDDDeclaracionesEmitidas = cn.getConexion2BBDD();
		Statement stDeclaracion = con_BBDDDeclaracionesEmitidas.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
		ResultSet rsD = null;
		
		//En este bucle compruebo si el Numero de Declaracion ya existe, haciendo un select a mi BBDD
		do {
			System.out.println(" ==========================================");
			System.out.println("|Introduce un NUMERO de declaracion UNICO: |");
			System.out.println(" ==========================================");
			numDeclaracion = sc.nextLine();
			
			rsD = stDeclaracion.executeQuery("select NumDeclaracion from control where NumDeclaracion ="+ numDeclaracion);
			rsD.last();
			
			if(rsD.getRow() > 0) {	
				System.out.println("");
				System.out.println("ESE NUMERO DE DECLARACION YA EXISTE");	
			}
			
		}while(rsD.getRow() > 0);
		
		
		String consultaTrimestresPivote = "SELECT\r\n"
				+ "    Ejercicio,\r\n"
				+ "    NIF,\r\n"
				+ "    Nombre,\r\n"
				+ "    Cp,\r\n"
				+ "    Provincia,\r\n"
				+ "    Pais,\r\n"
				+ "    TipoIva,\r\n"
				+ "    ISNULL([1],0) AS Total1,\r\n"
				+ "    ISNULL([2],0) AS Total2,\r\n"
				+ "    ISNULL([3],0) AS Total3,\r\n"
				+ "    ISNULL([4],0) AS Total4,\r\n"
				+ "    ISNULL([1],0) + ISNULL([2],0) + ISNULL([3],0) + ISNULL([4],0) AS Total\r\n"
				+ "FROM  \r\n"
				+ "(\r\n"
				+ "    SELECT\r\n"
				+ "        YEAR(T0.RefDate) AS Ejercicio,\r\n"
				+ "        DATEPART(QUARTER, T0.RefDate) Tri,\r\n"
				+ "        IC.LicTradNum AS Nif, \r\n"
				+ "        IC.CardName	AS Nombre,\r\n"
				+ "    	IC.ZipCode AS Cp,\r\n"
				+ "        IIF(IC.Country = 'ES', IIF(ISNUMERIC(IC.ZipCode) = 1, FORMAT(CONVERT(INT, IC.ZipCode) / 1000, '00'), ''), '99') AS Provincia,\r\n"
				+ "        IIF(IC.Country = 'ES', '', IC.Country) AS Pais,\r\n"
				+ "        IIF(TI.Category = 'I', 'A', 'B') AS TipoIva,\r\n"
				+ "        SUM(IIF(T1.Debit - T1.Credit = 0, SIGN(T0.LocTotal), IIF(TI.Category = 'I', -1, 1) * SIGN(T1.Credit - T1.Debit)) * (ABS(T1.BaseSum) + ABS(T1.Debit - T1.Credit))) AS Total\r\n"
				+ "    FROM OJDT T0 (NOLOCK)\r\n"
				+ "    LEFT JOIN JDT1 T1 (NOLOCK) ON T0.TransId = T1.TransId\r\n"
				+ "    LEFT JOIN OCRD IC (NOLOCK) ON T1.ContraAct = IC.CardCode\r\n"
				+ "    LEFT JOIN OVTG TI (NOLOCK) ON T1.VatGroup = TI.Code\r\n"
				+ "    WHERE YEAR(T0.RefDate) = "+ayoFichero+" AND T1.VatLine = 'Y' AND T1.EquVatRate = 0 AND TI.R349Code = 0\r\n"
				+ "    GROUP BY YEAR(T0.RefDate), DATEPART(QUARTER, T0.RefDate), IC.LicTradNum, IC.CardName, TI.Category, IC.Country, IC.ZipCode\r\n"
				+ ") AS SourceTable  \r\n"
				+ "PIVOT  \r\n"
				+ "( \r\n"
				+ "  SUM(Total)\r\n"
				+ "  FOR Tri IN ([1], [2], [3], [4])  \r\n"
				+ ") AS PivotTable\r\n"
				+ "WHERE ABS(ISNULL([1],0) + ISNULL([2],0) + ISNULL([3],0) + ISNULL([4],0)) > 3005.06\r\n"
				+ "ORDER BY Nombre";
		//Abro mi conexion a la base de datos SBODemoES
		Connection con = cn.getConexion();
		
		int contadorTipo2 = 0;
		Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);				
		ResultSet rs = st.executeQuery(consultaTrimestresPivote);
		
		//Preparo suma operaciones en euro para mas delante pintarla
		int sumaOperacionesEuro = 0;
		while(rs.next()) {
			sumaOperacionesEuro += rs.getInt("Total");
		}
		//En el pdf pide que lo multipliquemos por 100 y sin decimales
		sumaOperacionesEuro = sumaOperacionesEuro * 100;

		//Creo mi fichero con mi ruta que esta en mi fichero de configuracion
		File fichero347 = new File(config.getProperty("fichero347"));

		try {
			
			String NIFDeclarante = "";
			String NombreDeclarante = "";
			String TelefonoContacto = "";
			String NIFDeclarado = "";
			String PersonaContacto = "";
			String razonSocialDeclarado = "";
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(fichero347));
			
			//Tipo Registro + fichero + año
			bw.write("1347"+ayoFichero);
			
			String consultaNIFDeclarante = "select TaxIdNum, CompnyName, Phone1 from OADM";
			//Llamo a mi metodo que me hace las consultas 
			ResultSet rs1 = cn.realizarSQL(consultaNIFDeclarante,con);

			try {
				while(rs1.next()) {
				
					NIFDeclarante = rs1.getString("TaxIdNum");
					NombreDeclarante = rs1.getString("CompnyName");
					TelefonoContacto = rs1.getString("Phone1");
				}			
			} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}	

			//NIF Declarante
			NIFDeclarante = longitudCadenas(NIFDeclarante, 9);
			bw.write(NIFDeclarante);
			
			//NombreDeDeclarante
			NombreDeclarante = longitudCadenas(NombreDeclarante, 40);
			bw.write(NombreDeclarante);
			
			//TipoSoporte
			bw.write("T");
			
			//TelefonoContacto
			TelefonoContacto = longitudCadenas(TelefonoContacto, 9);
			bw.write(TelefonoContacto);
			
			//PersonaContacto
			bw.write("                                       ");
			
			//Numero Declaracion
			numDeclaracion = longitudCadenas(numDeclaracion, 13);
			bw.write(numDeclaracion);
			
			//TipoDeclaracion
			bw.write(" ");
			//TipoDeclaracion
			bw.write(" ");
			
			//Numero Declaracion Anterior
			bw.write(numDeclaracion);
			
			
			//Acumulo en un contador la cantidad de registros que tengo en ese año
			rs.last();	
			contadorTipo2 = rs.getRow();
			
			//Cantidad de Registros Tipo 2 (contador registros 2)
			String Tipo2 = longitudCadenas(contadorTipo2+"", 9);
			bw.write(Tipo2);
			
			//Signo suma operaciones
			//Si el total de "Suma operaciones en euros" es negativo, pinto una N, sino lo dejo en blanco
			if(sumaOperacionesEuro < 0) {
				bw.write("N");
			}else{
				bw.write(" ");	
			}
			
			//sumas operaciones en euros	
			String TXTsumaOperaciones = longitudCadenas(sumaOperacionesEuro+"", 15);
			bw.write(TXTsumaOperaciones);
					
			//Preparo ciertos datos que me van hacer falta en el FOR de mas abajo para pintarlos directamente		
			Statement st5 = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
			ResultSet rs5 = st5.executeQuery(consultaTrimestresPivote);
			
			String PersonaSocialDeclarado = "";
			String codigoProvincia = "";
			String codigoPais = "";
			String tipoIvaAB = "";
			String total = "";
			String pais = "";
			String nifIntraComunitario = "";
			 
			String T1 = "";
			String T2 = "";
			String T3 = "";
			String T4 = "";
			
			while(rs5.next()) {
				
				bw.newLine();
				//Tipo Registro + Fichero + Año
				bw.write("2347" + ayoFichero);
				
				
				//NIF Declarante
				NIFDeclarante = longitudCadenas(NIFDeclarante,9);
				
				bw.write(NIFDeclarante);
				
				
				//NIFDeclarado
				NIFDeclarado = rs5.getString("NIF");
				

				
				if(NIFDeclarado == null) {					
					bw.write("         ");
				}else {
					//En el PDF pone que solo pintemos los ultimos 9 caracteres del NIFDeclarado
					NIFDeclarado = NIFDeclarado.substring(2);
					bw.write(NIFDeclarado);
				}
				
				//NIF Representante Declarado
				bw.write("         ");

				

				//Razon Social Declarado
				PersonaSocialDeclarado = rs5.getString("Nombre");
				
				if(PersonaSocialDeclarado == null) {
					//40 espacios
					bw.write("                                        ");
				}else {
					
					PersonaSocialDeclarado = longitudCadenas(PersonaSocialDeclarado,40);
					bw.write(PersonaSocialDeclarado + "D");
				}
				
				//CodigoProvincia
				codigoProvincia = rs5.getString("Provincia");
				
				if(codigoProvincia == null) {
					bw.write("  ");
				}else {
					
					codigoProvincia = longitudCadenas(codigoProvincia,2);
					bw.write(codigoProvincia);
				}
				
				
				//CodigoPais
				codigoPais = rs5.getString("Pais");
				
				//Si es nulo o si es de ESPAÑA pinto blanco
				if(codigoPais == null || codigoPais.equals("ES")) {
					bw.write("  ");
				}else {
					codigoPais = longitudCadenas(codigoPais,2);
					bw.write(codigoPais);
				}
				
				
				//En blanco
				bw.write("");

				//A para compras B para ventas
				tipoIvaAB = rs5.getString("TipoIva");
				
				
				if(tipoIvaAB == null) {
					bw.write(" ");
				}else {
					tipoIvaAB = longitudCadenas(tipoIvaAB,2);
					bw.write(tipoIvaAB);
				}
				
				//Signo Total Operacion 
				bw.write(" ");
				
				
				//Importe total anual				
				total = rs5.getString("Total");
				total = longitudCadenas(total, 15);				
				bw.write(total);
				
				//signo total
				bw.write("");
				
				//Trimestre1
				T1 = rs5.getString("Total1");
					
				T1 = longitudCadenas(T1, 15);
				bw.write(T1);
				
				//Signo total inmuebles	//Total inmuebles
				totalInmueble(bw);
			
				//signo total
				bw.write("");
				
				//Trimestre2
				T2 = rs5.getString("Total2");				
				T2 = longitudCadenas(T2, 15);
				bw.write(T2);
				
				//Signo total inmuebles	//Total inmuebles
				totalInmueble(bw);
								
				//Trimestre3
				T3 = rs5.getString("Total3");
				
				T3 = longitudCadenas(T3, 15);
				bw.write(T3);
				
				//Signo total inmuebles	//Total inmuebles
				totalInmueble(bw);
				
				
				//Trimestre4
				T4 = rs5.getString("Total4");
				
				T4 = longitudCadenas(T4, 15);
				bw.write(T4);
								
			}
			bw.flush();
		
			
			//Cierro la conexion con la BBDD SBODemoES
			con.close();
			
			//prepar insert
			String sentenciaInsertar = "INSERT into control(Declaracion,Periodo,NumDeclaracion,Tipo,GeneracionHora) VALUES ("+347+",'"+ayoFichero+"',"+numDeclaracion+",'Inicial',GETDATE())";
				  
			int validacion = cn.realizarSQLDeclaracionBBDD(sentenciaInsertar,con_BBDDDeclaracionesEmitidas);
				
			con.close();
			    
			if(validacion == 1) {
			    	
			    	System.out.println("Inserccion en la BBDD DeclaracionEmitidas realizada CORRECTAMENTE");
			}else {
			    	System.out.println("Algo ha FALLADO");
			    	
			}
				
		    con_BBDDDeclaracionesEmitidas.close();

		}catch(FileNotFoundException fn) {
			System.out.println("El fichero no existe");

		}catch(IOException e) {
			
			System.out.println("Error de E/S");
		}
		
	
	}

	private static void totalInmueble(BufferedWriter bw) throws IOException {
		bw.write("0" + "000000000000000");
	}
	
	//Metodo para que los string sean de la longitud que quiero
	public static String longitudCadenas(String cadena, int longitud) {
		
		StringBuffer sb = new StringBuffer(cadena);
		
		sb.setLength(longitud);
		
		return sb.toString();
		
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
