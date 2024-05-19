/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.uja.ambientales;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author iesdi
 */
public class Interfaz extends javax.swing.JFrame {

    private ArrayList<String> map;
    private final int numRows;
    private final int numCols;
    private final Map<String, String> imageTypes;
    private ArrayList<Grid> grids;
    private final ArrayList<String> imagePaths;
    private int currentIndex1;
    private final ArrayList<String> texts;
    private int currentIndex2;
    private boolean clickEnabled;
    private ArrayList<String> ordersQueue;
    private String order;
    private String currentOrder;
    private ArrayList<ImagedPanel> highlightedPanels;
    private ArrayList<ImagedPanel> selectedPanels;
    private int orderMaking;
    private int robotX;
    private int robotY;
    private float robotAngle;
    private String truckImage;
    private ImagedPanel truckPanel;
    private Map<String, ImagedPanel> truckPanels;
    private int grid_length;
    private ArrayList<Integer> currentOrderComponents;
    private ImagedPanel APanel;
    private ImagedPanel BPanel;
    private ImagedPanel lastClicked;
    private final MQTTMessageReceiver mqttReceiver;
    private final String ordersTopic;
    private ImagedPanel[][] mazePanels;
    private boolean pulsado;
    private boolean pedidoRecogido;

    /**
     * Creates new form Interfaz
     */
    public Interfaz() {
        this.pulsado = false;
        this.map = null;
        this.grids = null;
        this.lastClicked = null;
        this.APanel = null;
        this.BPanel = null;
        this.currentOrderComponents = new ArrayList();
        this.clickEnabled = true;
        this.order = null;
        this.currentOrder = null;
        this.highlightedPanels = new ArrayList();
        this.selectedPanels = new ArrayList();
        this.orderMaking = 0;
        this.robotX = -1;
        this.robotY = -1;
        this.robotAngle = -1;
        this.truckImage = null;
        this.numRows = 7;
        this.numCols = 5;
        this.pedidoRecogido = false;
        this.mazePanels = new ImagedPanel[7][5];
        this.imagePaths = new ArrayList();
        this.ordersTopic = "/Grupo G/orders";
        imagePaths.add("assets/a.png");
        imagePaths.add("assets/b.png");
        this.currentIndex1 = 0;
        this.texts = new ArrayList();
        texts.add("Select one of the locations highlighted on the map to the right as the package pickup point");
        texts.add("Select one of the locations highlighted on the map to the right as the package delivery point");
        texts.add("");
        this.currentIndex2 = 1;
        this.clickEnabled = true;
        this.ordersQueue = new ArrayList();
        this.imageTypes = new HashMap();
        this.imageTypes.put("00", "assets/image0.jpg");
        this.imageTypes.put("01", "assets/image1.jpg");
        this.imageTypes.put("02", "assets/image2.jpg");
        this.imageTypes.put("03", "assets/image3.jpg");
        this.imageTypes.put("04", "assets/image4.jpg");
        this.imageTypes.put("05", "assets/image5.jpg");
        this.imageTypes.put("06", "assets/image6.jpg");
        this.imageTypes.put("07", "assets/image7.jpg");
        this.imageTypes.put("08", "assets/image8.jpg");
        this.imageTypes.put("09", "assets/image9.jpg");
        this.imageTypes.put("10", "assets/image10.jpg");
        this.imageTypes.put("11", "assets/image11.jpg");

        initComponents();

        grid_length = jLayeredPane1.getWidth() / numCols;

        this.truckPanels = new HashMap();
        ImagedPanel rightPanel, upPanel, leftPanel, downPanel;
        try {
            rightPanel = new ImagedPanel("assets/truck_right.png", grid_length, grid_length);
            rightPanel.setOpaque(false);
            truckPanels.put("assets/truck_right.png", rightPanel);
            upPanel = new ImagedPanel("assets/truck_up.png", grid_length, grid_length);
            upPanel.setOpaque(false);
            truckPanels.put("assets/truck_up.png", upPanel);
            leftPanel = new ImagedPanel("assets/truck_left.png", grid_length, grid_length);
            leftPanel.setOpaque(false);
            truckPanels.put("assets/truck_left.png", leftPanel);
            downPanel = new ImagedPanel("assets/truck_down.png", grid_length, grid_length);
            downPanel.setOpaque(false);
            truckPanels.put("assets/truck_down.png", downPanel);
        } catch (IOException ex) {
            Logger.getLogger(Interfaz.class.getName()).log(Level.SEVERE, null, ex);
        }

        loadDeliveryMan();

        loadGIF();

        this.mqttReceiver = new MQTTMessageReceiver(this);

        jButton2.setVisible(false);
        jButton3.setVisible(false);
        jButton4.setVisible(false);
        jButton5.setVisible(false);
        jLabel4.setVisible(false);
    }

