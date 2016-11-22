import java.sql.*;

public class ConexionMysql {
	Connection conexion;
	boolean correcto;
	String mensajeError;
	public ConexionMysql(String servidor, String esquema, String user, String pass){
		correcto=true;
		mensajeError="";
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			correcto=false;
			mensajeError= e.getMessage();
		}
		try{
			conexion = DriverManager.getConnection("jdbc:mysql://"+servidor+"/"+esquema,user,pass);
		}catch(SQLException e){
			correcto=false;
			mensajeError = e.getErrorCode() + ": " + e.getMessage();
		}
	}
}
