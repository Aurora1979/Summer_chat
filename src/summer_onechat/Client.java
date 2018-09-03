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
	private static JTextArea showArea;//显示区
	private static JTextField iuputField;//输入区
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
	private MessageThread messageThread;// 负责接收消息的线程
	private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户
	public static Map<String, Socket> onLineUserscs = null;// 所有在线用户


	
	
	//得到文件信息
		public static void getinfo(File f )throws IOException {

	        SimpleDateFormat sdf;
	        sdf=new SimpleDateFormat("yyyy 年  MM 月 dd 日 hh 时 mm 分");
	        if(f.isFile()) {
	        	showArea.append("文件大小为"+f.length()+"      "+"时间为"+sdf.format(new Date(f.lastModified())));
	           
	        }
		}

	// 执行发送
	public void send() {
		if (!isConnected) {
			JOptionPane.showMessageDialog(frame, "还没有连接服务器，无法发送消息！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String message =iuputField.getText().trim();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		//sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message);
		showArea.append(frame.getTitle()+":"+message+"\r\n");
		
		
		sendMessage(message);
		iuputField.setText(null);
	}

	// 构造方法
	public Client(){
		 frame=new JFrame("客户机");   
	      open_file=new JButton("文件");  
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
		btn_start = new JButton("连接");
		btn_stop = new JButton("断开");
		btn_send = new JButton("发送");
		listModel = new DefaultListModel();
		userList = new JList(listModel);

		northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1, 7));
		northPanel.add(new JLabel("端口"));
		northPanel.add(txt_port);
		northPanel.add(new JLabel("服务器IP"));
		northPanel.add(txt_hostIp);
		northPanel.add(new JLabel("姓名"));
		northPanel.add(txt_name);
		northPanel.add(btn_start);
		northPanel.add(btn_stop);
		northPanel.add(open_file);
		northPanel.setBorder(new TitledBorder("连接信息"));

		rightScroll = new JScrollPane(showArea);
		rightScroll.setBorder(new TitledBorder("消息显示区"));
		leftScroll = new JScrollPane(userList);
		leftScroll.setBorder(new TitledBorder("在线用户"));
		southPanel = new JPanel(new BorderLayout());
		southPanel.add(iuputField, "Center");
		southPanel.add(btn_send, "East");
		southPanel.setBorder(new TitledBorder("写消息"));

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll,
				rightScroll);
		centerSplit.setDividerLocation(100);

		
		// 更改JFrame的图标：
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
		userList.addMouseListener(this);// 一遇到鼠标事件立即执行.
	    showArea.append("**默认为群聊模式，若想单聊点击好友名字即可**");
	    showArea.append("\r\n");

		//userList.addListSelectionListener(this);

		// 写消息的文本框中按回车键时事件
		iuputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				 if(flag1==0){
		    	        //String x=iuputField.getText().trim();
		    	        //iuputField.setText("");
		    	            //showArea.append("选择"+x);
		    	            writer.println("g");
		    	            writer.flush();
		    	            flag1++;
		    	        }
				 
					 
					 //String s =":"+inputChat.getText().trim();
					 
				 
				 
				send();
				
			}
		});

		// 单击发送按钮时事件
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 if(flag1==0){
		    	        //String x=iuputField.getText().trim();
		    	        //iuputField.setText("");
		    	            //showArea.append("选择"+x);
		    	            writer.println("g");
		    	            writer.flush();
		    	            flag1++;
		    	        }
				send();
			}
		});

		// 单击连接按钮时事件
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port;
				if (isConnected) {
					JOptionPane.showMessageDialog(frame, "已处于连接上状态，不要重复连接!",
							"错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					try {
						port = Integer.parseInt(txt_port.getText().trim());
					} catch (NumberFormatException e2) {
						throw new Exception("端口号不符合要求!端口为整数!");
					}
					String hostIp = txt_hostIp.getText().trim();
					String name = txt_name.getText().trim();
					if (name.equals("") || hostIp.equals("")) {
						throw new Exception("姓名、服务器IP不能为空!");
					}
					boolean flag = connectServer(port, hostIp, name);//连接服务器
					if (flag == false) {
						throw new Exception("与服务器连接失败!");
					}
					frame.setTitle(name);
					JOptionPane.showMessageDialog(frame, "成功连接!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// 单击断开按钮时事件
		btn_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isConnected) {
					JOptionPane.showMessageDialog(frame, "已处于断开状态，不要重复断开!",
							"错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					boolean flag = closeConnection();// 断开连接
					if (flag == false) {
						throw new Exception("断开连接发生异常！");
					}
					JOptionPane.showMessageDialog(frame, "成功断开!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(),
							"错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		open_file.addActionListener(new ActionListener() { 
	
			public void actionPerformed(ActionEvent e) {
			
				if(e.getSource()==open_file)   
					//设置打开文件对话框的标题   
	                fc.setDialogTitle("Open File");   
	  
	              //这里显示打开文件的对话框   
	           try{    
	                       flag=fc.showOpenDialog(frame);    
	                }   
	           catch(HeadlessException head){    
	  
	                       System.out.println("Open File Dialog ERROR!");   
	                }   
	                 
	              //如果按下确定按钮，则获得该文件。   
	              if(flag==JFileChooser.APPROVE_OPTION)   
	                {   
	                     //获得该文件   
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
		
	
           
		
		// 关闭窗口时事件
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					try {
						closeConnection();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}// 关闭连接
					//writer.println(getName()+"下线了");
					//writer.flush();
				}
				System.exit(0);// 退出程序
			}
		});
	}
	 
	  public void readFile(String file) throws IOException{

		    String readline;
		    BufferedReader in = new BufferedReader(new FileReader(file));

		        while((readline=in.readLine())!=null)

		        {
		        writer.println(readline);


		        writer.flush();//刷新输出流，使Server马上收到该字符串

		        //在系统标准输出上打印读入的字符串

		        System.out.println(readline);

		        }

		}

	//} //**************************************************

	/**
	 * 连接服务器
	 * 
	 * 
	 * @param port
	 * @param hostIp
	 * @param name
	 */
	public boolean connectServer(int port, String hostIp, String name) {
		// 连接服务器
		try {
			this.socket = new Socket(hostIp, port);// 根据端口号和服务器ip建立连接
			this.writer = new PrintWriter(socket.getOutputStream());
			this.reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			// 发送客户端用户基本信息(用户名和ip地址)
			sendMessage(name + "@" + socket.getLocalAddress().toString());
			// 开启接收消息的线程
			messageThread = new MessageThread(reader, showArea);
			messageThread.start();
			isConnected = true;// 已经连接上了
			return true;
		} catch (Exception e) {
			showArea.append("与端口号为：" + port + "    IP地址为：" + hostIp
					+ "   的服务器连接失败!" + "\r\n");
			isConnected = false;// 未连接上
			return false;
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		writer.println(message);//发送给服务器
		writer.flush();//刷新
	}

	/**
	 * 客户端主动关闭连接
	 * @throws IOException 
	 */
	@SuppressWarnings("deprecation")
	public synchronized boolean closeConnection() throws IOException {
		if(flag1==1) {
			
			writer.println("下线了胖友们~~~");
			writer.flush();
			reader.close();
			writer.close();
			socket.close();
			
		}
		
		
		try {
			sendMessage("CLOSE");// 发送断开连接命令给服务器
			messageThread.stop();// 停止接受消息线程
			// 释放资源
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

	// 不断接收消息的线程*******************************************************
	class MessageThread extends Thread {
		private BufferedReader reader;
		private JTextArea showArea;

		// 接收消息线程的构造方法
		public MessageThread(BufferedReader reader, JTextArea textArea) {
			this.reader = reader;
			this.showArea = textArea;//显示区
		}

		// 被动的关闭连接
		public synchronized void closeCon() throws Exception {
			// 清空用户列表
			listModel.removeAllElements();
			// 被动的关闭连接释放资源
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;// 修改状态为断开
		}

		public void run() {
			String message = "";
			while (true) {
				try {
					message = reader.readLine();
					StringTokenizer stringTokenizer = new StringTokenizer(
							message, "/@");
					String command = stringTokenizer.nextToken();// 命令
					if (command.equals("CLOSE"))// 服务器已关闭命令
					{
						showArea.append("服务器已关闭!\r\n");
						closeCon();// 被动的关闭连接
						return;// 结束线程
					} else if (command.equals("ADD")) {// 有用户上线更新在线列表
						String username = "";
						String userIp = "";
						if ((username = stringTokenizer.nextToken()) != null
								&& (userIp = stringTokenizer.nextToken()) != null) {
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
						}
					} else if (command.equals("DELETE")) {// 有用户下线更新在线列表
						String username = stringTokenizer.nextToken();
						User user = (User) onLineUsers.get(username);
						onLineUsers.remove(user);
						listModel.removeElement(username);
					} else if (command.equals("USERLIST")) {// 加载在线用户列表
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
					} else if (command.equals("MAX")) {// 人数已达上限
						showArea.append(stringTokenizer.nextToken()
								+ stringTokenizer.nextToken() + "\r\n");
						closeCon();// 被动的关闭连接
						JOptionPane.showMessageDialog(frame, "服务器缓冲区已满！", "错误",
								JOptionPane.ERROR_MESSAGE);
						return;// 结束线程
					} else {// 普通消息
						showArea.append(message + "\r\n");
						//接收文件
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
	                                     
	                                	//如果发送的消息中包含“end”则退出循环，同时打印文件信息
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
	
	

//主方法,程序入口*********************************************
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
	 * 对list1而言，当鼠标在某个项目连续按两下时，我们利用JList所提供的locationToIndex()方法，找到所键击的项目，并
	 * 由tmp取得此项目的项目值
	 * ，然后将此项目值增加到mode2中[mode2.addElement(tmp)],用setModel重新设置list2的
	 * ListModel,使list2可显示出所增加的项目，将刚刚在list1双击的项目删除.
	 */
	if (e.getSource() == userList) {
		if (e.getClickCount() == 2) {
			
			
			index = userList.locationToIndex(e.getPoint());
			String tmp = (String) listModel.getElementAt(index);
			showArea.append("你选择了好友"+tmp+"聊天");
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
 

	