    private void loadDeliveryMan() {
        ImageIcon deliveryMan = new ImageIcon("assets/packet_delivery.png");
        JLabel deliveryManLabel = new JLabel(deliveryMan);
        deliveryManLabel.setBackground(new Color(0, 0, 0, 0));
        deliveryManLabel.setOpaque(true);
        jPanel3.add(deliveryManLabel, BorderLayout.CENTER);
    }

    private void loadGIF() {
        ImageIcon gifIcon = new ImageIcon("assets/loading.gif");
        JLabel gifLabel = new JLabel(gifIcon);
        loadingScreen.add(gifLabel, BorderLayout.CENTER);
    }

    public void moveRobot(int x, int y, float angle) {
        if (angle < 0) {
            angle = 360 + angle;
        } else {
            if (angle > 360) {
                angle = angle % 360;
            }
        }
        if (x != robotX || y != robotY || angle != robotAngle) {
            robotX = x;
            robotY = y;
            if (angle != robotAngle) {
                robotAngle = angle;
                if ((robotAngle >= 315 && robotAngle <= 359) || (robotAngle >= 0 && robotAngle < 45)) {
                    truckImage = "assets/truck_right.png";
                } else {
                    if (robotAngle >= 45 && robotAngle < 135) {
                        truckImage = "assets/truck_up.png";
                    } else {
                        if (robotAngle >= 135 && robotAngle < 225) {
                            truckImage = "assets/truck_left.png";
                        } else {
                            truckImage = "assets/truck_down.png";
                        }
                    }
                }
            }
            jLabel9.setText("Robot location: (" + robotX + "," + robotY + ")");
            jLabel10.setText("Orientation: " + robotAngle + " degrees");
            if (!currentOrderComponents.isEmpty() && robotX == currentOrderComponents.get(0) && robotY == currentOrderComponents.get(1)) {
                jLabel8.setText("Delivering package");
            }
            if (!currentOrderComponents.isEmpty()) {
                if (robotX == currentOrderComponents.get(0) && robotY == currentOrderComponents.get(1)){
                    pedidoRecogido = true;
                }
                if (pedidoRecogido && robotX == currentOrderComponents.get(2) && robotY == currentOrderComponents.get(3)) {
                    pedidoRecogido = false;
                    jLayeredPane2.remove(APanel);
                    ImagedPanel panel = mazePanels[APanel.getCoordX()][APanel.getCoordY()];
                    jLayeredPane2.moveToFront(panel);
                    jLayeredPane2.moveToBack(panel);
                    jLayeredPane2.remove(BPanel);
                    panel = mazePanels[BPanel.getCoordX()][BPanel.getCoordY()];
                    jLayeredPane2.moveToFront(panel);
                    jLayeredPane2.moveToBack(panel);
                    orderMaking++;
                    if (!ordersQueue.isEmpty()) {
                        currentOrder = ordersQueue.remove(0);
                        jLabel6.setText("Delivering order number " + orderMaking);
                        processCurrentOrder();
                        if (robotX == currentOrderComponents.get(0) && robotY == currentOrderComponents.get(1)) {
                            jLabel8.setText("Delivering package");
                        } else {
                            jLabel8.setText("Picking up package");
                        }
                        putAandB();
                    } else {
                        jLabel6.setText("There are currently no orders. If you want to place an order click on the button below left");
                        currentOrderComponents.clear();
                        currentOrder = null;
                        jLabel8.setText("");
                    }
                }
            }
            jLabel7.setText("Orders in queue: " + ordersQueue.size());
            if (truckPanel != null) {
                jLayeredPane2.remove(truckPanel);
            }
            truckPanel = truckPanels.get(truckImage);
            truckPanel.setX(robotX);
            truckPanel.setY(robotY);
            int top = getGridTop(robotY);
            int left = getGridLeft(robotX);
            truckPanel.setBounds(top, left, grid_length, grid_length);
            jLayeredPane2.add(truckPanel);
            jLayeredPane2.moveToFront(truckPanel);
            revalidate();
            repaint();
        }
    }

