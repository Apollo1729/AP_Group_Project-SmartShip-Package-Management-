package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import controller.DriverController;
import model.User;



public class DriverFrame extends JFrame {
		private JTable deliveries;

	    private static final long serialVersionUID = 1L;

		public DriverFrame(User user) { //DriverFrame(User user)
	        setTitle("Driver Frame");
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setSize(950, 700);
	        setLocationRelativeTo(null);
	        ImageIcon ssLogo = new ImageIcon("./assets/iutLogo.png");
	        setIconImage(ssLogo.getImage());
	        
	       

	        if (ssLogo.getIconWidth() > 0) {
	            setIconImage(ssLogo.getImage());
	            System.out.println("Icon set successfully!");
	        }
	        
	        getContentPane().setBackground(new Color(249, 249, 249));
	        
	        // Create main panel with BorderLayout
	        JPanel mainPanel = new JPanel(new BorderLayout());
	        mainPanel.setBackground(new Color(249, 249, 249));
	        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	        JPanel welcTab = createDriverPanel(user); //createDriverPanel(user)
	        JPanel btnTab = createBtnPanel(user);
	        
	        mainPanel.add(welcTab,BorderLayout.CENTER);
	        mainPanel.add(btnTab,BorderLayout.SOUTH);
	       

	        add(mainPanel);
	        
	        
	        setVisible(true);
	        
	        new SwingWorker<Void, Void>() {
	            @Override
	            protected Void doInBackground() {
	                loadDeliv(user.getUserId());
	                return null;
	            }
	        }.execute();
	    }
		
		public JPanel createDriverPanel(User user) //createDriverPanel(User user)
		{
			JPanel panel = new JPanel();
			 panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		     panel.setBackground(new Color(249, 249, 249));
		     panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 20));
		     //panel.setPreferredSize(new Dimension(400, getHeight()));
		     
		     JLabel welcomeDriver = new JLabel("Welcome Driver " + user.getUsername()); //JLabel welcomeDriver = new JLabel("Welcome Driver " + user.getUsername());
		     welcomeDriver.setFont(new Font("Arial", Font.BOLD, 32));
		     welcomeDriver.setForeground(new Color(52, 73, 94));
		     welcomeDriver.setAlignmentX(Component.CENTER_ALIGNMENT);
		     
		     JLabel message = new JLabel("Here are your currently assigned deliveries, Drive Safe!");
		     message.setFont(new Font("Arial", Font.PLAIN, 18));
		     message.setForeground(new Color(52, 73, 94));
		     message.setAlignmentX(Component.CENTER_ALIGNMENT);
		     
		     DefaultTableModel model = new DefaultTableModel() {
		    	 public boolean isCellEditable(int y,int x) {
					return x == 3;
		    		 
		    	 }
		     };
		     model.addColumn("ID");
		     model.addColumn("Customer Name");
		     model.addColumn("Destination");
		     model.addColumn("Status");
		     
		     deliveries = new JTable(model);
		     deliveries.setFont(new Font("Arial",Font.ITALIC,18));
		     deliveries.setRowHeight(20);
		     deliveries.setGridColor(new Color(161,163,153));
		     
		     String[] status = {"In Transit", "Delivered"};
		     JComboBox<String> statusMenu =  new JComboBox<>(status);
		     deliveries.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusMenu));
		     
		     JTableHeader deliHeader = deliveries.getTableHeader();
		     deliHeader.setFont(new Font("Arial",Font.BOLD,18));
		     deliHeader.setForeground(new Color(249,249,249));
		     deliHeader.setBackground(new Color(52,73,94));
		     
		     JScrollPane Table = new JScrollPane(deliveries);
		     Table.setPreferredSize(new Dimension(panel.getWidth(),350));
		     //Table.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
		     Table.setAlignmentX(Component.CENTER_ALIGNMENT);
		     Table.setBackground(new Color(99,89,89));
		     
		     
		     
		     
		     
		     panel.add(welcomeDriver);
		     panel.add(Box.createRigidArea(new Dimension(0,15)));
		     panel.add(message);
		     panel.add(Box.createRigidArea(new Dimension(0,60)));
		     panel.add(Table);
		     
		     
		        
			return panel;
		}
		
		public JPanel createBtnPanel(User user) {
			
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout());
			panel.setBackground(new Color(249, 249, 249));
			
			JButton refreshBtn = new JButton("Refresh");
			refreshBtn.setFont(new Font("Arial",Font.BOLD,16));
			refreshBtn.setForeground(Color.white);
			refreshBtn.setBackground(new Color(52,152,219));
			refreshBtn.setFocusPainted(false);
			refreshBtn.addActionListener(e -> new SwingWorker<Void, Void>() {
	            @Override
	            protected Void doInBackground() {
	                loadDeliv(user.getUserId());
	                return null;
	            }
	        }.execute());
			
			JButton applyBtn = new JButton("Apply Status Update");
			applyBtn.setFont(new Font("Arial",Font.BOLD,16));
			applyBtn.setForeground(Color.white);
			applyBtn.setBackground(new Color(39,174,96));
			applyBtn.setFocusPainted(false);
			applyBtn.addActionListener(e -> {
			    int row = deliveries.getSelectedRow();

			    if (row == -1) {
			        JOptionPane.showMessageDialog(null, "Please select a delivery row.");
			        return;
			    }

			    if (deliveries.isEditing()) {
			        deliveries.getCellEditor().stopCellEditing();
			    }

			    String trackNum = deliveries.getValueAt(row, 0).toString();
			    String newStat = deliveries.getValueAt(row, 3).toString();

			    DriverController controller = new DriverController();
			    boolean success = controller.updateDelivStatus(trackNum, newStat);

			    if (success) {
			        JOptionPane.showMessageDialog(null, "Delivery status updated!");
			    } else {
			        JOptionPane.showMessageDialog(null, "Failed to update status.");
			    }
			});
			
			panel.add(refreshBtn);
			panel.add(applyBtn);
			
	
			return panel;
		}
		
		public void loadDeliv(int userID)
		{
			DriverController x = new DriverController();
			List<Map<String,String>> deliv = x.getDeliveriesArray(userID);
			DefaultTableModel model = (DefaultTableModel) deliveries.getModel();
			model.setRowCount(0);
			
			for (Map<String,String> holder : deliv) {
				Object[] appendedrow = {
						holder.get("trackingNumber"),
						holder.get("recipientInfo"),
						holder.get("address"),
						holder.get("status")
				};
				model.addRow(appendedrow);
			}
		}
		
		//test to open frame
		public static void main(String [] args) {
			SwingUtilities.invokeLater(() -> {
	            //new DriverFrame();
				new LoginFrame(); // User logs in first
	        });
			
		}
	}
