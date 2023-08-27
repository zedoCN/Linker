package main;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import data.EventPack;
import mode.LinkerClient;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author xiang2333
 */
public class LinkerFrame extends JFrame {
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public JPanel rootPane;
    public String titleName = "Linker - tcp转发";
    public Font font = new Font("Microsoft YaHei", Font.PLAIN, 16);


    //FontMetrics fm = getFontMetrics(font);
    public Properties properties = new Properties();
    LinkerClient linkerClient;
    JSONArray groupsJsonArray;

    public void saveProperties() {
        try {
            properties.store(new FileWriter("./Linker.cfg", StandardCharsets.UTF_8), "Linker");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LinkerFrame() {


        try {
            System.out.println("载入配置");
            properties.load(new FileReader("./Linker.cfg", StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //大小300*175
        setSize((int) (300f / 1920 * SCREEN_SIZE.width), (int) (175f / 1080 * SCREEN_SIZE.height));
        //居中
        setLocation((SCREEN_SIZE.width - getWidth()) / 2, (SCREEN_SIZE.height - getHeight()) / 2);
        rootPane = new JPanel();
        setContentPane(rootPane);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        //首页
        JPanel firstMenu = new JPanel();
        firstMenu.setLayout(new BorderLayout());
        //首页标题
        JLabel title = getLabel(titleName);
        firstMenu.add(title, BorderLayout.NORTH);

        //组件容器(输入框-按钮)
        JPanel hBox = new JPanel();
        hBox.setLayout(new BoxLayout(hBox, BoxLayout.X_AXIS));
        //输入框
        JTextField inputNameField = new JTextField("输入名称");
        inputNameField.setText(properties.getProperty("Name"));
        inputNameField.setPreferredSize(new Dimension(200, 30));
        inputNameField.setMinimumSize(new Dimension(200, 30));
        hBox.add(inputNameField);
        //按钮
        JButton connectButton = getButton("连接");

        hBox.add(connectButton);

        firstMenu.add(hBox, BorderLayout.CENTER);


        //二级菜单
        JPanel secondaryMenu = new JPanel();
        secondaryMenu.setLayout(new FlowLayout());
        //二级菜单左边
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
        leftSide.add(getLabel("加入组"));


        JList<String> list = new JList<>();
        list.setPreferredSize(new Dimension(80, 100));
        list.setMinimumSize(new Dimension(80, 100));

        leftSide.add(list);
        DefaultListModel<String> model = new DefaultListModel<>();
        list.setModel(model);

        //下边俩按钮
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        JButton refreshButton = getButton("刷新");
        refreshButton.addActionListener((e -> {
            linkerClient.getGroups();
        }));
        JButton joinButton = getButton("加入");

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(joinButton);
        //二级菜单端口输入框
        JTextField userPortField = new JTextField();
        userPortField.setText(properties.getProperty("userPort"));
        //userPortField.setToolTipText("输入用户端口");
        {
            JPanel hBoxField = new JPanel();
            hBoxField.setLayout(new BoxLayout(hBoxField, BoxLayout.X_AXIS));
            hBoxField.add(new JLabel("用户端口："));
            hBoxField.add(userPortField);

            leftSide.add(hBoxField);
        }
        leftSide.add(buttonsPanel);


        secondaryMenu.add(leftSide);

        //二级菜单右边
        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        //二级菜单组名输入框
        JTextField groupNameField = new JTextField();
        groupNameField.setText(properties.getProperty("groupName"));
        //groupNameField.setToolTipText("输入组名");

        //二级菜单创建组按钮
        JButton creatGroup = getButton("创建组");

        //二级菜单端口输入框
        JTextField hostPortField = new JTextField();
        hostPortField.setText(properties.getProperty("hostPort"));
        //hostPortField.setToolTipText("输入主机端口");


        {
            JPanel hBoxField = new JPanel();
            hBoxField.setLayout(new BoxLayout(hBoxField, BoxLayout.X_AXIS));
            hBoxField.add(new JLabel("组名："));
            hBoxField.add(groupNameField);

            rightSide.add(hBoxField);
        }
        {
            JPanel hBoxField = new JPanel();
            hBoxField.setLayout(new BoxLayout(hBoxField, BoxLayout.X_AXIS));
            hBoxField.add(new JLabel("主机端口："));
            hBoxField.add(hostPortField);

            rightSide.add(hBoxField);
        }

        rightSide.setSize(160, 80);
        rightSide.setPreferredSize(new Dimension(160, 80));
        rightSide.setMinimumSize(new Dimension(160, 80));
        rightSide.add(creatGroup);
        secondaryMenu.add(rightSide);

        //主页连接按钮事件
        connectButton.addActionListener(e -> {
            setContentPane(secondaryMenu);
            validate();
        });


        //三级菜单
        JPanel thirdMenu = new JPanel();
        thirdMenu.setLayout(new BoxLayout(thirdMenu, BoxLayout.Y_AXIS));
        //三级菜单标题
        thirdMenu.add(getLabel("zedo组中"));
        //三级菜单成员列表
        DefaultListModel<String> members = new DefaultListModel<>();
        JList<String> membersList = new JList<>(members);
        membersList.setPreferredSize(new Dimension(80, 100));
        membersList.setMinimumSize(new Dimension(80, 100));
        thirdMenu.add(membersList);
        //三级菜单离开按钮
        JButton leaveButton = getButton("离开");
        leaveButton.addActionListener(e -> {
            //离开按钮事件
            setContentPane(secondaryMenu);
            repaint();
            validate();
        });
        thirdMenu.add(leaveButton);

        joinButton.addActionListener(e -> {
            properties.put("userPort", userPortField.getText());
            saveProperties();
            JSONObject group = groupsJsonArray.getJSONObject(list.getSelectedIndex());
            linkerClient.joinGroup(group.getString("uuid"), Integer.parseInt(properties.getProperty("userPort")));
            /*linkerClient.createGroup(properties.getProperty("groupName"),
                    Integer.parseInt(properties.getProperty("hostPort")));*/

            //二级菜单加入按钮事件
            setContentPane(thirdMenu);
            repaint();
            validate();
        });
        creatGroup.addActionListener(e -> {
            //二级菜单创建组按钮事件
            properties.put("groupName", groupNameField.getText());
            properties.put("hostPort", hostPortField.getText());
            saveProperties();

            linkerClient.createGroup(properties.getProperty("groupName"),
                    Integer.parseInt(properties.getProperty("hostPort")));

            setContentPane(thirdMenu);
            repaint();
            validate();


            linkerClient.getGroupMembers();
            /*groupTop.add(new DefaultMutableTreeNode(groupNameField.getText()));
            tree.validate();
            tree.updateUI();
            tree.repaint();*/
        });
        rootPane.add(firstMenu);


        connectButton.addActionListener(e -> {
            //按钮按下 连接Linker服务器
            System.out.println("连接Linker服务器");
            properties.setProperty("Name", inputNameField.getText());
            saveProperties();
            linkerClient = new LinkerClient(properties.getProperty("LinkerServerIp"),
                    Integer.parseInt(properties.getProperty("LinkerServerPort")),
                    properties.getProperty("Name"), eventPack -> {

                switch (eventPack.getType()) {
                    case USER_GET_IDENTITY -> {
                        System.out.println(eventPack.getParameterJSON());
                        setTitle("Linker 连接成功:" + eventPack.getParameterJSON().getString("name"));
                        linkerClient.getGroups();
                    }
                    case GET_GROUPS -> {
                        groupsJsonArray = eventPack.getParameterJSON().getJSONArray("groups");
                        model.clear();
                        if (groupsJsonArray.size() > 0) {
                            for (int i = 0; i < groupsJsonArray.size(); i++) {
                                JSONObject group = groupsJsonArray.getJSONObject(i);
                                model.add(i, group.getString("name") + "   [" + group.getString("hostName") + "]   共:" + group.getIntValue("users") + "人");
                            }
                        } else {
                            model.add(0, "          暂无组        ");
                        }

                    }
                    case GROUP_JOIN, GROUP_LEAVE -> {
                        linkerClient.getGroupMembers();
                    }
                    case USER_GET_GROUP_MEMBER -> {
                        members.clear();
                        JSONArray membersJSON = eventPack.getParameterJSON().getJSONArray("members");
                        for (int i = 0; i < membersJSON.size(); i++) {
                            JSONObject member = membersJSON.getJSONObject(i);
                            members.add(i, (i == 0 ? "[主机]" : "") + "       " + member.getString("name") + "      ");
                        }
                    }
                }


            });
        });


    }

    /*private Dimension getStringDim(String str){
        LineMetrics metrics = fm.getLineMetrics(str,getGraphics());
        return new Dimension(fm.stringWidth(str),(int)(metrics.getAscent() + Math.abs(metrics.getDescent()) + metrics.getLeading()));
    }*/
    private JLabel getLabel(String label) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(font);
        jLabel.setAlignmentX(CENTER_ALIGNMENT);
        return jLabel;
    }

    private JButton getButton(String name) {
        JButton button = new JButton(name);
        button.setFont(font);
        button.setAlignmentX(CENTER_ALIGNMENT);
        return button;
    }

    public static void main(String[] args) {
        new LinkerFrame().setVisible(true);
    }
}