import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class VentanaPrincipal extends JFrame {

	private JPanel contentPane;
	private JTextField tfValor;
	private JButton bEjecutar, bXML, bmysql;
	private DefaultTableModel modelo;
	private DefaultListModel<String> modeloLista;
	private JTable tabla;
	private JList<String> listaCampos;
	private JComboBox<String> chTablas, chColumnas, chOperador;
	private String usuario, tablaMysql;
	private boolean cadena, fecha;
	private SimpleDateFormat formatoFecha;
	private ArrayList<String> tiposColumnas;
	private Conexion bd;
	private DatabaseMetaData mb;
	private ResultSetMetaData rsmd;
	private JTextField tfValor2;
	private JLabel lbFech;
	private JTextArea textoSentencia;
	private JLabel lblPulseCtrl;
	private JLabel lblSeleccionarVariosCampos;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document doc;
	private Element elementoRaiz, elementoTabla, elementoRegistro, elementoNuevo;
	private Transformer xformer;
	private Source source;
	private Result result;
	private ResultSet resMysql;

	/**
	 * Create the frame.
	 */
	public VentanaPrincipal() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				cerrarConexion();
			}
		});
		setTitle("Gestion de tablas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 859, 509);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTablas = new JLabel("Tablas");
		lblTablas.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTablas.setBounds(10, 21, 46, 14);
		contentPane.add(lblTablas);
		
		chTablas = new JComboBox<String>();
		chTablas.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				cargarColumnas(chTablas.getSelectedItem().toString());
			}
		});
		chTablas.setBounds(66, 19, 147, 20);
		contentPane.add(chTablas);
		
		JLabel lblColumnas = new JLabel("Columnas");
		lblColumnas.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblColumnas.setBounds(10, 65, 70, 14);
		contentPane.add(lblColumnas);
		
		chColumnas = new JComboBox<String>();
		chColumnas.addItemListener(new ItemListener() {
			public void itemStateChanged (ItemEvent e) {
				if(!tiposColumnas.isEmpty())
					cargarOperadores(tiposColumnas.get(chColumnas.getSelectedIndex()));
			}
		});
		chColumnas.setBounds(76, 63, 137, 20);
		contentPane.add(chColumnas);
		
		JLabel lblOperador = new JLabel("Operador");
		lblOperador.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblOperador.setBounds(237, 67, 70, 14);
		contentPane.add(lblOperador);
		
		chOperador = new JComboBox<String>();
		chOperador.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				//primero reiniciamos los valores de tfValor2
				tfValor2.setText("");
				lbFech.setText("");
				tfValor2.setEditable(false);
				
				if(chOperador.getItemCount()>0)
					if(chOperador.getSelectedItem().toString().equals("BETWEEN")){
						tfValor2.setEditable(true);
						lbFech.setText("En caso de intervalo de fechas, introduzca las fechas "
								+ "con el siguiente formato: dd-mm-aaaa");
					}
					else
						tfValor2.setEditable(false);
			}
		});
		chOperador.setBounds(304, 63, 95, 20);
		contentPane.add(chOperador);
		
		JLabel lblValor = new JLabel("Valor");
		lblValor.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblValor.setBounds(409, 66, 46, 14);
		contentPane.add(lblValor);
		
		tfValor = new JTextField();
		tfValor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if(!tfValor2.isEditable())
					if(chColumnas.getItemCount()>0 && chOperador.getItemCount()>0)
						generarSentencia();
			}
		});
		tfValor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!tfValor2.isEditable())
					bEjecutar.requestFocus();
				else
					tfValor2.requestFocus();
			}
		});
		tfValor.setBounds(454, 63, 117, 20);
		contentPane.add(tfValor);
		tfValor.setColumns(10);
		
		bEjecutar = new JButton("Ejecutar");
		bEjecutar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				construirTabla();
			}
		});
		bEjecutar.setFont(new Font("Dialog", Font.BOLD, 12));
		bEjecutar.setBounds(724, 116, 89, 45);
		contentPane.add(bEjecutar);
		
		tabla = new JTable();
		tabla.setEnabled(false);
		tabla.setBounds(10, 178, 644, 238);
		tabla.setFillsViewportHeight(true);
		
		JScrollPane scrollPane = new JScrollPane(tabla);
		scrollPane.setBounds(10, 176, 646, 240);
		contentPane.add(scrollPane);
		
		JButton bSalir = new JButton("SALIR");
		bSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cerrarConexion();
				System.exit(0);
			}
		});
		bSalir.setFont(new Font("Dialog", Font.BOLD, 12));
		bSalir.setBounds(724, 423, 89, 23);
		contentPane.add(bSalir);
		
		JLabel lblSegundoValor = new JLabel("AND");
		lblSegundoValor.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblSegundoValor.setBounds(590, 65, 32, 14);
		contentPane.add(lblSegundoValor);
		
		tfValor2 = new JTextField();
		tfValor2.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if(chColumnas.getItemCount()>0 && chOperador.getItemCount()>0)
					generarSentencia();
			}
		});
		tfValor2.setEditable(false);
		tfValor2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bEjecutar.requestFocus();
			}
		});
		tfValor2.setColumns(10);
		tfValor2.setBounds(646, 63, 117, 20);
		contentPane.add(tfValor2);
		
		listaCampos = new JList<String>();
		listaCampos.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if(chColumnas.getItemCount()>0 && chOperador.getItemCount()>0)
					generarSentencia();
			}
		});
		listaCampos.setBounds(666, 201, 147, 182);
		listaCampos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modeloLista = new DefaultListModel<String>();
		listaCampos.setModel(modeloLista);
		
		JScrollPane scrollLista = new JScrollPane(listaCampos);
		scrollLista.setBounds(666, 201, 147, 182);
		contentPane.add(scrollLista);
		
		JLabel lbCampos = new JLabel("Campos a mostrar");
		lbCampos.setHorizontalAlignment(SwingConstants.CENTER);
		lbCampos.setFont(new Font("Tahoma", Font.BOLD, 12));
		lbCampos.setBounds(679, 176, 124, 14);
		contentPane.add(lbCampos);
		
		lbFech = new JLabel("");
		lbFech.setHorizontalAlignment(SwingConstants.CENTER);
		lbFech.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lbFech.setBounds(10, 91, 803, 14);
		contentPane.add(lbFech);
		
		textoSentencia = new JTextArea();
		textoSentencia.setEditable(false);
		textoSentencia.setBounds(10, 116, 704, 45);
		
		JScrollPane scrollPane2 = new JScrollPane(textoSentencia);
		scrollPane2.setBounds(10, 116, 704, 45);
		contentPane.add(scrollPane2);
		
		lblPulseCtrl = new JLabel("Pulse Ctrl + clic para");
		lblPulseCtrl.setHorizontalAlignment(SwingConstants.CENTER);
		lblPulseCtrl.setBounds(666, 387, 167, 14);
		contentPane.add(lblPulseCtrl);
		
		lblSeleccionarVariosCampos = new JLabel("seleccionar varios campos");
		lblSeleccionarVariosCampos.setHorizontalAlignment(SwingConstants.CENTER);
		lblSeleccionarVariosCampos.setBounds(666, 402, 167, 14);
		contentPane.add(lblSeleccionarVariosCampos);
		
		bXML = new JButton("Exportar a XML");
		bXML.setFont(new Font("Tahoma", Font.BOLD, 12));
		bXML.setEnabled(false);
		bXML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(modelo.getRowCount()>0)
					exportarXML();
			}
		});
		bXML.setBounds(104, 427, 147, 23);
		contentPane.add(bXML);
		
		bmysql = new JButton("Exportar a MySQL");
		bmysql.setFont(new Font("Tahoma", Font.BOLD, 12));
		bmysql.setEnabled(false);
		bmysql.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exportarMysql();
			}
		});
		bmysql.setBounds(384, 427, 147, 23);
		contentPane.add(bmysql);
		
		usuario = "";
		cadena = fecha = false;
		tiposColumnas= new ArrayList<String>();
		
		formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
	}
	
	public void conectarBD(Conexion con,String user){
		bd = con;
		usuario = user;
		try {
			mb = bd.conexion.getMetaData();
			cargarTablas();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,e.getErrorCode() + ": " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void cargarTablas(){
		ResultSet res;
		try {
			res = mb.getTables(null,usuario.toUpperCase(),"%",new String[]{"TABLE"});
			chTablas.removeAllItems();
			while(res.next())
				chTablas.addItem(res.getString(3));
			
			//se cierran los recursos al final...
			if(res!=null)
				res.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,e.getErrorCode() + ": " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void cargarColumnas(String nombreTabla){
		ResultSet res;
		try {
			res = mb.getColumns(null,usuario.toUpperCase(),nombreTabla.toUpperCase(),"%");
			tiposColumnas.clear();
			chColumnas.removeAllItems();
			modeloLista.clear();
			while(res.next()){
				chColumnas.addItem(res.getString(4));
				modeloLista.addElement(res.getString(4));
				tiposColumnas.add(res.getString(6));
			}
			//Cargar el indice seleccionado por defecto...
			cargarOperadores(tiposColumnas.get(chColumnas.getSelectedIndex()));
			
			if(res!=null)
				res.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,e.getErrorCode() + ": " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void cargarOperadores(String tipoDato){
		String[] items = new String[]{};
		chOperador.removeAllItems();
		cadena = fecha = false;
		switch (tipoDato){
			case "NUMBER":
				items=new String[]{"<","=",">",">=","<=","BETWEEN"};
				break; 
				
			case "DATE":
				items=new String[]{"<","=",">",">=","<=","BETWEEN"};
				cadena = fecha = true;
				break; 
				
			case "VARCHAR2": 
				items=new String[]{"LIKE","=","BETWEEN"};
				cadena = true;
				break; 
				
			default: break;
		}
		for(int i=0;i<items.length;i++)
			chOperador.addItem(items[i]);
	}
	
	public List<String> mostrarCampos(){ //para obtener los campos seleccionados de la lista
		List<String> campos = listaCampos.getSelectedValuesList();
		//Si se ha seleccionado todo o no se ha seleccionado nada...
		if(chColumnas.getItemCount() == campos.size() || listaCampos.isSelectionEmpty()){
			//Si en vez de varios se ha seleccionado un solo campo...
			if(campos.isEmpty() && !listaCampos.isSelectionEmpty()){
				campos.add(listaCampos.getSelectedValue());
				return campos;
			}
			return null;
		}
		else
			return campos;
	}
	
	 //imprimir cadena con los campos seleccionados de la lista
	public String imprimirCampos(List<String> list){
		String cad="";
		for(int i=0; i<list.size();i++){
			if(i==list.size()-1)
				cad +=list.get(i);
			else
				cad +=list.get(i)+", ";
		}
		return cad;
	}
	
	public void generarSentencia(){
		String tablaBd = chTablas.getSelectedItem().toString();
		String columnaBd = chColumnas.getSelectedItem().toString();
		String operador = chOperador.getSelectedItem().toString();
		String sentencia;
		
		List<String> camposSel = mostrarCampos();
		if(camposSel!=null)
			sentencia = "SELECT "+imprimirCampos(camposSel)+" FROM "+tablaBd+" WHERE "+columnaBd+" "
					+operador+" ";
		else
			sentencia = "SELECT * FROM "+tablaBd+" WHERE "+columnaBd+" "
				+operador+" ";
		
		if(cadena) //si el valor es una cadena o fecha (necesita comillas)...
			if(operador.equals("LIKE"))
				sentencia=sentencia+"'%"+tfValor.getText()+"%'";
			else if(operador.equals("BETWEEN"))
				if(fecha) //si es un intervalo de fechas...
					sentencia=sentencia+"TO_DATE('"+tfValor.getText()+"','dd-mm-yyyy')"
							+ " AND TO_DATE('"+tfValor2.getText()+"','dd-mm-yyyy')";
				else
					sentencia=sentencia+"'"+tfValor.getText()+"' AND '"+tfValor2.getText()+"'";
			else
				sentencia=sentencia+"'"+tfValor.getText()+"'";
		else
			if(operador.equals("BETWEEN"))
				sentencia=sentencia+tfValor.getText()+" AND "+tfValor2.getText();
			else
				sentencia=sentencia+tfValor.getText();

		textoSentencia.setText(sentencia);
	}
	
	public void construirTabla(){
		Statement st, stmysql;
		ResultSet res;
		String ins;

		try{
			ins = textoSentencia.getText();
			st = bd.conexion.createStatement();
			res = st.executeQuery(ins);
			rsmd = res.getMetaData();
			
			//Copiar el resultset por si queremos exportar los datos a MySQL
			stmysql =bd.conexion.createStatement();
			resMysql = stmysql.executeQuery(ins);
			tablaMysql=chTablas.getSelectedItem().toString();
			
			//Construir la estructura del JTable
			String[] nombreCols = new String[rsmd.getColumnCount()];
			for(int i=0;i<nombreCols.length;i++)
				nombreCols[i] = rsmd.getColumnName(i+1); //la primera columna es la 1
				
			modelo = new DefaultTableModel(new Object[][]{},nombreCols);
			tabla.setModel(modelo);
			
			//Llenar la tabla con los registros correspondientes
			int tipo;
			Vector<Object> fila;
			while(res.next()){
				fila = new Vector<Object>();
				for(int i=1; i<=rsmd.getColumnCount();i++){
					tipo=rsmd.getColumnType(i); //devuelve el tipo sql de java
					switch(tipo){
						case Types.CHAR:
						case Types.VARCHAR:
						case Types.LONGVARCHAR: fila.add(res.getString(i));break;
						
						case Types.NUMERIC: fila.add(res.getObject(i));break;
						
						case Types.TINYINT:
						case Types.SMALLINT:
						case Types.INTEGER: fila.add(res.getInt(i));break;

						case Types.BIGINT: fila.add(res.getLong(i));break;
						case Types.FLOAT: fila.add(res.getFloat(i));break;
						
						case Types.DOUBLE:
						case Types.DECIMAL:fila.add(res.getDouble(i));break;
						
						case Types.TIME:
						case Types.DATE:
						case Types.TIMESTAMP: 
							Date fec;
							String fecha="";
							//Si existe una fecha...
							if((fec=res.getDate(i))!=null)
								fecha = formatoFecha.format(fec);
							fila.add(fecha);
							break;
							
						default: fila.add(res.getObject(i));break;
					}
				}
				modelo.addRow(fila);
			}
			if(st!=null)
				st.close();
			if(res!=null)
				res.close();
			
			//Habilitar botones de exportar
			bXML.setEnabled(true);
			bmysql.setEnabled(true);
			
		}catch(SQLException e){
			JOptionPane.showMessageDialog(this,e.getErrorCode() + ": " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void cerrarConexion(){
		try {
			if(resMysql!=null)
				resMysql.close();
			
			bd.conexion.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,e.getErrorCode() + ": " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//Metodos de la funcionalidad Exportar (a XML y a MySQL)
	public void exportarXML(){
		//Nombre tabla a exportar 
		String tablaExp = chTablas.getSelectedItem().toString();
		int numCols=modelo.getColumnCount(); 
		int numRegs=modelo.getRowCount();
		
		//1)Generar arbol DOM
		dbf = DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		//Comprobar si el archivo ExportarOracle.xml ya existe, para no borrar la info anterior
		File f = new File("ExportarOracle.xml");
		if(f.exists() && f.isFile()){
			try {
				doc = db.parse(f);
			} catch (SAXException | IOException e) {
				JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			}
			elementoRaiz = doc.getDocumentElement();
		}
		else{
			doc = db.newDocument();
			elementoRaiz = doc.createElement("tablas");
			doc.appendChild(elementoRaiz);
		}
		//2)Generar el resto de elementos con la info del jTable
		elementoTabla = doc.createElement(tablaExp);
		elementoRaiz.appendChild(elementoTabla);
		int i=0;
		while(i<numRegs){
			elementoRegistro = doc.createElement("registro");
			elementoRegistro.setAttribute("numero",Integer.toString(i+1));
			for(int pos=0;pos<numCols;pos++){
				elementoNuevo = doc.createElement(modelo.getColumnName(pos));
				if(modelo.getValueAt(i,pos)!=null){
					elementoNuevo.setTextContent(modelo.getValueAt(i,pos).toString());
				}
				else
					elementoNuevo.setTextContent("NULL");
				elementoRegistro.appendChild(elementoNuevo);
			}
			elementoTabla.appendChild(elementoRegistro);
			i++;
		}
		//3)Crear el XML fisico
		try{
			xformer = TransformerFactory.newInstance().newTransformer();
		}catch(TransformerConfigurationException | TransformerFactoryConfigurationError e){
			JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}

		xformer.setOutputProperty(OutputKeys.METHOD,"xml");
		xformer.setOutputProperty(OutputKeys.INDENT,"yes");

		source = new DOMSource(doc);
		result = new StreamResult(new File("ExportarOracle.xml"));
		try{
			xformer.transform(source,result);
		}catch(TransformerException e){
			JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		JOptionPane.showMessageDialog(this, "XML creado o modificado con exito.","Mensaje",JOptionPane.INFORMATION_MESSAGE);
		bXML.setEnabled(false);
	}
	
	public void exportarMysql(){
		//Abre la ventana de conexion a MySQL y manda el resultset a dicha ventana
		VentanaConexionMySql frameMysql = new VentanaConexionMySql();
		frameMysql.setDatos(resMysql,tablaMysql,this);
		frameMysql.setVisible(true);
		bmysql.setEnabled(false);
		this.setEnabled(false);
	}
}
