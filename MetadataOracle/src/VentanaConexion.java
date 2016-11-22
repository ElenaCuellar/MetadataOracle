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
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

public class VentanaConexion extends JFrame {

	private JPanel contentPane;
	private JTextField tfServidor;
	private JTextField tfPuerto;
	private JTextField tfUsuario;
	private JButton bAceptar;
	private JButton bCancelar;
	private String usuario;
	private Conexion bd;
	private JPasswordField tfPass;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VentanaConexion frame = new VentanaConexion();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public VentanaConexion() {
		setTitle("Parametros de conexion");
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
				tfPuerto.requestFocus();
			}
		});
		tfServidor.setBounds(102, 21, 229, 20);
		contentPane.add(tfServidor);
		tfServidor.setColumns(10);
		
		JLabel lblPuerto = new JLabel("Puerto:");
		lblPuerto.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblPuerto.setBounds(20, 66, 72, 14);
		contentPane.add(lblPuerto);
		
		tfPuerto = new JTextField();
		tfPuerto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tfUsuario.requestFocus();
			}
		});
		tfPuerto.setColumns(10);
		tfPuerto.setBounds(102, 64, 229, 20);
		contentPane.add(tfPuerto);
		
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
				System.exit(0);
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
		
		usuario="";
	}
	
	public void conectar(){
		String servidor = tfServidor.getText();
		String puerto = tfPuerto.getText();
		usuario = tfUsuario.getText();
		char[] passwd;
		String pass;
		if ((passwd=tfPass.getPassword())!=null)
			pass = String.valueOf(passwd);
		else
			pass="";
		
		bd = new Conexion(servidor,puerto,usuario,pass);
		
		if(bd.correcto){ //Si la conexion es correcta...
			//Abrir la ventana de la aplicacion
			VentanaPrincipal framePrincipal = new VentanaPrincipal();
			framePrincipal.conectarBD(bd,usuario);
			framePrincipal.setVisible(true);
			this.setVisible(false);
		}
		else{
			JOptionPane.showMessageDialog(this, "No se ha establecido una conexion valida con el servidor.\n"+
					bd.mensajeError,"Error",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
}
