package main;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.time.chrono.JapaneseChronology;
import java.util.ArrayList;

/**
 * @author xiang2333
 */
public class LinkerFrame extends JFrame {
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public JPanel rootPane;
    public String titleName = "Linker - tcp转发";
    public Font font = new Font("Microsoft YaHei", Font.PLAIN, 16);

    public ArrayList<String> groups = new ArrayList<>();
    public ArrayList<String> members = new ArrayList<>();
    FontMetrics fm = getFontMetrics(font);

    public LinkerFrame() {
        groups.add("zedo");
        groups.add("xiang");
        groups.add("mizar");

        members.add("zedo[主机]");
        members.add("xiang");
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
        firstMenu.add(title,BorderLayout.NORTH);

        //组件容器(输入框-按钮)
        JPanel hBox = new JPanel();
        hBox.setLayout(new BoxLayout(hBox, BoxLayout.X_AXIS));
        //输入框
        JTextField inputNameField = new JTextField("输入名称");
        inputNameField.setPreferredSize(new Dimension(200, 30));
        inputNameField.setMinimumSize(new Dimension(200, 30));
        hBox.add(inputNameField);
        //按钮
        JButton connectButton = getButton("连接");
        hBox.add(connectButton);

        firstMenu.add(hBox,BorderLayout.CENTER);


        //二级菜单
        JPanel secondaryMenu = new JPanel();
        secondaryMenu.setLayout(new FlowLayout());
        //二级菜单左边
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));
        leftSide.add(getLabel("加入组"));
        //二级菜单组列表图
        DefaultMutableTreeNode groupTop = new DefaultMutableTreeNode("组");
        for (String s:groups) {
            groupTop.add(new DefaultMutableTreeNode(s));
        }
        JTree tree = new JTree(groupTop);
        leftSide.add(tree);
        //下边俩按钮
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel,BoxLayout.X_AXIS));
        JButton refreshButton =getButton("刷新");
        JButton joinButton =getButton("加入");
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(joinButton);

        leftSide.add(buttonsPanel);

        secondaryMenu.add(leftSide);

        //二级菜单右边
        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        //二级菜单组名输入框
        JTextField groupNameField = new JTextField();
        groupNameField.setToolTipText("输入组名");
        //二级菜单创建组按钮
        JButton creatGroup = getButton("创建组");

        rightSide.add(groupNameField);
        rightSide.add(creatGroup);
        secondaryMenu.add(rightSide);

        //主页连接按钮事件
        connectButton.addActionListener(e->{
            setContentPane(secondaryMenu);
            validate();
        });


        //三级菜单
        JPanel thirdMenu = new JPanel();
        thirdMenu.setLayout(new BoxLayout(thirdMenu,BoxLayout.Y_AXIS));
        //三级菜单标题
        thirdMenu.add(getLabel("zedo组中"));
        //三级菜单成员列表
        DefaultMutableTreeNode memberTop = new DefaultMutableTreeNode("zedo组");
        for (String s : members){
            memberTop.add(new DefaultMutableTreeNode(s));
        }
        JTree membersTree = new JTree(memberTop);
        thirdMenu.add(membersTree);
        //三级菜单离开按钮
        JButton leaveButton = getButton("离开");
        leaveButton.addActionListener(e -> {
            //离开按钮事件
            setContentPane(secondaryMenu);
            repaint();
            validate();
        });
        thirdMenu.add(leaveButton);

        joinButton.addActionListener(e->{
            //二级菜单加入按钮事件
            setContentPane(thirdMenu);
            repaint();
            validate();
        });
        creatGroup.addActionListener(e->{
            //二级菜单创建组按钮事件
            groupTop.add(new DefaultMutableTreeNode(groupNameField.getText()));
            tree.validate();
            tree.updateUI();
            tree.repaint();
        });
        rootPane.add(firstMenu);

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