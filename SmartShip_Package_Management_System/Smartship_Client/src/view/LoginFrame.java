package view;

import java.awt.*;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

	
public class LoginFrame extends JFrame {
		private static final long serialVersionUID = 1L;

		public LoginFrame() {
			setTitle("Login");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(950, 700);
			setLocationRelativeTo(null);
			ImageIcon ssLogo = new ImageIcon("./assets/iutLogo.png");
		    setIconImage(ssLogo.getImage());
		
			//System.out.println("Icon loaded: " + (ssLogo.getIconWidth() > 0));
			//System.out.println("Icon dimensions: " + ssLogo.getIconWidth() + "x" + ssLogo.getIconHeight());

			if (ssLogo.getIconWidth() > 0) {
			    setIconImage(ssLogo.getImage());
			    System.out.println("Icon set successfully!");
			}
			 getContentPane().setBackground(new Color(249, 249, 249));
		        
		        // Create main panel with BorderLayout
		        JPanel mainPanel = new JPanel(new BorderLayout());
		        mainPanel.setBackground(new Color(249, 249, 249));
		        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		        // Left panel for login form
		        JPanel leftPanel = createLoginFormPanel();
		        
		        // Right panel for image
		       JPanel rightPanel = createImagePanel();

		        // Add panels to main panel
		        mainPanel.add(leftPanel, BorderLayout.WEST);
		        mainPanel.add(rightPanel, BorderLayout.CENTER);

		        add(mainPanel);
			
			setVisible(true);
			
			
		
		};
		
		private JPanel createLoginFormPanel() {
			JPanel panel = new JPanel();
	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	        panel.setBackground(new Color(249, 249, 249));
	        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 20));
	        panel.setPreferredSize(new Dimension(400, getHeight()));

	        // Welcome text
	        JLabel welcomeLabel1 = new JLabel("Hello there Welcome!");
	        welcomeLabel1.setFont(new Font("Arial", Font.BOLD, 28));
	        welcomeLabel1.setForeground(new Color(52, 73, 94));
	        welcomeLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);

	        JLabel welcomeLabel2 = new JLabel("SmartShip Management System");
	        welcomeLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
	        welcomeLabel2.setForeground(new Color(52, 73, 94));
	        welcomeLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);

	        // Add spacing
	        panel.add(welcomeLabel1);
	        panel.add(Box.createRigidArea(new Dimension(0, 5)));
	        panel.add(welcomeLabel2);
	        panel.add(Box.createRigidArea(new Dimension(0, 40)));

	        
	        // ID label
	        JLabel idLabel = new JLabel("Enter ID:");
	        idLabel.setFont(new Font("Arial", Font.BOLD, 12));
	        idLabel.setForeground(new Color(52, 73, 94));
	        idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
	        panel.add(idLabel);
	        panel.add(Box.createRigidArea(new Dimension(0, 5)));

	        // ID field
	        JTextField idField = new JTextField();
	        idField.setPreferredSize(new Dimension(500, 40));
	        idField.setMaximumSize(new Dimension(500, 40));
	        idField.setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createLineBorder(new Color(236, 240, 241)),
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)
	        ));
	        idField.setBackground(Color.WHITE);
	        panel.add(idField);
	        panel.add(Box.createRigidArea(new Dimension(0, 20)));

	        // Password label
	        JLabel passwordLabel = new JLabel("Enter Password:");
	        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
	        passwordLabel.setForeground(new Color(52, 73, 94));
	        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
	        panel.add(passwordLabel);
	        panel.add(Box.createRigidArea(new Dimension(0, 5)));

	        // Password field
	        JPasswordField passwordField = new JPasswordField();
	        passwordField.setPreferredSize(new Dimension(500, 40));
	        passwordField.setMaximumSize(new Dimension(500, 40));
	        passwordField.setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createLineBorder(new Color(236, 240, 241)),
	            BorderFactory.createEmptyBorder(10, 10, 10, 10)
	        ));
	        passwordField.setBackground(Color.WHITE);
	        panel.add(passwordField);
	        panel.add(Box.createRigidArea(new Dimension(0, 20)));

	        

	        // Login button
	        JButton loginButton = new JButton("Log In");
	        loginButton.setBackground(new Color(163, 67, 53));
	        loginButton.setForeground(Color.WHITE);
	        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
	        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
	        loginButton.setFocusPainted(false);
	        loginButton.setPreferredSize(new Dimension(500, 40));
	        loginButton.setMaximumSize(new Dimension(500, 40));
	        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	        loginButton.addActionListener(e->{
	            String id = idField.getText();
	            String password = passwordField.getText();
	            //LoginController lgc = new LoginController();
	            //lgc.authenticate(id, password);
	            setVisible(false);
	        });
	        panel.add(loginButton);
	        panel.add(Box.createRigidArea(new Dimension(0, 20)));

	        
	        
	        // Add flexible space to push credits to bottom
	        panel.add(Box.createVerticalGlue());

	        JPanel signupPanel = new JPanel();
	        signupPanel.setLayout(new BoxLayout(signupPanel, BoxLayout.X_AXIS));
	        signupPanel.setBackground(new Color(249, 249, 249));
	        signupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	        signupPanel.setMaximumSize(new Dimension(300, 30));
	        
	        JLabel signupLabel = new JLabel("Don't have an account? ");
	        signupLabel.setFont(new Font("Arial", Font.PLAIN, 12));
	        signupLabel.setForeground(new Color(52, 73, 94));
	        
	        JButton signupButton = new JButton("Sign Up");
	        signupButton.setFont(new Font("Arial", Font.BOLD, 12));
	        signupButton.setForeground(new Color(163, 67, 53));
	        signupButton.setBorderPainted(false);
	        signupButton.setContentAreaFilled(false);
	        signupButton.setFocusPainted(false);
	        signupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
	        signupButton.addActionListener(e -> {
	            // Add your sign up logic here
	            System.out.println("Sign up button clicked");
	            // You can open a new SignUpFrame here
	            new SignUpFrame();
	            setVisible(false);
	        });
	        
	        signupPanel.add(signupLabel);
	        signupPanel.add(signupButton);
	        
	        panel.add(signupPanel);

	        return panel;
		}
		 private JPanel createImagePanel() {
		        JPanel panel = new JPanel(new BorderLayout());
		        panel.setBackground(new Color(249, 249, 249));
		        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		        try {
		            ImageIcon loginImage = new ImageIcon("./assets/smartshipImage.png");
		            JLabel imageLabel = new JLabel(loginImage);
		            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		            panel.add(imageLabel, BorderLayout.CENTER);
		        } catch (Exception e) {
		            // Fallback if image not found
		            JLabel placeholder = new JLabel("Login Image", SwingConstants.CENTER);
		            placeholder.setFont(new Font("Arial", Font.ITALIC, 18));
		            placeholder.setForeground(new Color(52, 73, 94));
		            panel.add(placeholder, BorderLayout.CENTER);
		        }

		        return panel;
		    }
		

		public static void main(String [] args) {
			
			 new LoginFrame();
		}
	

}
