import java.sql.*;

public class Conexion {
	Connection conexion;
	boolean correcto;
	String mensajeError;
	public Conexion(String servidor, String puerto, String user, String pass){
		correcto=true;
		mensajeError="";
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}catch(ClassNotFoundException e){
			correcto=false;
			mensajeError= e.getMessage();
		}
		try{
			conexion = DriverManager.getConnection("jdbc:oracle:thin:@"+servidor+":"+puerto+":xe",user,pass);
		}catch(SQLException e){
			correcto=false;
			mensajeError = e.getErrorCode() + ": " + e.getMessage();
		}
	}
}