    private void createMap() {
        int i = 0;
        for (int x = 0; x < numRows; x++) {
            for (int y = 0; y < numCols; y++) {
                Grid grid = new Grid(x, y);
                String assetAddress = imageTypes.get(map.get(i++));
                ImagedPanel panel1, panel2;
                int top = getGridTop(y);
                int left = getGridLeft(x);
                try {
                    panel1 = new ImagedPanel(assetAddress, grid_length, grid_length, x, y);
                    panel2 = new ImagedPanel(assetAddress, grid_length, grid_length, x, y);
                    panel1.setBounds(top, left, grid_length, grid_length);
                    panel2.setBounds(top, left, grid_length, grid_length);
                    mazePanels[x][y] = panel2;
                    boolean salir = false;
                    for (int j = 0; j < grids.size() && !salir; j++) {
                        Grid otherGrid = grids.get(j);
                        if (otherGrid.equals(grid)) {
                            salir = true;
                        }
                    }
                    if (salir) {
                        panel1.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                        highlightedPanels.add(panel1);
                        addMouseListenerToPanel(panel1, top, left);
                    }
                    jLayeredPane1.add(panel1);
                    jLayeredPane2.add(panel2);
                } catch (IOException ex) {
                }
            }
        }
    }

    private void addMouseListenerToPanel(ImagedPanel panel, int top, int left) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (clickEnabled && lastClicked != panel) {
                    if (currentIndex1 == 0) {
                        order = String.valueOf(panel.getCoordX()) + String.valueOf(panel.getCoordY());
                    } else {
                        order += String.valueOf(panel.getCoordX()) + String.valueOf(panel.getCoordY());
                    }
                    lastClicked = panel;
                    panel.setBorder(null);
                    jLabel3.setText(texts.get(currentIndex2));
                    currentIndex2 = (currentIndex2 + 1) % texts.size();
                    ImagedPanel letterPanel;
                    try {
                        letterPanel = new ImagedPanel(imagePaths.get(currentIndex1), grid_length, grid_length);
                        currentIndex1 = (currentIndex1 + 1) % imagePaths.size();
                        if (currentIndex1 == 0) {
                            for (ImagedPanel comp : highlightedPanels) {
                                comp.setBorder(null);
                            }
                            lastClicked = null;
                            clickEnabled = false;
                            jButton2.setVisible(true);
                            jButton3.setVisible(true);
                        }
                        letterPanel.setOpaque(false);
                        letterPanel.setBounds(top, left, grid_length, grid_length);
                        jLayeredPane1.add(letterPanel);
                        jLayeredPane1.moveToFront(letterPanel);
                        selectedPanels.add(letterPanel);
                    } catch (IOException ex) {
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (currentIndex2 == 0) {
                    panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                } else {
                    panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    public void setData(ArrayList<String> map, ArrayList<Grid> grids) {
        this.map = map;
        this.grids = grids;
        createMap();
        if (pulsado) {
            loadingScreen.setVisible(false);
            orderScreen.setVisible(true);
        }
    }

    private int getGridLeft(int x) {
        return x * grid_length;
    }

    private int getGridTop(int y) {
        return y * grid_length;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new Interfaz().setVisible(true);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        homeScreen = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        loadingScreen = new javax.swing.JPanel();
        orderScreen = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        deliveryScreen = new javax.swing.JPanel();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        jButton6 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(250, 230, 233));
        setResizable(false);

        jPanel2.setBackground(new java.awt.Color(250, 230, 233));
        jPanel2.setPreferredSize(new java.awt.Dimension(1550, 830));

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Dialog", 0, 52)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("PACKET DELIVERY");

        jPanel1.setBackground(new java.awt.Color(250, 230, 233));
        jPanel1.setForeground(new java.awt.Color(167, 167, 167));
        jPanel1.setLayout(new java.awt.CardLayout());

        homeScreen.setBackground(new java.awt.Color(250, 230, 233));
        homeScreen.setPreferredSize(new java.awt.Dimension(1550, 746));
        homeScreen.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton1.setBackground(new java.awt.Color(5, 104, 253));
        jButton1.setFont(new java.awt.Font("Dialog", 0, 40)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("MAKE AN ORDER");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton1MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton1MouseExited(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        homeScreen.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(515, 475, 520, 180));

        jPanel3.setLayout(new java.awt.GridLayout(1, 0));
        homeScreen.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(575, 40, 400, 410));

        jPanel1.add(homeScreen, "card2");

        loadingScreen.setBackground(new java.awt.Color(250, 230, 233));
        loadingScreen.setPreferredSize(new java.awt.Dimension(1550, 751));
        loadingScreen.setLayout(new java.awt.BorderLayout());
        jPanel1.add(loadingScreen, "card5");

        orderScreen.setBackground(new java.awt.Color(250, 230, 233));
        orderScreen.setPreferredSize(new java.awt.Dimension(1550, 746));
        orderScreen.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLayeredPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLayeredPane1.setPreferredSize(new java.awt.Dimension(475, 665));

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 473, Short.MAX_VALUE)
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 663, Short.MAX_VALUE)
        );

