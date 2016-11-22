import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

public class VentanaConexionMySql extends JFrame {

	private JPanel contentPane;
	private JTextField tfServidor;
	private JTextField tfEsquema;
	private JTextField tfUsuario;
	private JButton bAceptar;
	private JButton bCancelar;
	private String usuario,esq;
	private ConexionMysql bd;
	private JPasswordField tfPass;
	private VentanaPrincipal ventana;
	private ResultSet resultado;
	private JLabel lblesquema;
	private ResultSetMetaData rsmd;
	private DatabaseMetaData dbmd;
	private String tablaMysql;

	/**
	 * Create the frame.
	 */
	public VentanaConexionMySql() {
		setTitle("Parametros de conexion de MySQL");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 399, 278);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblServidor = new JLabel("Servidor:");
		lblServidor.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblServidor.setBounds(20, 23, 72, 14);
		contentPane.add(lblServidor);
		
		tfServidor = new JTextField();
		tfServidor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tfEsquema.requestFocus();
			}
		});
		tfServidor.setBounds(102, 21, 229, 20);
		contentPane.add(tfServidor);
		tfServidor.setColumns(10);
		
		lblesquema = new JLabel("Esquema:");
		lblesquema.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblesquema.setBounds(20, 66, 72, 14);
		contentPane.add(lblesquema);
		
		tfEsquema = new JTextField();
		tfEsquema.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfUsuario.requestFocus();
			}
		});
		tfEsquema.setColumns(10);
		tfEsquema.setBounds(102, 64, 229, 20);
		contentPane.add(tfEsquema);
		
		JLabel lblUsuario = new JLabel("Usuario:");
		lblUsuario.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblUsuario.setBounds(20, 112, 72, 14);
		contentPane.add(lblUsuario);
		
		tfUsuario = new JTextField();
		tfUsuario.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfPass.requestFocus();
			}
		});
		tfUsuario.setColumns(10);
		tfUsuario.setBounds(102, 110, 229, 20);
		contentPane.add(tfUsuario);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblPassword.setBounds(20, 160, 72, 14);
		contentPane.add(lblPassword);
		
		bAceptar = new JButton("Aceptar");
		bAceptar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conectar();
			}
		});
		bAceptar.setFont(new Font("Tahoma", Font.BOLD, 12));
		bAceptar.setBounds(20, 199, 89, 23);
		contentPane.add(bAceptar);
		
		bCancelar = new JButton("Cancelar");
		bCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					cerrarVentana();
				} catch (SQLException e1) {
					System.out.println(e1.getErrorCode()+": "+e1.getMessage());
				}
			}
		});
		bCancelar.setFont(new Font("Tahoma", Font.BOLD, 12));
		bCancelar.setBounds(260, 200, 89, 23);
		contentPane.add(bCancelar);
		
		tfPass = new JPasswordField();
		tfPass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				conectar();
			}
		});
		tfPass.setBounds(102, 158, 229, 20);
		contentPane.add(tfPass);
		
		esq= "";
		usuario="";
		
		tablaMysql="";
	}
	
	public void conectar(){
		String servidor = tfServidor.getText();
		esq = tfEsquema.getText();
		usuario = tfUsuario.getText();
		char[] passwd;
		String pass;
		if ((passwd=tfPass.getPassword())!=null)
			pass = String.valueOf(passwd);
		else
			pass="";
		
		bd = new ConexionMysql(servidor,esq,usuario,pass);
		
		if(bd.correcto){ //Si la conexion es correcta...
			//Exportar datos a MySQL
			exportarTabla();
		}
		else{
			JOptionPane.showMessageDialog(this, "No se ha establecido una conexion valida con el servidor.\n"+
					bd.mensajeError,"Error",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	public void setDatos(ResultSet res,String tab,VentanaPrincipal v){
		ventana = v;
		resultado = res;
		tablaMysql=tab;
	}
	
	public void exportarTabla(){
		ResultSet resDbmetadata;
		String[] nombreCols;
		boolean tablaExiste=false;
		String nombreCol, sentencia, tipoMysql;
		int tipo, numReg;
		Statement st;
		
		try{
			rsmd = resultado.getMetaData();
			st = bd.conexion.createStatement();
			
			//Obtener el nombre de las columnas
			nombreCols = new String[rsmd.getColumnCount()];
			for(int i=0;i<nombreCols.length;i++){
				nombreCols[i]=rsmd.getColumnName(i+1);
			}
			//Tenemos que sacar metadatos de las tablas para ver si la tabla existe o no
			dbmd = bd.conexion.getMetaData();
			resDbmetadata = dbmd.getTables(null, esq, "%",new String[]{"TABLE"});
			while(resDbmetadata.next())
				if(resDbmetadata.getString(3).equals(tablaMysql))
					tablaExiste=true;
			if(!tablaExiste){
				//Crear la tabla
				String sentenciaTabla="CREATE TABLE IF NOT EXISTS "+tablaMysql+" (";
				
				for(int i=0;i<nombreCols.length;i++){
					tipoMysql=rsmd.getColumnTypeName(i+1);
					if(tipoMysql.equals("NUMBER"))
						tipoMysql="NUMERIC";
					else if(tipoMysql.equals("VARCHAR") ||tipoMysql.equals("VARCHAR2"))
						tipoMysql="VARCHAR (255)";

					if(!(i==(nombreCols.length-1)))
						sentenciaTabla += nombreCols[i]+" "+ tipoMysql+",";
					else
						sentenciaTabla += nombreCols[i]+" "+ tipoMysql;
				}
				sentenciaTabla += ")";
				System.out.println(sentenciaTabla);
				numReg = st.executeUpdate(sentenciaTabla);
				if(numReg==0)
					System.out.println("Tabla creada");
			}
			
			//Insertar los distintos registros
			
			st.executeUpdate("DELETE FROM "+tablaMysql); //borrar registros anteriores
			
			while(resultado.next()){
				//Sentencia de insercion de registros
				sentencia = "INSERT INTO "+tablaMysql+" (";
				for(int i=1;i<=rsmd.getColumnCount();i++){
					if(i!=rsmd.getColumnCount())
						sentencia += nombreCols[i-1]+", ";
					else
						sentencia += nombreCols[i-1];
				}
				sentencia +=") VALUES (";
				for(int i=1;i<=rsmd.getColumnCount();i++){
					nombreCol = rsmd.getColumnName(i);
					tipo=rsmd.getColumnType(i); //devuelve el tipo sql de java
					switch(tipo){
						case Types.CHAR:
						case Types.VARCHAR:
						case Types.LONGVARCHAR: 
							sentencia +="'"+resultado.getString(i)+"'";
							if(!(i==rsmd.getColumnCount()))
								sentencia += ",";
							else
								sentencia += ")";
							break;
						
						case Types.TINYINT:
						case Types.SMALLINT:
						case Types.INTEGER: 
							sentencia +=resultado.getInt(i);
							if(!(i==rsmd.getColumnCount()))
								sentencia += ",";
							else
								sentencia += ")";
							break;

						case Types.BIGINT: 
							sentencia +=resultado.getLong(i);
							if(!(i==rsmd.getColumnCount()))
								sentencia += ",";
							else
								sentencia += ")";
							break;
						case Types.FLOAT: 
							sentencia +=resultado.getFloat(i);
							if(!(i==rsmd.getColumnCount()))
								sentencia += ",";
							else
								sentencia += ")";
							break;
						
						case Types.NUMERIC:
						case Types.DOUBLE:
						case Types.DECIMAL:
							sentencia +=resultado.getDouble(i);
							if(!(i==rsmd.getColumnCount()))
								sentencia += ",";
							else
								sentencia += ")";
							break;
						
						case Types.TIME:
						case Types.DATE:
						case Types.TIMESTAMP: 
							Date fecha;
							if((fecha=resultado.getDate(i))==null)
								sentencia += "'0000-01-01'";
							else
								sentencia += "'"+fecha+"'";
							if(!(i==rsmd.getColumnCount()))
								sentencia += ",";
							else
								sentencia += ")";
							break;
							
						default: break;
					}
				}
				//Insertar registro
				System.out.println(sentencia);
				numReg = st.executeUpdate(sentencia);
				System.out.println("Registro añadido ("+numReg+")");
			}
			if(st!=null)
				st.close();
	
			JOptionPane.showMessageDialog(this,"Datos exportados correctamente","Mensaje",JOptionPane.INFORMATION_MESSAGE);
		}catch(SQLException e){
			JOptionPane.showMessageDialog(this,e.getErrorCode() + ": " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		try {
			cerrarVentana();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,e.getErrorCode() + ": " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void cerrarVentana() throws SQLException{
		if(resultado!=null)
			resultado.close();
		ventana.setEnabled(true);
		this.setVisible(false);
	}
}
