import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CupOJavaGui {

    public static final int SEARCH = 0;
    public static final int DELETE = 1;
    public static final int button3 = 2;
    private final QueryMaker qm;
    JButton[] buttons;
    JTable centerDisplayTable;
    GridBagConstraints centerGbc;
    GridBagLayout centerGbl;
    JScrollPane centerScrollPane;
    GridBagConstraints eastGbc;
    GridBagLayout eastGbl;
    JFrame frame;
    JLabel guiTitle;
    JPanel panelCenter;
    JPanel panelEast;
    JPanel panelNorth;
    JPanel panelWest;
    GridBagConstraints westGbc;
    GridBagLayout westGbl;
    private Object[][] centerDisplayData;
    public CupOJavaGui(QueryMaker qm) throws SQLException {
        this.qm = qm;
        frame = new JFrame("Cup O' Java");
        getTitlePanelNorth("This is my location");
        getDisplayTableCenter();
        getOptionsPanelWest(new String[]{"SEARCH", " Button2", "Button3"});
        getTableListPanelEast(new String[]{"Button1", " Button2", "Button3", "Button4"});
        frame.setLocationRelativeTo(null);
        ResultSet rs = qm.generateQuery(" SELECT * FROM " + qm.getTableName());
        refresh(qm.extractResults(rs, false));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        final Credentials credentials = new Credentials();
        QueryMaker qm = credentials.getQueryMaker();
        qm.setTableName("inventory");
        CupOJavaGui coj = new CupOJavaGui(qm);
    }

    private void refresh(Object[][] data) throws SQLException {
        centerDisplayData = data;
        DefaultTableModel dm = (DefaultTableModel) centerDisplayTable.getModel();
        dm.setDataVector(this.centerDisplayData, qm.getColumnNames());
        dm.fireTableDataChanged();
    }

    private void createVerticleButtonLayout(String[] buttonNames, GridBagConstraints constraints, JPanel panel, JButton[] buttons) {
        for (int i = 0; i < buttonNames.length; i++) {
            constraints.gridy = i;
            buttons[i] = new JButton(buttonNames[i]);
            buttons[i].setBackground(Color.white);
            buttons[i].setForeground(Color.black);
            buttons[i].setFont(new java.awt.Font("Arial", Font.BOLD, 12));

            panel.add(this.buttons[i], constraints);

        }
        buttons[SEARCH].addActionListener(e -> {
            String columnValue = null;
            String columnName = null;
            String tableName = null;
            try {
                while (columnValue == null || columnValue.isEmpty()) {
                    columnValue = JOptionPane.showInputDialog("Enter value of column");
                    if (columnValue.isEmpty()) {
                        JOptionPane optionPane = new JOptionPane("Enter a valid column value!", JOptionPane.ERROR_MESSAGE);
                        JDialog dialog = optionPane.createDialog("Failure");
                        dialog.setAlwaysOnTop(true);
                        dialog.setVisible(true);
                    }
                }
                while (columnName == null || columnName.isEmpty()) {
                    columnName = JOptionPane.showInputDialog("Enter column Name");
                    if (columnName.isEmpty()) {
                        JOptionPane optionPane = new JOptionPane("Enter a valid column name!", JOptionPane.ERROR_MESSAGE);
                        JDialog dialog = optionPane.createDialog("Failure");
                        dialog.setAlwaysOnTop(true);
                        dialog.setVisible(true);
                    }

                }
                while (tableName == null || tableName.isEmpty()) {
                    tableName = JOptionPane.showInputDialog("Enter table Name");
                    if (tableName.isEmpty()) {
                        JOptionPane optionPane = new JOptionPane("Enter a valid table name!", JOptionPane.ERROR_MESSAGE);
                        JDialog dialog = optionPane.createDialog("Failure");
                        dialog.setAlwaysOnTop(true);
                        dialog.setVisible(true);
                    }
                }
                Object[][] rows = qm.getProduct(tableName, columnName, columnValue);
                refresh(rows);
            } catch (NullPointerException | SQLException ignore) {
            } finally {
            }

        });

        buttons[DELETE].addActionListener(e -> {

        });

    }

    public void getDisplayTableCenter() {
        centerGbl = new GridBagLayout();
        centerGbc = new GridBagConstraints();
        panelCenter = new JPanel(centerGbl);
        centerScrollPane = new JScrollPane();
        centerDisplayTable = new JTable();
        panelCenter.add(centerScrollPane);
        centerScrollPane.getViewport().add(centerDisplayTable);
        frame.getContentPane().add(panelCenter, BorderLayout.CENTER);
    }

    private void getOptionsPanelWest(String[] buttonNames) {
        westGbl = new GridBagLayout();
        westGbc = new GridBagConstraints();
        panelWest = new JPanel(westGbl);
        buttons = new JButton[buttonNames.length];
        createVerticleButtonLayout(buttonNames, westGbc, panelWest, buttons);
        frame.getContentPane().add(panelWest, BorderLayout.WEST);
    }

    private void getTableListPanelEast(String[] buttonNames) {
        eastGbl = new GridBagLayout();
        eastGbc = new GridBagConstraints();
        panelEast = new JPanel(eastGbl);
        buttons = new JButton[buttonNames.length];
        createVerticleButtonLayout(buttonNames, eastGbc, panelEast, buttons);
        frame.getContentPane().add(panelEast, BorderLayout.EAST);
    }

    //Create new title for gui
    private void getTitlePanelNorth(String title) {
        guiTitle = new JLabel(title);
        panelNorth = new JPanel();
        panelNorth.add(guiTitle);
    }


    /*public void setData(Object[][] extractResults , String[] headers) {
        DefaultTableModel tm = new DefaultTableModel();
        tm.setDataVector(extractResults, headers);
        centerDisplayTable.setModel(tm);
        tm.fireTableDataChanged();
    }*/
}