        orderScreen.add(jLayeredPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1000, 40, 475, 665));

        jLabel2.setBackground(new java.awt.Color(0, 0, 0));
        jLabel2.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel2.setText("ADD ORDER");
        orderScreen.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 45, 150, -1));

        jLabel3.setBackground(new java.awt.Color(0, 0, 0));
        jLabel3.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Select one of the locations highlighted on the map to the right as the package pickup point");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        orderScreen.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 830, -1));

        jButton2.setBackground(new java.awt.Color(250, 52, 63));
        jButton2.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Cancel order");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton2MouseExited(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        orderScreen.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 300, 150, 50));

        jButton3.setBackground(new java.awt.Color(5, 104, 253));
        jButton3.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Add order");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton3MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton3MouseExited(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        orderScreen.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(255, 300, 150, 50));

        jLabel4.setBackground(new java.awt.Color(0, 0, 0));
        jLabel4.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Order added to queue. Do you want to place another order?");
        orderScreen.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 260, 830, -1));

        jButton4.setBackground(new java.awt.Color(5, 104, 253));
        jButton4.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Yes");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton4MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton4MouseExited(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        orderScreen.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(255, 300, 150, 50));

        jButton5.setBackground(new java.awt.Color(250, 52, 63));
        jButton5.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("No");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton5MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton5MouseExited(evt);
            }
        });
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        orderScreen.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 300, 150, 50));

        jPanel1.add(orderScreen, "card3");

        deliveryScreen.setBackground(new java.awt.Color(250, 230, 233));
        deliveryScreen.setPreferredSize(new java.awt.Dimension(1550, 746));
        deliveryScreen.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLayeredPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
        jLayeredPane2.setLayout(jLayeredPane2Layout);
        jLayeredPane2Layout.setHorizontalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 473, Short.MAX_VALUE)
        );
        jLayeredPane2Layout.setVerticalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 663, Short.MAX_VALUE)
        );

        deliveryScreen.add(jLayeredPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1000, 40, 475, 665));

        jButton6.setBackground(new java.awt.Color(5, 104, 253));
        jButton6.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Make an order");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButton6MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButton6MouseExited(evt);
            }
        });
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        deliveryScreen.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 655, 140, 50));

        jLabel5.setBackground(new java.awt.Color(0, 0, 0));
        jLabel5.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel5.setText("DELIVERIES");
        deliveryScreen.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 45, 150, -1));

        jLabel6.setBackground(new java.awt.Color(0, 0, 0));
        jLabel6.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel6.setText("jLabel6");
        deliveryScreen.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, -1, -1));

        jLabel7.setBackground(new java.awt.Color(0, 0, 0));
        jLabel7.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jLabel7.setText("jLabel7");
        deliveryScreen.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, -1, -1));

        jLabel8.setBackground(new java.awt.Color(0, 0, 0));
        jLabel8.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        deliveryScreen.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 280, -1, -1));

        jLabel9.setBackground(new java.awt.Color(0, 0, 0));
        jLabel9.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        deliveryScreen.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 160, -1, -1));

        jLabel10.setBackground(new java.awt.Color(0, 0, 0));
        jLabel10.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        deliveryScreen.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, -1, -1));

        jPanel1.add(deliveryScreen, "card4");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1505, Short.MAX_VALUE)
                .addGap(39, 39, 39))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1550, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(775, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addContainerGap(74, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 750, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        pulsado = true;
        homeScreen.setVisible(false);
        if(map != null){
            orderScreen.setVisible(true);
        }else{
            loadingScreen.setVisible(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jButton2.setVisible(false);
        jButton3.setVisible(false);
        jLabel4.setText("Order added to queue. Do you want to place another order?");
        jLabel4.setVisible(true);
        jButton4.setVisible(true);
        jButton5.setVisible(true);
        if (currentOrder == null) {
            currentOrder = order;
            processCurrentOrder();
            jLabel6.setText("Delivering order number " + orderMaking);
            if (robotX == currentOrderComponents.get(0) && robotY == currentOrderComponents.get(1)) {
                jLabel8.setText("Delivering package");
            } else {
                jLabel8.setText("Picking up package");
            }
            putAandB();
        } else {
            ordersQueue.add(order);
        }
        try {
            mqttReceiver.sendMessage(order, ordersTopic);
        } catch (MqttException ex) {
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void putAandB() {
        try {
            if (APanel == null) {
                APanel = new ImagedPanel("assets/a.png", grid_length, grid_length, currentOrderComponents.get(0), currentOrderComponents.get(1));
                APanel.setOpaque(false);
                BPanel = new ImagedPanel("assets/b.png", grid_length, grid_length, currentOrderComponents.get(2), currentOrderComponents.get(3));
                BPanel.setOpaque(false);
            }
            int top = getGridTop(currentOrderComponents.get(1));
            int left = getGridLeft(currentOrderComponents.get(0));
            APanel.setBounds(top, left, grid_length, grid_length);
            jLayeredPane2.add(APanel);
            jLayeredPane2.moveToFront(APanel);
            top = getGridTop(currentOrderComponents.get(3));
            left = getGridLeft(currentOrderComponents.get(2));
            BPanel.setBounds(top, left, grid_length, grid_length);
            jLayeredPane2.add(BPanel);
            jLayeredPane2.moveToFront(BPanel);
            if (truckPanel != null) {
                jLayeredPane2.moveToFront(truckPanel);
            }
        } catch (IOException ex) {
        }
    }

    private void processCurrentOrder() {
        currentOrderComponents.clear();
        for (int i = 0; i < currentOrder.length(); i++) {
            int substring = Integer.valueOf(currentOrder.substring(i, i + 1));
            currentOrderComponents.add(substring);
        }
    }

    private void removeAllPanels() {
        for (int i = 0; i < selectedPanels.size(); i++) {
            jLayeredPane1.remove(selectedPanels.get(i));
        }
        selectedPanels.clear();
        for (int i = 0; i < highlightedPanels.size(); i++) {
            highlightedPanels.get(i).setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        }
        revalidate();
        repaint();
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        jButton4.setVisible(false);
        jButton5.setVisible(false);
        jLabel4.setVisible(false);
        jLabel3.setText(texts.get(++currentIndex2));
        removeAllPanels();
        clickEnabled = true;
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jButton2.setVisible(false);
        jButton3.setVisible(false);
        jLabel4.setText("Order canceled. Do you want to place another order?");
        jLabel4.setVisible(true);
        jButton4.setVisible(true);
        jButton5.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        orderScreen.setVisible(false);
        if (ordersQueue.isEmpty() && currentOrder == null) {
            jLabel6.setText("There are currently no orders. If you want to place an order click on the button below left");
        } else {
            jLabel6.setText("Delivering order number " + orderMaking);
        }
        jLabel7.setText("Orders in queue: " + ordersQueue.size());
        deliveryScreen.setVisible(true);
        removeAllPanels();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jButton4.setVisible(false);
        jButton5.setVisible(false);
        jLabel4.setVisible(false);
        jLabel3.setText(texts.get(++currentIndex2));
        clickEnabled = true;
        deliveryScreen.setVisible(false);
        orderScreen.setVisible(true);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseEntered
        jButton1.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton1MouseEntered

    private void jButton1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseExited
        jButton1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jButton1MouseExited

    private void jButton2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseEntered
        jButton2.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton2MouseEntered

    private void jButton2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseExited
        jButton2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jButton2MouseExited

    private void jButton3MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseEntered
        jButton3.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton3MouseEntered

    private void jButton3MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseExited
        jButton3.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jButton3MouseExited

    private void jButton4MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseEntered
        jButton4.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton4MouseEntered

    private void jButton4MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseExited
        jButton4.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jButton4MouseExited

    private void jButton5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseEntered
        jButton5.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton5MouseEntered

    private void jButton5MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseExited
        jButton5.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jButton5MouseExited

    private void jButton6MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseEntered
        jButton6.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jButton6MouseEntered

    private void jButton6MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseExited
        jButton6.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jButton6MouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel deliveryScreen;
    private javax.swing.JPanel homeScreen;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel loadingScreen;
    private javax.swing.JPanel orderScreen;
    // End of variables declaration//GEN-END:variables
}
