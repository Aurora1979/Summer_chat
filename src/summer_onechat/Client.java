package summer_onechat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;



 class Client extends Frame implements  ActionListener,  MouseListener{

    private int flag;  
    static int flag1=0;
	private JFrame frame;
	private JList userList;
	private static JTextArea showArea;//��ʾ��
	private static JTextField iuputField;//������
	private JButton open_file;
	private JPanel p;   
	private File f;   
    private JFileChooser fc;
	private JTextField txt_port;
	private JTextField txt_hostIp;
	private JTextField txt_name;
	private JButton btn_start;
	private JButton btn_stop;
	private JButton btn_send;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightScroll;
	private JScrollPane leftScroll;
	private JSplitPane centerSplit;
	private DefaultListModel listModel;
	private boolean isConnected = false;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private MessageThread messageThread;// ���������Ϣ���߳�
	private Map<String, User> onLineUsers = new HashMap<String, User>();// ���������û�
	public static Map<String, Socket> onLineUserscs = null;// ���������û�


	
	
	//�õ��ļ���Ϣ
		public static void getinfo(File f )throws IOException {

	        SimpleDateFormat sdf;
	        sdf=new SimpleDateFormat("yyyy ��  MM �� dd �� hh ʱ mm ��");
	        if(f.isFile()) {
	        	showArea.append("�ļ���СΪ"+f.length()+"      "+"ʱ��Ϊ"+sdf.format(new Date(f.lastModified())));
	           
	        }
		}

	// ִ�з���
	public void send() {
		if (!isConnected) {
			JOptionPane.showMessageDialog(frame, "��û�����ӷ��������޷�������Ϣ��", "����",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String message =iuputField.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "��Ϣ����Ϊ�գ�", "����",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		//sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message);
		showArea.append(frame.getTitle()+":"+message+"\r\n");
		
		
		sendMessage(message);
		iuputField.setText(null);
	}

	// ���췽��
	public Client(){
		 frame=new JFrame("�ͻ���");   
	      open_file=new JButton("�ļ�");  
	    //  p=new JPanel();   
	      fc=new JFileChooser();  
	     
        setSize(90,90);
        setVisible(true);
		showArea = new JTextArea();
		showArea.setEditable(false);
		showArea.setForeground(Color.blue);
		iuputField = new JTextField();
		txt_port = new JTextField("6666");
		txt_hostIp = new JTextField("127.0.0.1");
		txt_name = new JTextField("xiaoqiang");
		btn_start = new JButton("����");
		btn_stop = new JButton("�Ͽ�");
		btn_send = new JButton("����");
		listModel = new DefaultListModel();
		userList = new JList(listModel);

		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 7));
		northPanel.add(new JLabel("�˿�"));
		northPanel.add(txt_port);
		northPanel.add(new JLabel("������IP"));
		northPanel.add(txt_hostIp);
		northPanel.add(new JLabel("����"));
		northPanel.add(txt_name);
		northPanel.add(btn_start);
		northPanel.add(btn_stop);
		northPanel.add(open_file);
		northPanel.setBorder(new TitledBorder("������Ϣ"));

		rightScroll = new JScrollPane(showArea);
		rightScroll.setBorder(new TitledBorder("��Ϣ��ʾ��"));
		leftScroll = new JScrollPane(userList);
		leftScroll.setBorder(new TitledBorder("�����û�"));
		southPanel = new JPanel(new BorderLayout());
		southPanel.add(iuputField, "Center");
		southPanel.add(btn_send, "East");
		southPanel.setBorder(new TitledBorder("д��Ϣ"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll,
				rightScroll);
		centerSplit.setDividerLocation(100);

		
		// ����JFrame��ͼ�꣺
		//frame.setIconImage(Toolkit.getDefaultToolkit().createImage(Client.class.getResource("qq.png")));
		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.add(southPanel, "South");
		frame.setSize(600, 400);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2,
				(screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);
		userList.addMouseListener(this);// һ��������¼�����ִ��.
	    showArea.append("**Ĭ��ΪȺ��ģʽ�����뵥�ĵ���������ּ���**");
	    showArea.append("\r\n");

		//userList.addListSelectionListener(this);

		// д��Ϣ���ı����а��س���ʱ�¼�
		iuputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				 if(flag1==0){
		    	        //String x=iuputField.getText().trim();
		    	        //iuputField.setText("");
		    	            //showArea.append("ѡ��"+x);
		    	            writer.println("g");
		    	            writer.flush();
		    	            flag1++;
		    	        }
				 
					 
					 //String s =":"+inputChat.getText().trim();
					 
				 
				 
				send();
				
			}
		});

		// �������Ͱ�ťʱ�¼�
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if(flag1==0){
		    	        //String x=iuputField.getText().trim();
		    	        //iuputField.setText("");
		    	            //showArea.append("ѡ��"+x);
		    	            writer.println("g");
		    	            writer.flush();
		    	            flag1++;
		    	        }
				send();
			}
		});

		// �������Ӱ�ťʱ�¼�
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port;
				if (isConnected) {
					JOptionPane.showMessageDialog(frame, "�Ѵ���������״̬����Ҫ�ظ�����!",
							"����", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					try {
						port = Integer.parseInt(txt_port.getText().trim());
					} catch (NumberFormatException e2) {
						throw new Exception("�˿ںŲ�����Ҫ��!�˿�Ϊ����!");
					}
					String hostIp = txt_hostIp.getText().trim();
					String name = txt_name.getText().trim();
					if (name.equals("") || hostIp.equals("")) {
						throw new Exception("������������IP����Ϊ��!");
					}
					boolean flag = connectServer(port, hostIp, name);//���ӷ�����
					if (flag == false) {
						throw new Exception("�����������ʧ��!");
					}
					frame.setTitle(name);
					JOptionPane.showMessageDialog(frame, "�ɹ�����!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"����", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// �����Ͽ���ťʱ�¼�
		btn_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isConnected) {
					JOptionPane.showMessageDialog(frame, "�Ѵ��ڶϿ�״̬����Ҫ�ظ��Ͽ�!",
							"����", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					boolean flag = closeConnection();// �Ͽ�����
					if (flag == false) {
						throw new Exception("�Ͽ����ӷ����쳣��");
					}
					JOptionPane.showMessageDialog(frame, "�ɹ��Ͽ�!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"����", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		open_file.addActionListener(new ActionListener() { 
	
			public void actionPerformed(ActionEvent e) {
			
				if(e.getSource()==open_file)   
					//���ô��ļ��Ի���ı���   
	                fc.setDialogTitle("Open File");   
	  
	              //������ʾ���ļ��ĶԻ���   
	           try{    
	                       flag=fc.showOpenDialog(frame);    
	                }   
	           catch(HeadlessException head){    
	  
	                       System.out.println("Open File Dialog ERROR!");   
	                }   
	                 
	              //�������ȷ����ť�����ø��ļ���   
	              if(flag==JFileChooser.APPROVE_OPTION)   
	                {   
	                     //��ø��ļ�   
	                       f=fc.getSelectedFile();   
	                       try {
							readFile(f.toString());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	                       System.out.println("open file----"+f.getName());
	                       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                       
	                 } 
	             
	              
			}
			
		});
		
	
           
		
		// �رմ���ʱ�¼�
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					try {
						closeConnection();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}// �ر�����
					//writer.println(getName()+"������");
					//writer.flush();
				}
				System.exit(0);// �˳�����
			}
		});
	}
	 
	  public void readFile(String file) throws IOException{

		    String readline;
		    BufferedReader in = new BufferedReader(new FileReader(file));

		        while((readline=in.readLine())!=null)

		        {
		        writer.println(readline);


		        writer.flush();//ˢ���������ʹServer�����յ����ַ���

		        //��ϵͳ��׼����ϴ�ӡ������ַ���

		        System.out.println(readline);

		        }

		}

	//} //**************************************************

	/**
	 * ���ӷ�����
	 * 
	 * 
	 * @param port
	 * @param hostIp
	 * @param name
	 */
	public boolean connectServer(int port, String hostIp, String name) {
		// ���ӷ�����
		try {
			this.socket = new Socket(hostIp, port);// ���ݶ˿ںźͷ�����ip��������
			this.writer = new PrintWriter(socket.getOutputStream());
			this.reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			// ���Ϳͻ����û�������Ϣ(�û�����ip��ַ)
			sendMessage(name + "@" + socket.getLocalAddress().toString());
			// ����������Ϣ���߳�
			messageThread = new MessageThread(reader, showArea);
			messageThread.start();
			isConnected = true;// �Ѿ���������
			return true;
		} catch (Exception e) {
			showArea.append("��˿ں�Ϊ��" + port + "    IP��ַΪ��" + hostIp
					+ "   �ķ���������ʧ��!" + "\r\n");
			isConnected = false;// δ������
			return false;
		}
	}

	/**
	 * ������Ϣ
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		writer.println(message);//���͸�������
		writer.flush();//ˢ��
	}

	/**
	 * �ͻ��������ر�����
	 * @throws IOException 
	 */
	@SuppressWarnings("deprecation")
	public synchronized boolean closeConnection() throws IOException {
		if(flag1==1) {
			
			writer.println("������������~~~");
			writer.flush();
			reader.close();
			writer.close();
			socket.close();
			
		}
		
		
		try {
			sendMessage("CLOSE");// ���ͶϿ����������������
			messageThread.stop();// ֹͣ������Ϣ�߳�
			// �ͷ���Դ
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
			isConnected = true;
			return false;
		}
	}

	// ���Ͻ�����Ϣ���߳�*******************************************************
	class MessageThread extends Thread {
		private BufferedReader reader;
		private JTextArea showArea;

		// ������Ϣ�̵߳Ĺ��췽��
		public MessageThread(BufferedReader reader, JTextArea textArea) {
			this.reader = reader;
			this.showArea = textArea;//��ʾ��
		}

		// �����Ĺر�����
		public synchronized void closeCon() throws Exception {
			// ����û��б�
			listModel.removeAllElements();
			// �����Ĺر������ͷ���Դ
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;// �޸�״̬Ϊ�Ͽ�
		}

		public void run() {
			String message = "";
			while (true) {
				try {
					message = reader.readLine();
					StringTokenizer stringTokenizer = new StringTokenizer(
							message, "/@");
					String command = stringTokenizer.nextToken();// ����
					if (command.equals("CLOSE"))// �������ѹر�����
					{
						showArea.append("�������ѹر�!\r\n");
						closeCon();// �����Ĺر�����
						return;// �����߳�
					} else if (command.equals("ADD")) {// ���û����߸��������б�
						String username = "";
						String userIp = "";
						if ((username = stringTokenizer.nextToken()) != null
								&& (userIp = stringTokenizer.nextToken()) != null) {
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
						}
					} else if (command.equals("DELETE")) {// ���û����߸��������б�
						String username = stringTokenizer.nextToken();
						User user = (User) onLineUsers.get(username);
						onLineUsers.remove(user);
						listModel.removeElement(username);
					} else if (command.equals("USERLIST")) {// ���������û��б�
						int size = Integer
								.parseInt(stringTokenizer.nextToken());
						String username = null;
						String userIp = null;
						for (int i = 0; i < size; i++) {
							username = stringTokenizer.nextToken();
							userIp = stringTokenizer.nextToken();
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
						}
					} else if (command.equals("MAX")) {// �����Ѵ�����
						showArea.append(stringTokenizer.nextToken()
								+ stringTokenizer.nextToken() + "\r\n");
						closeCon();// �����Ĺر�����
						JOptionPane.showMessageDialog(frame, "������������������", "����",
								JOptionPane.ERROR_MESSAGE);
						return;// �����߳�
					} else {// ��ͨ��Ϣ
						showArea.append(message + "\r\n");
						//�����ļ�
						if(message.contains("sendfile"))

	                    {
	                        try {
	                            File f2;
	                          
	                            f2=new File("recv.txt");
	                         
	                            FileWriter fw=new FileWriter("recv.txt");
	                            showArea.append("file start transproting!!!");
	                            while((message=reader.readLine())!=null){
	                                fw.write(message);
	                                fw.write("\r\n");
	                                fw.flush();
	                                if(message.contains("end")){
	                                     
	                                	//������͵���Ϣ�а�����end�����˳�ѭ����ͬʱ��ӡ�ļ���Ϣ
	                                	showArea.append("receiving file preferectly!!");
	                                	getinfo(f2);
	                                    break;
	                                    
	                                }
	                                
	                            }
	                            
	                           
	                           
	                        }
	                        
	                        catch(Exception e) {

	                            e.printStackTrace();
	                        }
	            	
	               
	              } 
								
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	

//������,�������*********************************************
public static void main(String[] args) {
		new Client();
	}

@Override
public void actionPerformed(ActionEvent arg0) {
	// TODO Auto-generated method stub
	
}


@Override
public void mouseClicked(MouseEvent e) {
	// TODO Auto-generated method stub
	int index;
	/*
	 * ��list1���ԣ��������ĳ����Ŀ����������ʱ����������JList���ṩ��locationToIndex()�������ҵ�����������Ŀ����
	 * ��tmpȡ�ô���Ŀ����Ŀֵ
	 * ��Ȼ�󽫴���Ŀֵ���ӵ�mode2��[mode2.addElement(tmp)],��setModel��������list2��
	 * ListModel,ʹlist2����ʾ�������ӵ���Ŀ�����ո���list1˫������Ŀɾ��.
	 */
	if (e.getSource() == userList) {
		if (e.getClickCount() == 2) {
			
			
			index = userList.locationToIndex(e.getPoint());
			String tmp = (String) listModel.getElementAt(index);
			showArea.append("��ѡ���˺���"+tmp+"����");
			//String myname=txt_name.getText().trim();
			String friendname=tmp;
			//send(friendname);
			writer.println(friendname);
			writer.flush();
			flag1=1;
		
			
			
			
			
			
		}
	}
	
}

@Override
public void mouseEntered(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseExited(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mousePressed(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseReleased(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}
	
}
 

	